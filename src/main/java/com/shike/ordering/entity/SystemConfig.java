package com.shike.ordering.entity;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.shike.ordering.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter @TableName("system_config")
public class SystemConfig extends BaseEntity {
    private String configKey;
    private String configValue;
    private String description;
    @TableLogic private Integer deleted;
    @Version private Integer version;
}
