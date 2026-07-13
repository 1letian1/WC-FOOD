package com.shike.ordering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shike.ordering.entity.ShoppingCart;
import com.shike.ordering.vo.user.CartItemVO;
import com.shike.ordering.mapper.projection.OrderCartItemProjection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
    List<CartItemVO> selectUserCart(@Param("userId") Long userId);

    List<OrderCartItemProjection> selectOrderItems(@Param("userId") Long userId,
                                                   @Param("cartItemIds") List<Long> cartItemIds);
}
