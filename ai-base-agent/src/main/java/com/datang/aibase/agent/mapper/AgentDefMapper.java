package com.datang.aibase.agent.mapper;

import com.datang.aibase.agent.entity.AgentDef;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AgentDefMapper {

    @Select("SELECT * FROM agent_def WHERE status != 'DELETED' ORDER BY created_at DESC")
    List<AgentDef> selectAll();

    @Select("SELECT * FROM agent_def WHERE id = #{id}")
    AgentDef selectById(String id);

    @Select("SELECT * FROM agent_def WHERE status = 'ACTIVE' ORDER BY created_at DESC")
    List<AgentDef> selectActive();

    @Insert("INSERT INTO agent_def (id, name, description, system_prompt, model, tools, " +
            "skill_ids, kb_ids, coordination_mode, constraints, " +
            "status, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{name}, #{description}, #{systemPrompt}, #{model}, #{tools}, " +
            "#{skillIds}, #{kbIds}, #{coordinationMode}, #{constraints}, " +
            "#{status}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(AgentDef entity);
}
