package com.shike.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shike.ordering.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("user")
public class User extends BaseEntity {
    private String openid;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private Integer status;
    private LocalDateTime lastLoginTime;
}
