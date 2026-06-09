package com.datang.aibase.mcp.mapper;

import com.datang.aibase.mcp.entity.McpTool;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface McpToolMapper {

    @Select("SELECT * FROM mcp_tool WHERE status != 'DELETED' ORDER BY created_at DESC")
    List<McpTool> selectAll();

    @Select("SELECT * FROM mcp_tool WHERE server_id = #{serverId} AND status != 'DELETED'")
    List<McpTool> selectByServerId(String serverId);

    @Select("SELECT * FROM mcp_tool WHERE id = #{id}")
    McpTool selectById(String id);

    @Insert("INSERT INTO mcp_tool (id, server_id, name, description, input_schema, " +
            "status, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{serverId}, #{name}, #{description}, #{inputSchema}, " +
            "#{status}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(McpTool entity);
}
