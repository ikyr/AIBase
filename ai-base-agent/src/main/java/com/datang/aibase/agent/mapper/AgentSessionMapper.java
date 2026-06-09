package com.datang.aibase.agent.mapper;

import com.datang.aibase.agent.entity.AgentSession;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AgentSessionMapper {

    @Select("SELECT * FROM agent_session ORDER BY started_at DESC LIMIT #{limit}")
    List<AgentSession> selectRecent(@Param("limit") int limit);

    @Select("SELECT * FROM agent_session WHERE agent_id = #{agentId} ORDER BY started_at DESC")
    List<AgentSession> selectByAgentId(String agentId);

    @Select("SELECT * FROM agent_session WHERE id = #{id}")
    AgentSession selectById(String id);

    @Insert("INSERT INTO agent_session (id, agent_id, user_id, title, status, context, trace_id, " +
            "started_at, completed_at, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{agentId}, #{userId}, #{title}, #{status}, #{context}, #{traceId}, " +
            "#{startedAt}, #{completedAt}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(AgentSession entity);

    @Update("UPDATE agent_session SET status = #{status}, context = #{context}, trace_id = #{traceId}, " +
            "completed_at = #{completedAt}, updated_at = #{updatedAt}, updated_by = #{updatedBy} " +
            "WHERE id = #{id}")
    int update(AgentSession entity);
}
