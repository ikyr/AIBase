package com.datang.aibase.skill.mapper;

import com.datang.aibase.skill.entity.SkillExecutionLog;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SkillExecutionLogMapper {

    @Select("SELECT * FROM skill_execution_log ORDER BY created_at DESC LIMIT #{limit}")
    List<SkillExecutionLog> selectRecent(@Param("limit") int limit);

    @Select("SELECT * FROM skill_execution_log WHERE skill_id = #{skillId} ORDER BY created_at DESC")
    List<SkillExecutionLog> selectBySkillId(String skillId);

    @Insert("INSERT INTO skill_execution_log (id, skill_id, skill_version, session_id, input, output, " +
            "status, duration_ms, error_msg, trace_id, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{skillId}, #{skillVersion}, #{sessionId}, #{input}, #{output}, " +
            "#{status}, #{durationMs}, #{errorMsg}, #{traceId}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(SkillExecutionLog entity);
}
