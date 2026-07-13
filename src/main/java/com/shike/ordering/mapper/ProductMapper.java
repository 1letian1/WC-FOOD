package com.shike.ordering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shike.ordering.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    IPage<Product> selectUserPage(Page<Product> page, @Param("shopId") Long shopId,
                                  @Param("categoryId") Long categoryId, @Param("keyword") String keyword);

    int deductStock(@Param("productId") Long productId, @Param("shopId") Long shopId,
                    @Param("quantity") Integer quantity);
    int restoreStock(@Param("productId") Long productId, @Param("shopId") Long shopId,
                     @Param("quantity") Integer quantity);
}
