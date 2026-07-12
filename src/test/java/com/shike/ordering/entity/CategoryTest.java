package com.shike.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {
    @Test
    void deletedField_shouldUseMybatisPlusLogicalDelete() throws NoSuchFieldException {
        assertThat(Category.class.getDeclaredField("deleted").isAnnotationPresent(TableLogic.class)).isTrue();
    }
}
