package com.shike.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shike.ordering.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("order_status_log")
public class OrderStatusLog extends BaseEntity {
    private Long orderId;
    private Integer fromStatus;
    private Integer toStatus;
    private String operatorType;
    private Long operatorId;
    private String reason;
    private Integer orderVersion;
}
