package com.shike.ordering.service.impl.common;

import com.shike.ordering.auth.model.*;
import com.shike.ordering.common.exception.*;
import com.shike.ordering.entity.*;
import com.shike.ordering.mapper.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {
    private final OrderMapper orderMapper=mock(OrderMapper.class);
    private final OrderItemMapper itemMapper=mock(OrderItemMapper.class);
    private final OrderStatusLogMapper logMapper=mock(OrderStatusLogMapper.class);
    private final ProductMapper productMapper=mock(ProductMapper.class);
    private final OrderServiceImpl service=new OrderServiceImpl(orderMapper,itemMapper,logMapper,productMapper);

    @AfterEach void clear(){PrincipalContext.clear();}

    @Test void cancel_whenPending_shouldTransitionLogAndRestoreStockOnce(){
        PrincipalContext.set(new CurrentPrincipal(12L,PrincipalType.USER,null,"token"));
        Order order=order(1,1); when(orderMapper.selectUserOrder(8L,12L)).thenReturn(order);
        when(orderMapper.transitionUserOrder(8L,12L,1,8,0)).thenReturn(1);
        OrderItem item=item(); when(itemMapper.selectByOrderId(8L)).thenReturn(List.of(item));
        when(productMapper.restoreStock(3L,1L,2)).thenReturn(1);
        service.cancel(8L);
        verify(productMapper).restoreStock(3L,1L,2);
        ArgumentCaptor<OrderStatusLog> log=ArgumentCaptor.forClass(OrderStatusLog.class); verify(logMapper).insert(log.capture());
        assertThat(log.getValue().getFromStatus()).isEqualTo(1); assertThat(log.getValue().getToStatus()).isEqualTo(8);
        assertThat(log.getValue().getOrderVersion()).isEqualTo(1);
    }

    @Test void cancel_whenConditionalUpdateLosesRace_shouldNotLogOrRestore(){
        PrincipalContext.set(new CurrentPrincipal(12L,PrincipalType.USER,null,"token"));
        when(orderMapper.selectUserOrder(8L,12L)).thenReturn(order(1,1));
        when(orderMapper.transitionUserOrder(8L,12L,1,8,0)).thenReturn(0);
        assertThatThrownBy(()->service.cancel(8L)).isInstanceOf(OrderStateConflictException.class);
        verifyNoInteractions(logMapper,productMapper); verify(itemMapper,never()).selectByOrderId(anyLong());
    }

    @Test void confirmReceipt_whenDineIn_shouldRejectIllegalType(){
        PrincipalContext.set(new CurrentPrincipal(12L,PrincipalType.USER,null,"token"));
        when(orderMapper.selectUserOrder(8L,12L)).thenReturn(order(1,6));
        assertThatThrownBy(()->service.confirmReceipt(8L)).isInstanceOf(OrderStateConflictException.class);
        verify(orderMapper,never()).transitionUserOrder(anyLong(),anyLong(),anyInt(),anyInt(),anyInt());
    }

    @Test void reject_whenPending_shouldPersistReasonAndRestoreStock(){
        PrincipalContext.set(new CurrentPrincipal(5L,PrincipalType.MERCHANT,1L,"token"));
        Order order=order(2,1); when(orderMapper.selectMerchantOrder(8L,1L)).thenReturn(order);
        when(orderMapper.transitionMerchantOrder(8L,1L,1,9,0,"店铺繁忙")).thenReturn(1);
        when(itemMapper.selectByOrderId(8L)).thenReturn(List.of(item())); when(productMapper.restoreStock(3L,1L,2)).thenReturn(1);
        service.reject(8L,"店铺繁忙");
        verify(productMapper).restoreStock(3L,1L,2);
        ArgumentCaptor<OrderStatusLog> log=ArgumentCaptor.forClass(OrderStatusLog.class); verify(logMapper).insert(log.capture());
        assertThat(log.getValue().getReason()).isEqualTo("店铺繁忙"); assertThat(log.getValue().getOperatorType()).isEqualTo("MERCHANT");
    }

    @Test void startDelivery_whenDineIn_shouldRejectIllegalType(){
        PrincipalContext.set(new CurrentPrincipal(5L,PrincipalType.MERCHANT,1L,"token"));
        when(orderMapper.selectMerchantOrder(8L,1L)).thenReturn(order(1,3));
        assertThatThrownBy(()->service.startDelivery(8L)).isInstanceOf(OrderStateConflictException.class);
        verify(orderMapper,never()).transitionMerchantOrder(anyLong(),anyLong(),anyInt(),anyInt(),anyInt(),any());
    }

    @Test void detail_whenOrderDoesNotBelongToCurrentUser_shouldReturnNotFound(){
        PrincipalContext.set(new CurrentPrincipal(12L,PrincipalType.USER,null,"token"));
        when(orderMapper.selectUserOrder(99L,12L)).thenReturn(null);
        assertThatThrownBy(()->service.detail(99L)).isInstanceOf(ResourceNotFoundException.class).extracting("code").isEqualTo(60001);
    }

    private Order order(int type,int status){Order o=new Order();o.setId(8L);o.setShopId(1L);o.setUserId(12L);o.setOrderType(type);o.setStatus(status);o.setVersion(0);return o;}
    private OrderItem item(){OrderItem i=new OrderItem();i.setProductId(3L);i.setQuantity(2);return i;}
}
