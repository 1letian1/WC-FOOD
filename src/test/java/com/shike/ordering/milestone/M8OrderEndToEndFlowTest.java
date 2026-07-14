package com.shike.ordering.milestone;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.shike.ordering.auth.model.CurrentPrincipal;
import com.shike.ordering.auth.model.PrincipalContext;
import com.shike.ordering.auth.model.PrincipalType;
import com.shike.ordering.common.enums.OrderStatus;
import com.shike.ordering.common.enums.OrderType;
import com.shike.ordering.config.ShopProperties;
import com.shike.ordering.dto.user.OrderCreateDTO;
import com.shike.ordering.entity.*;
import com.shike.ordering.mapper.*;
import com.shike.ordering.mapper.projection.OrderCartItemProjection;
import com.shike.ordering.service.impl.common.OrderServiceImpl;
import com.shike.ordering.service.impl.user.OrderCreationServiceImpl;
import com.shike.ordering.service.user.OrderIdempotencyService;
import com.shike.ordering.service.user.OrderNumberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class M8OrderEndToEndFlowTest {
    private static final long USER_ID = 12L;
    private static final long MERCHANT_ID = 5L;
    private static final long SHOP_ID = 1L;
    private static final long ORDER_ID = 88L;

    @AfterEach
    void clearPrincipal() {
        PrincipalContext.clear();
    }

    @Test
    void dineInOrder_fromCreateToMerchantComplete_shouldCloseTheLoop() {
        Fixture fixture = new Fixture(OrderType.DINE_IN);

        var created = fixture.create(new OrderCreateDTO(1, List.of(5L), "小李", "13800000000",
                "A06", false, null, "少辣"));
        fixture.asMerchant();
        fixture.fulfillment.accept(ORDER_ID);
        fixture.fulfillment.startCooking(ORDER_ID);
        fixture.fulfillment.readyForPickup(ORDER_ID);
        fixture.fulfillment.complete(ORDER_ID);

        assertThat(created.status().code()).isEqualTo(OrderStatus.PENDING_ACCEPT.getCode());
        assertThat(created.payAmount()).isEqualByComparingTo("26.00");
        fixture.assertFinalState(OrderStatus.COMPLETED,
                OrderStatus.PENDING_ACCEPT, OrderStatus.ACCEPTED, OrderStatus.COOKING,
                OrderStatus.READY_FOR_PICKUP, OrderStatus.COMPLETED);
    }

    @Test
    void deliveryOrder_fromCreateToUserConfirmation_shouldCloseTheLoop() {
        Fixture fixture = new Fixture(OrderType.DELIVERY);

        var created = fixture.create(new OrderCreateDTO(2, List.of(5L), null, null,
                null, false, 7L, "到达后电话联系"));
        fixture.asMerchant();
        fixture.fulfillment.accept(ORDER_ID);
        fixture.fulfillment.startCooking(ORDER_ID);
        fixture.fulfillment.startDelivery(ORDER_ID);
        fixture.fulfillment.markDelivered(ORDER_ID);
        fixture.asUser();
        fixture.fulfillment.confirmReceipt(ORDER_ID);

        assertThat(created.payAmount()).isEqualByComparingTo("29.00");
        fixture.assertFinalState(OrderStatus.COMPLETED,
                OrderStatus.PENDING_ACCEPT, OrderStatus.ACCEPTED, OrderStatus.COOKING,
                OrderStatus.DELIVERING, OrderStatus.DELIVERED, OrderStatus.COMPLETED);
    }

    private static final class Fixture {
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
        private final AtomicReference<Order> persistedOrder = new AtomicReference<>();
        private final AtomicInteger stock = new AtomicInteger(20);
        private final List<OrderItem> items = new ArrayList<>();
        private final List<OrderStatusLog> logs = new ArrayList<>();
        private final OrderCreationServiceImpl creation;
        private final OrderServiceImpl fulfillment;

        private Fixture(OrderType type) {
            when(orderMapper.selectOne(any(Wrapper.class))).thenReturn(null);
            when(idempotencyService.claim(USER_ID, "idem-key-001")).thenReturn(true);
            when(userMapper.selectIdForUpdate(USER_ID)).thenReturn(USER_ID);
            when(shopMapper.selectByIdForUpdate(SHOP_ID)).thenReturn(shop());
            when(cartMapper.selectOrderItems(USER_ID, List.of(5L))).thenReturn(List.of(cartItem()));
            when(addressMapper.selectOne(any(Wrapper.class))).thenReturn(type == OrderType.DELIVERY ? address() : null);
            when(orderNumberService.nextOrderNo()).thenReturn("2026071400000001");
            when(productMapper.deductStock(3L, SHOP_ID, 2)).thenAnswer(invocation -> {
                stock.addAndGet(-2);
                return 1;
            });
            when(cartMapper.delete(any(Wrapper.class))).thenReturn(1);
            when(orderItemMapper.selectByOrderId(ORDER_ID)).thenAnswer(invocation -> List.copyOf(items));
            when(orderMapper.selectMerchantOrder(eq(ORDER_ID), eq(SHOP_ID))).thenAnswer(invocation -> copy(persistedOrder.get()));
            when(orderMapper.selectUserOrder(eq(ORDER_ID), eq(USER_ID))).thenAnswer(invocation -> copy(persistedOrder.get()));

            doAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(ORDER_ID);
                order.setCreateTime(LocalDateTime.of(2026, 7, 14, 10, 0));
                persistedOrder.set(copy(order));
                return 1;
            }).when(orderMapper).insert(any(Order.class));
            doAnswer(invocation -> {
                items.add(invocation.getArgument(0));
                return 1;
            }).when(orderItemMapper).insert(any(OrderItem.class));
            doAnswer(invocation -> {
                logs.add(invocation.getArgument(0));
                return 1;
            }).when(statusLogMapper).insert(any(OrderStatusLog.class));
            when(orderMapper.transitionMerchantOrder(anyLong(), anyLong(), anyInt(), anyInt(), anyInt(), nullable(String.class)))
                    .thenAnswer(invocation -> transition(invocation.getArgument(2), invocation.getArgument(3), invocation.getArgument(4)));
            when(orderMapper.transitionUserOrder(anyLong(), anyLong(), anyInt(), anyInt(), anyInt()))
                    .thenAnswer(invocation -> transition(invocation.getArgument(2), invocation.getArgument(3), invocation.getArgument(4)));

            creation = new OrderCreationServiceImpl(orderMapper, orderItemMapper, statusLogMapper, cartMapper,
                    productMapper, shopMapper, addressMapper, userMapper, orderNumberService,
                    idempotencyService, new ShopProperties(SHOP_ID));
            fulfillment = new OrderServiceImpl(orderMapper, orderItemMapper, statusLogMapper, productMapper);
        }

        private com.shike.ordering.vo.user.OrderCreateVO create(OrderCreateDTO request) {
            asUser();
            return creation.create(request, "idem-key-001");
        }

        private void asUser() {
            PrincipalContext.set(new CurrentPrincipal(USER_ID, PrincipalType.USER, SHOP_ID, "not-logged"));
        }

        private void asMerchant() {
            PrincipalContext.set(new CurrentPrincipal(MERCHANT_ID, PrincipalType.MERCHANT, SHOP_ID, "not-logged"));
        }

        private int transition(int expectedStatus, int targetStatus, int expectedVersion) {
            Order current = persistedOrder.get();
            if (current.getStatus() != expectedStatus || current.getVersion() != expectedVersion) {
                return 0;
            }
            Order updated = copy(current);
            updated.setStatus(targetStatus);
            updated.setVersion(expectedVersion + 1);
            persistedOrder.set(updated);
            return 1;
        }

        private void assertFinalState(OrderStatus expected, OrderStatus... history) {
            assertThat(persistedOrder.get().getStatus()).isEqualTo(expected.getCode());
            assertThat(stock).hasValue(18);
            assertThat(items).hasSize(1);
            assertThat(logs).extracting(OrderStatusLog::getToStatus)
                    .containsExactly(java.util.Arrays.stream(history).map(OrderStatus::getCode).toArray(Integer[]::new));
            assertThat(logs).extracting(OrderStatusLog::getOrderVersion)
                    .containsExactly(java.util.stream.IntStream.range(0, history.length).boxed().toArray(Integer[]::new));
        }

        private Shop shop() {
            Shop value = new Shop();
            value.setId(SHOP_ID);
            value.setBusinessStatus(1);
            value.setDineInEnabled(true);
            value.setDeliveryEnabled(true);
            value.setDeliveryFee(new BigDecimal("3.00"));
            value.setPackageFee(new BigDecimal("1.00"));
            value.setMinDeliveryAmount(new BigDecimal("20.00"));
            value.setDeliveryRange("店铺周边3公里");
            return value;
        }

        private Address address() {
            Address value = new Address();
            value.setId(7L);
            value.setUserId(USER_ID);
            value.setContactName("收货人");
            value.setPhone("13900000000");
            value.setArea("高新区");
            value.setDetail("阳光花园");
            value.setHouseNumber("2栋1203");
            return value;
        }

        private OrderCartItemProjection cartItem() {
            OrderCartItemProjection value = new OrderCartItemProjection();
            value.setCartId(5L);
            value.setShopId(SHOP_ID);
            value.setProductId(3L);
            value.setProductName("牛肉饭");
            value.setProductImageUrl("/files/beef.png");
            value.setProductStatus(1);
            value.setProductStock(20);
            value.setProductExists(true);
            value.setCategoryAvailable(true);
            value.setSpecificationId(9L);
            value.setSpecificationName("大份");
            value.setSpecificationValid(true);
            value.setTasteId(11L);
            value.setTasteName("少辣");
            value.setTasteValid(true);
            value.setUnitPrice(new BigDecimal("12.50"));
            value.setQuantity(2);
            return value;
        }

        private Order copy(Order source) {
            if (source == null) return null;
            Order value = new Order();
            value.setId(source.getId());
            value.setOrderNo(source.getOrderNo());
            value.setIdempotencyKey(source.getIdempotencyKey());
            value.setUserId(source.getUserId());
            value.setShopId(source.getShopId());
            value.setOrderType(source.getOrderType());
            value.setStatus(source.getStatus());
            value.setTotalAmount(source.getTotalAmount());
            value.setDeliveryFee(source.getDeliveryFee());
            value.setPackageFee(source.getPackageFee());
            value.setPayAmount(source.getPayAmount());
            value.setContactName(source.getContactName());
            value.setContactPhone(source.getContactPhone());
            value.setTableNo(source.getTableNo());
            value.setNoSeatYet(source.getNoSeatYet());
            value.setAddressId(source.getAddressId());
            value.setAddressArea(source.getAddressArea());
            value.setAddressDetail(source.getAddressDetail());
            value.setAddressHouseNumber(source.getAddressHouseNumber());
            value.setDeliveryRangeSnapshot(source.getDeliveryRangeSnapshot());
            value.setRemark(source.getRemark());
            value.setVersion(source.getVersion());
            value.setCreateTime(source.getCreateTime());
            return value;
        }
    }
}
