package com.datang.aibase.skill.mapper;

import com.datang.aibase.skill.entity.SkillDef;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SkillDefMapper {

    @Select("SELECT * FROM skill_def WHERE status != 'DELETED' ORDER BY created_at DESC")
    List<SkillDef> selectAll();

    @Select("SELECT * FROM skill_def WHERE id = #{id}")
    SkillDef selectById(String id);

    @Select("SELECT * FROM skill_def WHERE status = 'ACTIVE' ORDER BY created_at DESC")
    List<SkillDef> selectActive();

    @Select("SELECT * FROM skill_def WHERE (name ILIKE CONCAT('%', #{query}, '%') OR tags ILIKE CONCAT('%', #{query}, '%')) AND status = 'ACTIVE' ORDER BY created_at DESC")
    List<SkillDef> search(@Param("query") String query);

    @Insert("INSERT INTO skill_def (id, name, description, tags, skill_level, prompt_template, " +
            "params, input_schema, output_schema, execution_mode, timeout_ms, agent_ref_id, " +
            "status, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{name}, #{description}, #{tags}, #{skillLevel}, #{promptTemplate}, " +
            "#{params}, #{inputSchema}, #{outputSchema}, #{executionMode}, #{timeoutMs}, #{agentRefId}, " +
            "#{status}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(SkillDef entity);

    @Update("UPDATE skill_def SET name = #{name}, description = #{description}, tags = #{tags}, " +
            "skill_level = #{skillLevel}, prompt_template = #{promptTemplate}, params = #{params}, " +
            "input_schema = #{inputSchema}, output_schema = #{outputSchema}, " +
            "execution_mode = #{executionMode}, timeout_ms = #{timeoutMs}, agent_ref_id = #{agentRefId}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int update(SkillDef entity);

    @Update("UPDATE skill_def SET status = 'DELETED', updated_at = NOW() WHERE id = #{id}")
    int softDelete(String id);
}
