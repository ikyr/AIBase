package com.datang.aibase.model.mapper;

import com.datang.aibase.model.entity.ModelConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ModelConfigMapper {

    @Select("SELECT * FROM model_config WHERE status != 'DELETED' ORDER BY priority")
    List<ModelConfig> selectAll();

    @Select("SELECT * FROM model_config WHERE id = #{id}")
    ModelConfig selectById(String id);

    @Select("SELECT * FROM model_config WHERE status = 'ACTIVE' ORDER BY priority")
    List<ModelConfig> selectActive();

    @Insert("INSERT INTO model_config (id, name, provider, endpoint, api_key_ref, max_tokens, " +
            "capabilities, priority, status, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{name}, #{provider}, #{endpoint}, #{apiKeyRef}, #{maxTokens}, " +
            "#{capabilities}, #{priority}, #{status}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(ModelConfig entity);

    @Update("UPDATE model_config SET name = #{name}, provider = #{provider}, endpoint = #{endpoint}, " +
            "api_key_ref = #{apiKeyRef}, max_tokens = #{maxTokens}, capabilities = #{capabilities}, " +
            "priority = #{priority}, updated_at = NOW() WHERE id = #{id}")
    int update(ModelConfig entity);

    @Update("UPDATE model_config SET status = 'DELETED', updated_at = NOW() WHERE id = #{id}")
    int softDelete(String id);
}
