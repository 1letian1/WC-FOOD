package com.shike.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shike.ordering.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("address")
public class Address extends BaseEntity {
    private Long userId;
    private String contactName;
    private Integer gender;
    private String phone;
    private String area;
    private String detail;
    private String houseNumber;
    private String tag;
    private Boolean isDefault;
    @TableLogic
    private Integer deleted;
}
