package com.datang.aibase.workflow.mapper;

import com.datang.aibase.workflow.entity.WfNodeExec;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WfNodeExecMapper {

    @Insert("INSERT INTO wf_node_exec (id, wf_exec_id, node_id, node_name, node_type, status, input, output, " +
            "error, started_at, finished_at, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{wfExecId}, #{nodeId}, #{nodeName}, #{nodeType}, #{status}, #{input}, #{output}, " +
            "#{error}, #{startedAt}, #{finishedAt}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(WfNodeExec entity);

    @Update("UPDATE wf_node_exec SET status = #{status}, output = #{output}, error = #{error}, " +
            "finished_at = #{finishedAt}, updated_at = #{updatedAt}, updated_by = #{updatedBy} " +
            "WHERE id = #{id}")
    int update(WfNodeExec entity);

    @Select("SELECT * FROM wf_node_exec WHERE wf_exec_id = #{wfExecId} ORDER BY started_at")
    List<WfNodeExec> selectByWfExecId(String wfExecId);

    @Select("SELECT * FROM wf_node_exec WHERE id = #{id}")
    WfNodeExec selectById(String id);
}
