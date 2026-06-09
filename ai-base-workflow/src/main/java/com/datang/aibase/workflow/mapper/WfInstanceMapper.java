package com.datang.aibase.workflow.mapper;

import com.datang.aibase.workflow.entity.WfInstance;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface WfInstanceMapper {

    @Select("SELECT * FROM wf_instance ORDER BY started_at DESC LIMIT #{limit}")
    List<WfInstance> selectRecent(@Param("limit") int limit);

    @Select("SELECT * FROM wf_instance WHERE definition_id = #{definitionId} ORDER BY started_at DESC")
    List<WfInstance> selectByDefinitionId(String definitionId);

    @Update("UPDATE wf_instance SET status = #{status}, output = #{output}, context = #{context}, " +
            "error_msg = #{errorMsg}, trace_id = #{traceId}, completed_at = #{completedAt}, " +
            "updated_at = #{updatedAt}, updated_by = #{updatedBy} " +
            "WHERE id = #{id}")
    int update(WfInstance entity);

    @Select("SELECT * FROM wf_instance WHERE id = #{id}")
    WfInstance selectById(String id);

    @Insert("INSERT INTO wf_instance (id, definition_id, definition_version, status, input, output, " +
            "context, started_at, completed_at, error_msg, trace_id, " +
            "created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{definitionId}, #{definitionVersion}, #{status}, #{input}, #{output}, " +
            "#{context}, #{startedAt}, #{completedAt}, #{errorMsg}, #{traceId}, " +
            "#{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(WfInstance entity);
}
