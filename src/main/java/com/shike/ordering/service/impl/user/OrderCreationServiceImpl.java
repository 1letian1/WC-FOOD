package com.shike.ordering.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shike.ordering.auth.model.PrincipalContext;
import com.shike.ordering.auth.model.PrincipalType;
import com.shike.ordering.common.enums.OrderOperatorType;
import com.shike.ordering.common.enums.OrderStatus;
import com.shike.ordering.common.enums.OrderType;
import com.shike.ordering.common.exception.BusinessException;
import com.shike.ordering.common.exception.ErrorCode;
import com.shike.ordering.common.exception.ResourceNotFoundException;
import com.shike.ordering.config.ShopProperties;
import com.shike.ordering.dto.user.OrderCreateDTO;
import com.shike.ordering.entity.*;
import com.shike.ordering.mapper.*;
import com.shike.ordering.mapper.projection.OrderCartItemProjection;
import com.shike.ordering.service.user.OrderCreationService;
import com.shike.ordering.service.user.OrderIdempotencyService;
import com.shike.ordering.service.user.OrderNumberService;
import com.shike.ordering.vo.common.StatusVO;
import com.shike.ordering.vo.user.OrderCreateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCreationServiceImpl implements OrderCreationService {
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper statusLogMapper;
    private final ShoppingCartMapper cartMapper;
    private final ProductMapper productMapper;
    private final ShopMapper shopMapper;
    private final AddressMapper addressMapper;
    private final UserMapper userMapper;
    private final OrderNumberService orderNumberService;
    private final OrderIdempotencyService idempotencyService;
    private final ShopProperties shopProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO create(OrderCreateDTO request, String idempotencyKey) {
        Long userId = userId();
        Order existing = findExisting(userId, idempotencyKey);
        if (existing != null) return toView(existing);
        if (!idempotencyService.claim(userId, idempotencyKey)) {
            existing = findExisting(userId, idempotencyKey);
            if (existing != null) return toView(existing);
            throw new BusinessException(ErrorCode.DUPLICATE_OPERATION);
        }
        AtomicReference<String> createdOrderNo = new AtomicReference<>();
        idempotencyService.registerCompletion(userId, idempotencyKey, createdOrderNo::get);

        userMapper.selectIdForUpdate(userId);
        Shop shop = requireShop();
        OrderType orderType = OrderType.fromCode(request.orderType());
        validateFulfillmentAvailability(shop, orderType);
        List<OrderCartItemProjection> items = cartMapper.selectOrderItems(userId, request.cartItemIds());
        if (items.size() != request.cartItemIds().size()) {
            throw new ResourceNotFoundException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        items.forEach(this::validateItem);

        BigDecimal totalAmount = items.stream().map(this::itemAmount)
                .reduce(ZERO, BigDecimal::add).setScale(2, RoundingMode.UNNECESSARY);
        Address address = orderType == OrderType.DELIVERY ? requireAddress(request.addressId(), userId) : null;
        if (orderType == OrderType.DELIVERY && totalAmount.compareTo(shop.getMinDeliveryAmount()) < 0) {
            throw new BusinessException(ErrorCode.MIN_DELIVERY_AMOUNT_NOT_REACHED);
        }

        String orderNo = orderNumberService.nextOrderNo();
        createdOrderNo.set(orderNo);
        Order order = buildOrder(request, userId, shop, orderType, address, totalAmount, orderNo, idempotencyKey);
        for (OrderCartItemProjection item : items) {
            if (productMapper.deductStock(item.getProductId(), item.getShopId(), item.getQuantity()) != 1) {
                throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT);
            }
        }
        orderMapper.insert(order);
        for (OrderCartItemProjection item : items) {
            orderItemMapper.insert(buildOrderItem(order.getId(), item));
        }
        statusLogMapper.insert(buildInitialLog(order.getId(), userId));
        cartMapper.delete(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, userId)
                .in(ShoppingCart::getId, request.cartItemIds()));
        log.info("user created order, userId={}, orderId={}, orderNo={}", userId, order.getId(), orderNo);
        return toView(order);
    }

    private Shop requireShop() {
        Shop shop = shopMapper.selectByIdForUpdate(shopProperties.defaultShopId());
        if (shop == null) throw new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND);
        return shop;
    }

    private void validateFulfillmentAvailability(Shop shop, OrderType orderType) {
        if (shop.getBusinessStatus() != 1) throw new BusinessException(ErrorCode.SHOP_CLOSED);
        if (orderType == OrderType.DINE_IN && !shop.getDineInEnabled()) {
            throw new BusinessException(ErrorCode.DINE_IN_DISABLED);
        }
        if (orderType == OrderType.DELIVERY && !shop.getDeliveryEnabled()) {
            throw new BusinessException(ErrorCode.DELIVERY_DISABLED);
        }
    }

    private void validateItem(OrderCartItemProjection item) {
        if (!item.getProductExists() || !shopProperties.defaultShopId().equals(item.getShopId())) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (item.getProductStatus() == 0 || !item.getCategoryAvailable()) {
            throw new BusinessException(ErrorCode.PRODUCT_OFF_SALE);
        }
        if (item.getProductStatus() == 2) throw new BusinessException(ErrorCode.PRODUCT_SOLD_OUT);
        if (!item.getSpecificationValid()) throw new BusinessException(ErrorCode.PRODUCT_SPECIFICATION_INVALID);
        if (!item.getTasteValid()) throw new BusinessException(ErrorCode.PRODUCT_TASTE_INVALID);
        if (item.getProductStock() < item.getQuantity()) {
            throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT);
        }
    }

    private Address requireAddress(Long addressId, Long userId) {
        Address address = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getId, addressId).eq(Address::getUserId, userId));
        if (address == null) throw new ResourceNotFoundException(ErrorCode.ADDRESS_NOT_FOUND);
        return address;
    }

    private Order buildOrder(OrderCreateDTO request, Long userId, Shop shop, OrderType orderType,
                             Address address, BigDecimal totalAmount, String orderNo, String idempotencyKey) {
        BigDecimal deliveryFee = orderType == OrderType.DELIVERY ? money(shop.getDeliveryFee()) : ZERO;
        BigDecimal packageFee = money(shop.getPackageFee());
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setIdempotencyKey(idempotencyKey);
        order.setUserId(userId);
        order.setShopId(shop.getId());
        order.setOrderType(orderType.getCode());
        order.setStatus(OrderStatus.PENDING_ACCEPT.getCode());
        order.setTotalAmount(totalAmount);
        order.setDeliveryFee(deliveryFee);
        order.setPackageFee(packageFee);
        order.setPayAmount(totalAmount.add(deliveryFee).add(packageFee).setScale(2, RoundingMode.UNNECESSARY));
        order.setContactName(address == null ? request.contactName() : address.getContactName());
        order.setContactPhone(address == null ? request.contactPhone() : address.getPhone());
        order.setTableNo(orderType == OrderType.DINE_IN ? request.tableNo() : null);
        order.setNoSeatYet(orderType == OrderType.DINE_IN && request.noSeatYet());
        order.setAddressId(address == null ? null : address.getId());
        order.setAddressArea(address == null ? null : address.getArea());
        order.setAddressDetail(address == null ? null : address.getDetail());
        order.setAddressHouseNumber(address == null ? null : address.getHouseNumber());
        order.setDeliveryRangeSnapshot(address == null ? null : shop.getDeliveryRange());
        order.setRemark(request.remark());
        order.setVersion(0);
        return order;
    }

    private OrderItem buildOrderItem(Long orderId, OrderCartItemProjection source) {
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(source.getProductId());
        item.setProductName(source.getProductName());
        item.setProductImageUrl(source.getProductImageUrl());
        item.setSpecificationId(source.getSpecificationId());
        item.setSpecificationName(source.getSpecificationName());
        item.setTasteId(source.getTasteId());
        item.setTasteName(source.getTasteName());
        item.setUnitPrice(money(source.getUnitPrice()));
        item.setQuantity(source.getQuantity());
        item.setAmount(itemAmount(source));
        return item;
    }

    private OrderStatusLog buildInitialLog(Long orderId, Long userId) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setFromStatus(null);
        log.setToStatus(OrderStatus.PENDING_ACCEPT.getCode());
        log.setOperatorType(OrderOperatorType.USER.getCode());
        log.setOperatorId(userId);
        log.setOrderVersion(0);
        return log;
    }

    private BigDecimal itemAmount(OrderCartItemProjection item) {
        return money(item.getUnitPrice()).multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.UNNECESSARY);
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    private Order findExisting(Long userId, String idempotencyKey) {
        return orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId)
                .eq(Order::getIdempotencyKey, idempotencyKey));
    }

    private OrderCreateVO toView(Order order) {
        OrderType type = OrderType.fromCode(order.getOrderType());
        OrderStatus status = OrderStatus.fromCode(order.getStatus());
        return new OrderCreateVO(order.getId(), order.getOrderNo(),
                new StatusVO(type.getCode(), type.getDescription()),
                new StatusVO(status.getCode(), status.getDescription()), order.getTotalAmount(),
                order.getDeliveryFee(), order.getPackageFee(), order.getPayAmount(), order.getCreateTime());
    }

    private Long userId() { return PrincipalContext.require(PrincipalType.USER).principalId(); }
}
