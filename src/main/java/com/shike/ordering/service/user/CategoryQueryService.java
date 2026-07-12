package com.shike.ordering.service.user;

import com.shike.ordering.dto.user.CategoryQueryDTO;
import com.shike.ordering.vo.user.CategoryVO;
import jakarta.validation.Valid;

import java.util.List;

public interface CategoryQueryService {
    List<CategoryVO> listEnabledCategories(@Valid CategoryQueryDTO query);
}
