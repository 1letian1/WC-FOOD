package com.shike.ordering.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.shike.ordering.auth.model.CurrentPrincipal;
import com.shike.ordering.auth.model.PrincipalContext;
import com.shike.ordering.auth.model.PrincipalType;
import com.shike.ordering.common.exception.BusinessException;
import com.shike.ordering.common.exception.ResourceNotFoundException;
import com.shike.ordering.config.ShopProperties;
import com.shike.ordering.dto.user.OrderCreateDTO;
import com.shike.ordering.entity.*;
import com.shike.ordering.mapper.*;
import com.shike.ordering.mapper.projection.OrderCartItemProjection;
import com.shike.ordering.service.user.OrderIdempotencyService;
import com.shike.ordering.service.user.OrderNumberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderCreationServiceImplTest {
    private final OrderMapper orderMapper = mock(OrderMapper.class);
    private final OrderItemMapper orderItemMapper = mock(OrderItemMapper.class);
    private final OrderStatusLogMapper statusLogMapper = mock(OrderStatusLogMapper.class);
    private final ShoppingCartMapper cartMapper = mock(ShoppingCartMapper.class);
    private final ProductMapper productMapper = mock(ProductMapper.class);
    private final ShopMapper shopMapper = mock(ShopMapper.class);
    private final AddressMapper addressMapper = mock(AddressMapper.class);
    private final UserMapper userMapper = mock(UserMapper.class);
    private final OrderNumberService orderNumberService = mock(OrderNumberService.class);
    private final OrderIdempotencyService idempotencyService = mock(OrderIdempotencyService.class);
    private final OrderCreationServiceImpl service = new OrderCreationServiceImpl(orderMapper, orderItemMapper,
            statusLogMapper, cartMapper, productMapper, shopMapper, addressMapper, userMapper,
            orderNumberService, idempotencyService, new ShopProperties(1L));

    @BeforeEach
    void setUp() {
        PrincipalContext.set(new CurrentPrincipal(12L, PrincipalType.USER, null, "token"));
        when(orderMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(idempotencyService.claim(12L, "idem-key-001")).thenReturn(true);
        when(shopMapper.selectByIdForUpdate(1L)).thenReturn(shop());
        when(cartMapper.selectOrderItems(12L, List.of(5L))).thenReturn(List.of(item()));
        when(orderNumberService.nextOrderNo()).thenReturn("2026071400000001");
        when(productMapper.deductStock(3L, 1L, 2)).thenReturn(1);
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(88L);
            order.setCreateTime(LocalDateTime.of(2026, 7, 14, 12, 0));
            return 1;
        }).when(orderMapper).insert(any(Order.class));
    }

    @AfterEach void tearDown() { PrincipalContext.clear(); }

    @Test
    void createDelivery_shouldRecalculateAmountAndSnapshotOwnedAddress() {
        when(addressMapper.selectOne(any(Wrapper.class))).thenReturn(address());

        var result = service.create(deliveryRequest(), "idem-key-001");

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).insert(orderCaptor.capture());
        Order order = orderCaptor.getValue();
        assertThat(order.getUserId()).isEqualTo(12L);
        assertThat(order.getShopId()).isEqualTo(1L);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("25.00");
        assertThat(order.getDeliveryFee()).isEqualByComparingTo("3.00");
        assertThat(order.getPackageFee()).isEqualByComparingTo("1.00");
        assertThat(order.getPayAmount()).isEqualByComparingTo("29.00");
        assertThat(order.getContactName()).isEqualTo("收货人");
        assertThat(order.getAddressArea()).isEqualTo("高新区");
        assertThat(result.orderNo()).isEqualTo("2026071400000001");

        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemMapper).insert(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getProductName()).isEqualTo("牛肉饭");
        assertThat(itemCaptor.getValue().getUnitPrice()).isEqualByComparingTo("12.50");
        assertThat(itemCaptor.getValue().getAmount()).isEqualByComparingTo("25.00");

        ArgumentCaptor<OrderStatusLog> logCaptor = ArgumentCaptor.forClass(OrderStatusLog.class);
        verify(statusLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getToStatus()).isEqualTo(1);
        assertThat(logCaptor.getValue().getOperatorType()).isEqualTo("USER");
        assertThat(logCaptor.getValue().getOrderVersion()).isZero();
        verify(cartMapper).delete(any(Wrapper.class));
    }

    @Test
    void createDineIn_shouldNeverPersistDeliveryFeeOrAddress() {
        service.create(dineInRequest(), "idem-key-001");

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).insert(captor.capture());
        Order order = captor.getValue();
        assertThat(order.getDeliveryFee()).isEqualByComparingTo("0.00");
        assertThat(order.getAddressId()).isNull();
        assertThat(order.getAddressArea()).isNull();
        assertThat(order.getDeliveryRangeSnapshot()).isNull();
        assertThat(order.getTableNo()).isEqualTo("A06");
        verify(addressMapper, never()).selectOne(any(Wrapper.class));
    }

    @Test
    void createDelivery_whenBelowMinimum_shouldNotDeductStockOrCreateOrder() {
        Shop shop = shop();
        shop.setMinDeliveryAmount(new BigDecimal("30.00"));
        when(shopMapper.selectByIdForUpdate(1L)).thenReturn(shop);
        when(addressMapper.selectOne(any(Wrapper.class))).thenReturn(address());

        assertThatThrownBy(() -> service.create(deliveryRequest(), "idem-key-001"))
                .isInstanceOf(BusinessException.class).extracting("code").isEqualTo(70004);
        verify(productMapper, never()).deductStock(anyLong(), anyLong(), anyInt());
        verify(orderMapper, never()).insert(any(Order.class));
    }

    @Test
    void create_whenConditionalStockDeductionFails_shouldNotInsertOrder() {
        when(productMapper.deductStock(3L, 1L, 2)).thenReturn(0);

        assertThatThrownBy(() -> service.create(dineInRequest(), "idem-key-001"))
                .isInstanceOf(BusinessException.class).extracting("code").isEqualTo(40004);
        verify(orderMapper, never()).insert(any(Order.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
        verify(statusLogMapper, never()).insert(any(OrderStatusLog.class));
        verify(cartMapper, never()).delete(any(Wrapper.class));
    }

    @Test
    void createDelivery_whenAddressDoesNotBelongToUser_shouldFailBeforeStockDeduction() {
        when(addressMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> service.create(deliveryRequest(), "idem-key-001"))
                .isInstanceOf(ResourceNotFoundException.class).extracting("code").isEqualTo(50002);
        verify(productMapper, never()).deductStock(anyLong(), anyLong(), anyInt());
    }

    @Test
    void create_withSuccessfulExistingIdempotencyKey_shouldReturnOriginalOrder() {
        Order existing = existingOrder();
        when(orderMapper.selectOne(any(Wrapper.class))).thenReturn(existing);

        var result = service.create(dineInRequest(), "idem-key-001");

        assertThat(result.id()).isEqualTo(66L);
        assertThat(result.orderNo()).isEqualTo("2026071400000099");
        verify(idempotencyService, never()).claim(anyLong(), anyString());
        verify(productMapper, never()).deductStock(anyLong(), anyLong(), anyInt());
    }

    @Test
    void create_whenShopIsClosed_shouldFailBeforeStockDeduction() {
        Shop shop = shop();
        shop.setBusinessStatus(0);
        when(shopMapper.selectByIdForUpdate(1L)).thenReturn(shop);

        assertBusinessFailure(dineInRequest(), 70001);
    }

    @Test
    void create_whenDineInIsDisabled_shouldFailBeforeStockDeduction() {
        Shop shop = shop();
        shop.setDineInEnabled(false);
        when(shopMapper.selectByIdForUpdate(1L)).thenReturn(shop);

        assertBusinessFailure(dineInRequest(), 70002);
    }

    @Test
    void create_whenDeliveryIsDisabled_shouldFailBeforeStockDeduction() {
        Shop shop = shop();
        shop.setDeliveryEnabled(false);
        when(shopMapper.selectByIdForUpdate(1L)).thenReturn(shop);

        assertBusinessFailure(deliveryRequest(), 70003);
    }

    @Test
    void create_whenProductIsOffSale_shouldFailBeforeStockDeduction() {
        OrderCartItemProjection item = item();
        item.setProductStatus(0);
        when(cartMapper.selectOrderItems(12L, List.of(5L))).thenReturn(List.of(item));

        assertBusinessFailure(dineInRequest(), 40002);
    }

    @Test
    void create_whenProductIsSoldOut_shouldFailBeforeStockDeduction() {
        OrderCartItemProjection item = item();
        item.setProductStatus(2);
        when(cartMapper.selectOrderItems(12L, List.of(5L))).thenReturn(List.of(item));

        assertBusinessFailure(dineInRequest(), 40003);
    }

    @Test
    void create_whenIdempotencyKeyIsProcessing_shouldReturnConflictWithoutWriting() {
        when(idempotencyService.claim(12L, "idem-key-001")).thenReturn(false);

        assertBusinessFailure(dineInRequest(), 60004);
        verify(idempotencyService, never()).registerCompletion(anyLong(), anyString(), any());
    }

    @Test
    void create_whenSelectedCartItemDoesNotBelongToUser_shouldReturnNotFound() {
        when(cartMapper.selectOrderItems(12L, List.of(5L))).thenReturn(List.of());

        assertThatThrownBy(() -> service.create(dineInRequest(), "idem-key-001"))
                .isInstanceOf(ResourceNotFoundException.class).extracting("code").isEqualTo(50003);
        verify(productMapper, never()).deductStock(anyLong(), anyLong(), anyInt());
    }

    private void assertBusinessFailure(OrderCreateDTO request, int code) {
        assertThatThrownBy(() -> service.create(request, "idem-key-001"))
                .isInstanceOf(BusinessException.class).extracting("code").isEqualTo(code);
        verify(productMapper, never()).deductStock(anyLong(), anyLong(), anyInt());
        verify(orderMapper, never()).insert(any(Order.class));
    }

    private OrderCreateDTO deliveryRequest() {
        return new OrderCreateDTO(2, List.of(5L), null, null, null, false, 7L, "少辣");
    }

    private OrderCreateDTO dineInRequest() {
        return new OrderCreateDTO(1, List.of(5L), "小李", "13800000000", "A06", false, null, null);
    }

    private Shop shop() {
        Shop shop = new Shop();
        shop.setId(1L);
        shop.setBusinessStatus(1);
        shop.setDineInEnabled(true);
        shop.setDeliveryEnabled(true);
        shop.setDeliveryFee(new BigDecimal("3.00"));
        shop.setPackageFee(new BigDecimal("1.00"));
        shop.setMinDeliveryAmount(new BigDecimal("20.00"));
        shop.setDeliveryRange("商家配送");
        return shop;
    }

    private Address address() {
        Address address = new Address();
        address.setId(7L);
        address.setUserId(12L);
        address.setContactName("收货人");
        address.setPhone("13900000000");
        address.setArea("高新区");
        address.setDetail("阳光花园");
        address.setHouseNumber("2栋1203");
        return address;
    }

    private OrderCartItemProjection item() {
        OrderCartItemProjection item = new OrderCartItemProjection();
        item.setCartId(5L);
        item.setShopId(1L);
        item.setProductId(3L);
        item.setProductName("牛肉饭");
        item.setProductImageUrl("/files/beef.png");
        item.setProductStatus(1);
        item.setProductStock(10);
        item.setProductExists(true);
        item.setCategoryAvailable(true);
        item.setSpecificationId(9L);
        item.setSpecificationName("大份");
        item.setSpecificationValid(true);
        item.setTasteId(11L);
        item.setTasteName("少辣");
        item.setTasteValid(true);
        item.setUnitPrice(new BigDecimal("12.50"));
        item.setQuantity(2);
        return item;
    }

    private Order existingOrder() {
        Order order = new Order();
        order.setId(66L);
        order.setOrderNo("2026071400000099");
        order.setOrderType(1);
        order.setStatus(1);
        order.setTotalAmount(new BigDecimal("25.00"));
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setPackageFee(BigDecimal.ZERO);
        order.setPayAmount(new BigDecimal("25.00"));
        return order;
    }
}
