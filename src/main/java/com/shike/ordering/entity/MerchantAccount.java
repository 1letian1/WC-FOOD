package com.shike.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shike.ordering.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("merchant_account")
public class MerchantAccount extends BaseEntity {
    private Long shopId;
    private String username;
    private String passwordHash;
    private String passwordAlgorithm;
    private String merchantName;
    private String avatarUrl;
    private String role;
    private Integer status;
    private Integer sessionVersion;
    private LocalDateTime lastLoginTime;
}
