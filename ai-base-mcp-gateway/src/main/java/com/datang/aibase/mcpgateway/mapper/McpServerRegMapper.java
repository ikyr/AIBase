package com.datang.aibase.mcpgateway.mapper;

import com.datang.aibase.mcpgateway.entity.McpServerReg;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface McpServerRegMapper {

    @Select("SELECT * FROM mcp_server_reg WHERE status != 'DELETED' ORDER BY created_at DESC")
    List<McpServerReg> selectAll();

    @Select("SELECT * FROM mcp_server_reg WHERE id = #{id}")
    McpServerReg selectById(String id);

    @Select("SELECT * FROM mcp_server_reg WHERE endpoint = #{endpoint} AND status = 'ACTIVE'")
    McpServerReg selectByEndpoint(String endpoint);

    @Insert("INSERT INTO mcp_server_reg (id, name, server_type, transport, endpoint, auth_config, " +
            "tools_count, resources_count, prompts_count, health_status, last_health_check, " +
            "status, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{name}, #{serverType}, #{transport}, #{endpoint}, #{authConfig}, " +
            "#{toolsCount}, #{resourcesCount}, #{promptsCount}, #{healthStatus}, #{lastHealthCheck}, " +
            "#{status}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(McpServerReg entity);

    @Update("UPDATE mcp_server_reg SET health_status = #{healthStatus}, last_health_check = #{lastHealthCheck}, " +
            "tools_count = #{toolsCount}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateHealth(McpServerReg entity);

    @Update("UPDATE mcp_server_reg SET status = 'DELETED', updated_at = NOW() WHERE id = #{id}")
    int softDelete(String id);
}
