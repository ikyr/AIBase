package com.datang.aibase.workflow.mapper;

import com.datang.aibase.workflow.entity.WfDefinition;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface WfDefinitionMapper {

    @Select("SELECT * FROM wf_definition WHERE status != 'DELETED' ORDER BY updated_at DESC")
    List<WfDefinition> selectAll();

    @Select("SELECT * FROM wf_definition WHERE id = #{id}")
    WfDefinition selectById(String id);

    @Select("SELECT * FROM wf_definition WHERE status = 'ACTIVE' ORDER BY updated_at DESC")
    List<WfDefinition> selectActive();

    @Insert("INSERT INTO wf_definition (id, name, description, version, dag, timeout_seconds, " +
            "retry_policy, status, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{name}, #{description}, #{version}, #{dag}, #{timeoutSeconds}, " +
            "#{retryPolicy}, #{status}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(WfDefinition entity);

    @Update("UPDATE wf_definition SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("status") String status);

    @Update("UPDATE wf_definition SET name = #{name}, description = #{description}, " +
            "dag = #{dag}, timeout_seconds = #{timeoutSeconds}, retry_policy = #{retryPolicy}, " +
            "version = #{version}, updated_at = NOW() WHERE id = #{id}")
    int update(WfDefinition entity);

    @Update("UPDATE wf_definition SET status = 'DELETED', updated_at = NOW() WHERE id = #{id}")
    int softDelete(String id);
}
