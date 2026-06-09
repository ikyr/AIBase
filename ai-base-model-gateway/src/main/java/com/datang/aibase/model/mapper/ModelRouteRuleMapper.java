package com.datang.aibase.model.mapper;

import com.datang.aibase.model.entity.ModelRouteRule;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ModelRouteRuleMapper {

    @Select("SELECT * FROM model_route_rule WHERE status = 'ACTIVE' ORDER BY priority")
    List<ModelRouteRule> selectActive();

    @Select("SELECT * FROM model_route_rule WHERE id = #{id}")
    ModelRouteRule selectById(String id);

    @Insert("INSERT INTO model_route_rule (id, name, model_id, match_expression, priority, " +
            "fallback_model_id, status, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{name}, #{modelId}, #{matchExpression}, #{priority}, " +
            "#{fallbackModelId}, #{status}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(ModelRouteRule entity);
}
