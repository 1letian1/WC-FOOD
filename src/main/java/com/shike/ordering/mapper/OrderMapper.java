package com.shike.ordering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shike.ordering.entity.Order;
import com.shike.ordering.mapper.projection.OrderDashboardProjection;
import com.shike.ordering.mapper.projection.OrderSummaryProjection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    IPage<OrderSummaryProjection> selectUserPage(Page<OrderSummaryProjection> page, @Param("userId") Long userId,
            @Param("orderType") Integer orderType, @Param("status") Integer status);
    IPage<OrderSummaryProjection> selectMerchantPage(Page<OrderSummaryProjection> page, @Param("shopId") Long shopId,
            @Param("orderType") Integer orderType, @Param("status") Integer status, @Param("keyword") String keyword);
    Order selectUserOrder(@Param("id") Long id, @Param("userId") Long userId);
    Order selectMerchantOrder(@Param("id") Long id, @Param("shopId") Long shopId);
    String selectUserNickname(@Param("userId") Long userId);
    OrderDashboardProjection selectDashboard(@Param("shopId") Long shopId);
    int transitionUserOrder(@Param("id") Long id, @Param("userId") Long userId,
            @Param("expectedStatus") Integer expectedStatus, @Param("targetStatus") Integer targetStatus,
            @Param("version") Integer version);
    int transitionMerchantOrder(@Param("id") Long id, @Param("shopId") Long shopId,
            @Param("expectedStatus") Integer expectedStatus, @Param("targetStatus") Integer targetStatus,
            @Param("version") Integer version, @Param("rejectReason") String rejectReason);
}
