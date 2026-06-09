package com.datang.aibase.agent.mapper;

import com.datang.aibase.agent.entity.AgentMessage;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AgentMessageMapper {

    @Select("SELECT * FROM agent_message WHERE session_id = #{sessionId} ORDER BY created_at ASC")
    List<AgentMessage> selectBySessionId(String sessionId);

    @Insert("INSERT INTO agent_message (id, session_id, parent_id, role, content, content_type, " +
            "attachments, tool_calls, token_count, metadata, " +
            "created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{sessionId}, #{parentId}, #{role}, #{content}, #{contentType}, " +
            "#{attachments}, #{toolCalls}, #{tokenCount}, #{metadata}, " +
            "#{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(AgentMessage entity);
}
