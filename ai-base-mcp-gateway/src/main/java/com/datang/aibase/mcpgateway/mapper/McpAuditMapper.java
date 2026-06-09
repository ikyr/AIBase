package com.datang.aibase.mcpgateway.mapper;

import com.datang.aibase.mcpgateway.entity.McpAudit;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface McpAuditMapper {

    @Select("SELECT * FROM mcp_audit WHERE server_id = #{serverId} ORDER BY created_at DESC LIMIT #{limit}")
    List<McpAudit> selectByServerId(@Param("serverId") String serverId, @Param("limit") int limit);

    @Select("SELECT * FROM mcp_audit WHERE tool_name = #{toolName} ORDER BY created_at DESC LIMIT #{limit}")
    List<McpAudit> selectByToolName(@Param("toolName") String toolName, @Param("limit") int limit);

    @Select("SELECT * FROM mcp_audit ORDER BY created_at DESC LIMIT #{limit}")
    List<McpAudit> selectRecent(@Param("limit") int limit);

    @Insert("INSERT INTO mcp_audit (id, server_id, tool_name, session_id, caller, input, output, " +
            "status, duration_ms, error_msg, trace_id, created_at) " +
            "VALUES (#{id}, #{serverId}, #{toolName}, #{sessionId}, #{caller}, #{input}, #{output}, " +
            "#{status}, #{durationMs}, #{errorMsg}, #{traceId}, #{createdAt})")
    int insert(McpAudit entity);
}
