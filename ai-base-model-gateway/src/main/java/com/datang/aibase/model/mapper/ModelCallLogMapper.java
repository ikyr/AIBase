package com.datang.aibase.model.mapper;

import com.datang.aibase.model.entity.ModelCallLog;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ModelCallLogMapper {

    @Select("SELECT * FROM model_call_log ORDER BY created_at DESC LIMIT #{limit}")
    List<ModelCallLog> selectRecent(@Param("limit") int limit);

    @Select("SELECT * FROM model_call_log WHERE caller_ref = #{callerRef} ORDER BY created_at DESC")
    List<ModelCallLog> selectByCaller(String callerRef);

    @Insert("INSERT INTO model_call_log (id, model_name, caller_ref, input_tokens, output_tokens, " +
            "duration_ms, cost, call_status, error_msg, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{modelName}, #{callerRef}, #{inputTokens}, #{outputTokens}, " +
            "#{durationMs}, #{cost}, #{callStatus}, #{errorMsg}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(ModelCallLog entity);
}
