package com.shike.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shike.ordering.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("category")
public class Category extends BaseEntity {
    private Long shopId;
    private String name;
    private Integer status;
    private Integer sort;
    @TableLogic
    private Integer deleted;
}
