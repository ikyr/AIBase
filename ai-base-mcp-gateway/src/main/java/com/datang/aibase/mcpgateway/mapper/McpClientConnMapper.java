package com.datang.aibase.mcpgateway.mapper;

import com.datang.aibase.mcpgateway.entity.McpClientConn;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface McpClientConnMapper {

    @Select("SELECT * FROM mcp_client_conn WHERE server_id = #{serverId} ORDER BY created_at DESC")
    List<McpClientConn> selectByServerId(String serverId);

    @Select("SELECT * FROM mcp_client_conn WHERE server_id = #{serverId} AND status = 'CONNECTED'")
    McpClientConn selectActiveByServerId(String serverId);

    @Insert("INSERT INTO mcp_client_conn (id, server_id, status, session_token, connected_at, " +
            "disconnected_at, error_count, last_error, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{serverId}, #{status}, #{sessionToken}, #{connectedAt}, " +
            "#{disconnectedAt}, #{errorCount}, #{lastError}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(McpClientConn entity);

    @Update("UPDATE mcp_client_conn SET status = #{status}, session_token = #{sessionToken}, " +
            "connected_at = #{connectedAt}, disconnected_at = #{disconnectedAt}, error_count = #{errorCount}, " +
            "last_error = #{lastError}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(McpClientConn entity);
}
