package com.shike.ordering.service.impl.user;

import com.shike.ordering.converter.CategoryConverter;
import com.shike.ordering.dto.user.CategoryQueryDTO;
import com.shike.ordering.mapper.CategoryMapper;
import com.shike.ordering.service.user.CategoryQueryService;
import com.shike.ordering.vo.user.CategoryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class CategoryQueryServiceImpl implements CategoryQueryService {
    private final CategoryMapper categoryMapper;
    private final CategoryConverter categoryConverter;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryVO> listEnabledCategories(@Valid CategoryQueryDTO query) {
        return categoryMapper.selectEnabledByShopId(query.shopId()).stream()
                .map(categoryConverter::toVO)
                .toList();
    }
}
