package com.shike.ordering.service.user;

import com.shike.ordering.common.result.PageResult;
import com.shike.ordering.dto.user.ProductQueryDTO;
import com.shike.ordering.vo.common.ProductDetailVO;
import com.shike.ordering.vo.common.ProductSummaryVO;

public interface ProductQueryService {
    PageResult<ProductSummaryVO> list(ProductQueryDTO query);
    ProductDetailVO detail(Long id);
}
