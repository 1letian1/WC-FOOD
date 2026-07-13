package com.shike.ordering.service.merchant;

import com.shike.ordering.dto.merchant.CategorySaveDTO;
import com.shike.ordering.vo.merchant.CategoryVO;
import java.util.List;

public interface MerchantCategoryService {
    List<CategoryVO> list();
    CategoryVO create(CategorySaveDTO request);
    CategoryVO update(Long id, CategorySaveDTO request);
    CategoryVO updateStatus(Long id, Integer status);
    void delete(Long id);
}
