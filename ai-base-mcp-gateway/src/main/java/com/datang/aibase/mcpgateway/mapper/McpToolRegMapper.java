package com.datang.aibase.mcpgateway.mapper;

import com.datang.aibase.mcpgateway.entity.McpToolReg;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface McpToolRegMapper {

    @Select("SELECT * FROM mcp_tool_reg WHERE server_id = #{serverId} AND status = 'ACTIVE'")
    List<McpToolReg> selectByServerId(String serverId);

    @Select("SELECT * FROM mcp_tool_reg WHERE id = #{id}")
    McpToolReg selectById(String id);

    @Select("SELECT * FROM mcp_tool_reg WHERE status = 'ACTIVE' ORDER BY server_id, tool_name")
    List<McpToolReg> selectAllActive();

    @Insert("INSERT INTO mcp_tool_reg (id, server_id, tool_name, description, input_schema, " +
            "tool_type, source_service, cache_ttl_seconds, status, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{serverId}, #{toolName}, #{description}, #{inputSchema}, " +
            "#{toolType}, #{sourceService}, #{cacheTtlSeconds}, #{status}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(McpToolReg entity);

    @Insert("<script>" +
            "INSERT INTO mcp_tool_reg (id, server_id, tool_name, description, input_schema, " +
            "tool_type, source_service, cache_ttl_seconds, status, created_at, updated_at, created_by, updated_by) VALUES " +
            "<foreach collection='list' item='t' separator=','>" +
            "(#{t.id}, #{t.serverId}, #{t.toolName}, #{t.description}, #{t.inputSchema}, " +
            "#{t.toolType}, #{t.sourceService}, #{t.cacheTtlSeconds}, #{t.status}, #{t.createdAt}, #{t.updatedAt}, #{t.createdBy}, #{t.updatedBy})" +
            "</foreach>" +
            "</script>")
    int batchInsert(List<McpToolReg> tools);

    @Delete("DELETE FROM mcp_tool_reg WHERE server_id = #{serverId}")
    int deleteByServerId(String serverId);

    @Update("UPDATE mcp_tool_reg SET status = 'INACTIVE', updated_at = NOW() WHERE server_id = #{serverId}")
    int deactivateByServerId(String serverId);
}
