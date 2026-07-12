package com.shike.ordering.converter;

import com.shike.ordering.common.enums.EnabledStatus;
import com.shike.ordering.entity.Category;
import com.shike.ordering.vo.common.StatusVO;
import com.shike.ordering.vo.user.CategoryVO;
import org.springframework.stereotype.Component;

@Component
public class CategoryConverter {
    public CategoryVO toVO(Category category) {
        EnabledStatus status = category.getStatus() != null && category.getStatus() == 1
                ? EnabledStatus.ENABLED : EnabledStatus.DISABLED;
        return new CategoryVO(category.getId(), category.getName(),
                new StatusVO(status.getCode(), status.getDescription()), category.getSort());
    }
}
