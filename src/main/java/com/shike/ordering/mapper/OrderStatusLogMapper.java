package com.shike.ordering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shike.ordering.entity.OrderStatusLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderStatusLogMapper extends BaseMapper<OrderStatusLog> {
    List<OrderStatusLog> selectByOrderId(@Param("orderId") Long orderId);
}
