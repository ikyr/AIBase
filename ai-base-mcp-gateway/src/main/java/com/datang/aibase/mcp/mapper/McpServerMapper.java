package com.datang.aibase.mcp.mapper;

import com.datang.aibase.mcp.entity.McpServer;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface McpServerMapper {

    @Select("SELECT * FROM mcp_server WHERE status != 'DELETED' ORDER BY created_at DESC")
    List<McpServer> selectAll();

    @Select("SELECT * FROM mcp_server WHERE id = #{id}")
    McpServer selectById(String id);

    @Select("SELECT * FROM mcp_server WHERE status = 'ACTIVE' ORDER BY created_at DESC")
    List<McpServer> selectActive();

    @Insert("INSERT INTO mcp_server (id, name, server_type, transport, endpoint, description, " +
            "health_status, status, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{name}, #{serverType}, #{transport}, #{endpoint}, #{description}, " +
            "#{healthStatus}, #{status}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(McpServer entity);

    @Update("UPDATE mcp_server SET health_status = #{healthStatus}, updated_at = NOW() WHERE id = #{id}")
    int updateHealth(@Param("id") String id, @Param("healthStatus") String healthStatus);

    @Update("UPDATE mcp_server SET status = 'DELETED', updated_at = NOW() WHERE id = #{id}")
    int delete(String id);

    @Update("UPDATE mcp_server SET name = #{name}, server_type = #{serverType}, " +
            "transport = #{transport}, endpoint = #{endpoint}, description = #{description}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int update(McpServer entity);
}
