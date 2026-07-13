package com.shike.ordering.service.merchant;

import com.shike.ordering.common.result.PageResult;
import com.shike.ordering.dto.merchant.ProductQueryDTO;
import com.shike.ordering.dto.merchant.ProductSaveDTO;
import com.shike.ordering.vo.common.ProductDetailVO;
import com.shike.ordering.vo.common.ProductSummaryVO;

public interface MerchantProductService {
    PageResult<ProductSummaryVO> list(ProductQueryDTO query);
    ProductDetailVO detail(Long id);
    ProductDetailVO create(ProductSaveDTO request);
    ProductDetailVO update(Long id, ProductSaveDTO request);
    ProductDetailVO onSale(Long id);
    ProductDetailVO offSale(Long id);
    ProductDetailVO soldOut(Long id);
    void delete(Long id);
}
