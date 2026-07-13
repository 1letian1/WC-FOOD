package com.shike.ordering.service.impl.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shike.ordering.auth.model.*;
import com.shike.ordering.common.enums.*;
import com.shike.ordering.common.exception.*;
import com.shike.ordering.common.result.PageResult;
import com.shike.ordering.dto.merchant.MerchantOrderQueryDTO;
import com.shike.ordering.dto.user.OrderQueryDTO;
import com.shike.ordering.entity.*;
import com.shike.ordering.mapper.*;
import com.shike.ordering.mapper.projection.*;
import com.shike.ordering.service.merchant.MerchantOrderService;
import com.shike.ordering.service.user.UserOrderService;
import com.shike.ordering.vo.common.*;
import com.shike.ordering.vo.merchant.DashboardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Slf4j @Service @RequiredArgsConstructor
public class OrderServiceImpl implements UserOrderService, MerchantOrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper statusLogMapper;
    private final ProductMapper productMapper;

    @Override public PageResult<OrderSummaryVO> list(OrderQueryDTO query) {
        Long userId=PrincipalContext.require(PrincipalType.USER).principalId();
        return pageResult(orderMapper.selectUserPage(new Page<>(query.current(),query.size()),userId,query.orderType(),query.status()));
    }
    @Override public PageResult<OrderSummaryVO> list(MerchantOrderQueryDTO query) {
        Long shopId=PrincipalContext.require(PrincipalType.MERCHANT).shopId();
        return pageResult(orderMapper.selectMerchantPage(new Page<>(query.current(),query.size()),shopId,query.orderType(),query.status(),query.keyword()));
    }
    @Override public OrderDetailVO detail(Long id) {
        CurrentPrincipal principal=PrincipalContext.require();
        Order order; String nickname=null;
        if(principal.principalType()==PrincipalType.USER) order=orderMapper.selectUserOrder(id,principal.principalId());
        else { order=orderMapper.selectMerchantOrder(id,principal.shopId()); if(order!=null) nickname=orderMapper.selectUserNickname(order.getUserId()); }
        if(order==null) throw new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND);
        return detailView(order,nickname);
    }
    @Override public DashboardVO dashboard() {
        OrderDashboardProjection value=orderMapper.selectDashboard(PrincipalContext.require(PrincipalType.MERCHANT).shopId());
        return new DashboardVO(value.getTodayOrderCount(),money(value.getTodayTurnover()),value.getPendingAcceptCount(),
                value.getCookingCount(),value.getReadyForPickupCount(),value.getDeliveringCount());
    }
    @Override @Transactional(rollbackFor=Exception.class) public void cancel(Long id) {
        CurrentPrincipal p=PrincipalContext.require(PrincipalType.USER); Order order=requireUserOrder(id,p.principalId());
        transitionUser(order,p.principalId(),OrderStatus.PENDING_ACCEPT,OrderStatus.CANCELLED); restoreStock(order);
    }
    @Override @Transactional(rollbackFor=Exception.class) public void confirmReceipt(Long id) {
        CurrentPrincipal p=PrincipalContext.require(PrincipalType.USER); Order order=requireUserOrder(id,p.principalId());
        requireType(order,OrderType.DELIVERY); transitionUser(order,p.principalId(),OrderStatus.DELIVERED,OrderStatus.COMPLETED);
    }
    @Override @Transactional(rollbackFor=Exception.class) public void accept(Long id) { transitionMerchant(id,OrderStatus.PENDING_ACCEPT,OrderStatus.ACCEPTED,null,null); }
    @Override @Transactional(rollbackFor=Exception.class) public void reject(Long id,String reason) {
        Order order=transitionMerchant(id,OrderStatus.PENDING_ACCEPT,OrderStatus.REJECTED,null,reason); restoreStock(order);
    }
    @Override @Transactional(rollbackFor=Exception.class) public void startCooking(Long id) { transitionMerchant(id,OrderStatus.ACCEPTED,OrderStatus.COOKING,null,null); }
    @Override @Transactional(rollbackFor=Exception.class) public void readyForPickup(Long id) { transitionMerchant(id,OrderStatus.COOKING,OrderStatus.READY_FOR_PICKUP,OrderType.DINE_IN,null); }
    @Override @Transactional(rollbackFor=Exception.class) public void startDelivery(Long id) { transitionMerchant(id,OrderStatus.COOKING,OrderStatus.DELIVERING,OrderType.DELIVERY,null); }
    @Override @Transactional(rollbackFor=Exception.class) public void markDelivered(Long id) { transitionMerchant(id,OrderStatus.DELIVERING,OrderStatus.DELIVERED,OrderType.DELIVERY,null); }
    @Override @Transactional(rollbackFor=Exception.class) public void complete(Long id) { transitionMerchant(id,OrderStatus.READY_FOR_PICKUP,OrderStatus.COMPLETED,OrderType.DINE_IN,null); }

    private Order transitionMerchant(Long id,OrderStatus expected,OrderStatus target,OrderType requiredType,String reason) {
        CurrentPrincipal p=PrincipalContext.require(PrincipalType.MERCHANT);
        Order order=orderMapper.selectMerchantOrder(id,p.shopId());
        if(order==null) throw new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND);
        if(requiredType!=null) requireType(order,requiredType);
        if(orderMapper.transitionMerchantOrder(id,p.shopId(),expected.getCode(),target.getCode(),order.getVersion(),reason)!=1)
            throw new OrderStateConflictException();
        insertLog(order,expected,target,OrderOperatorType.MERCHANT,p.principalId(),reason);
        log.info("merchant changed order status, merchantId={}, orderId={}, fromStatus={}, toStatus={}",p.principalId(),id,expected.getCode(),target.getCode());
        return order;
    }
    private void transitionUser(Order order,Long userId,OrderStatus expected,OrderStatus target) {
        if(orderMapper.transitionUserOrder(order.getId(),userId,expected.getCode(),target.getCode(),order.getVersion())!=1)
            throw new OrderStateConflictException();
        insertLog(order,expected,target,OrderOperatorType.USER,userId,null);
        log.info("user changed order status, userId={}, orderId={}, fromStatus={}, toStatus={}",userId,order.getId(),expected.getCode(),target.getCode());
    }
    private void insertLog(Order order,OrderStatus from,OrderStatus to,OrderOperatorType operator,Long operatorId,String reason) {
        OrderStatusLog value=new OrderStatusLog(); value.setOrderId(order.getId()); value.setFromStatus(from.getCode());
        value.setToStatus(to.getCode()); value.setOperatorType(operator.getCode()); value.setOperatorId(operatorId);
        value.setReason(reason); value.setOrderVersion(order.getVersion()+1); statusLogMapper.insert(value);
    }
    private void restoreStock(Order order) {
        for(OrderItem item:orderItemMapper.selectByOrderId(order.getId()))
            if(productMapper.restoreStock(item.getProductId(),order.getShopId(),item.getQuantity())!=1)
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }
    private Order requireUserOrder(Long id,Long userId) {
        Order value=orderMapper.selectUserOrder(id,userId); if(value==null) throw new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND); return value;
    }
    private void requireType(Order order,OrderType type) { if(order.getOrderType()!=type.getCode()) throw new OrderStateConflictException(); }
    private PageResult<OrderSummaryVO> pageResult(IPage<OrderSummaryProjection> page) {
        List<OrderSummaryVO> records=page.getRecords().stream().map(this::summaryView).toList();
        return new PageResult<>(records,page.getTotal(),page.getCurrent(),page.getSize(),page.getPages());
    }
    private OrderSummaryVO summaryView(OrderSummaryProjection v) {
        return new OrderSummaryVO(v.getId(),v.getOrderNo(),type(v.getOrderType()),status(v.getStatus()),v.getUserNickname(),
                v.getContactName(),v.getTableNo(),v.getAddressSummary(),v.getProductSummary(),v.getProductImageUrl(),
                v.getTotalQuantity(),v.getPayAmount(),v.getCreateTime());
    }
    private OrderDetailVO detailView(Order o,String nickname) {
        List<OrderDetailVO.Item> items=orderItemMapper.selectByOrderId(o.getId()).stream().map(i->new OrderDetailVO.Item(
                i.getId(),i.getProductId(),i.getProductName(),i.getProductImageUrl(),i.getSpecificationName(),i.getTasteName(),i.getUnitPrice(),i.getQuantity(),i.getAmount())).toList();
        List<OrderDetailVO.StatusLog> logs=statusLogMapper.selectByOrderId(o.getId()).stream().map(this::logView).toList();
        return new OrderDetailVO(o.getId(),o.getOrderNo(),type(o.getOrderType()),status(o.getStatus()),nickname,o.getContactName(),
                o.getContactPhone(),o.getTableNo(),o.getNoSeatYet(),o.getAddressId(),o.getAddressArea(),o.getAddressDetail(),
                o.getAddressHouseNumber(),o.getDeliveryRangeSnapshot(),o.getRemark(),o.getRejectReason(),o.getTotalAmount(),
                o.getDeliveryFee(),o.getPackageFee(),o.getPayAmount(),o.getCreateTime(),o.getAcceptTime(),o.getCookingTime(),
                o.getReadyTime(),o.getDeliveryTime(),o.getDeliveredTime(),o.getCancelTime(),o.getFinishTime(),items,logs);
    }
    private OrderDetailVO.StatusLog logView(OrderStatusLog v) {
        OrderOperatorType operator=OrderOperatorType.fromCode(v.getOperatorType());
        return new OrderDetailVO.StatusLog(v.getFromStatus()==null?null:status(v.getFromStatus()),status(v.getToStatus()),
                operator.getCode(),operator.getDescription(),v.getReason(),v.getOrderVersion(),v.getCreateTime());
    }
    private StatusVO type(int code) { OrderType v=OrderType.fromCode(code); return new StatusVO(v.getCode(),v.getDescription()); }
    private StatusVO status(int code) { OrderStatus v=OrderStatus.fromCode(code); return new StatusVO(v.getCode(),v.getDescription()); }
    private BigDecimal money(BigDecimal value) { return value==null?new BigDecimal("0.00"):value; }
}
