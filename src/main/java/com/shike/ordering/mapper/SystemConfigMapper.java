package com.shike.ordering.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shike.ordering.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
    List<SystemConfig> selectByKeyword(@Param("keyword") String keyword);
}
