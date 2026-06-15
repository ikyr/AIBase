package com.datang.aibase.workflow.mapper;

import com.datang.aibase.workflow.entity.WfTemplate;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface WfTemplateMapper {

    @Select("SELECT * FROM wf_template WHERE status != 'DELETED' ORDER BY updated_at DESC")
    List<WfTemplate> selectAll();

    @Select("SELECT * FROM wf_template WHERE id = #{id}")
    WfTemplate selectById(String id);

    @Select("SELECT * FROM wf_template WHERE category = #{category} AND status = 'PUBLISHED' ORDER BY usage_count DESC")
    List<WfTemplate> selectByCategory(String category);

    @Insert("INSERT INTO wf_template (id, name, description, category, dag, usage_count, status, " +
            "created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{name}, #{description}, #{category}, #{dag}, #{usageCount}, #{status}, " +
            "#{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(WfTemplate entity);

    @Update("UPDATE wf_template SET name = #{name}, description = #{description}, " +
            "category = #{category}, dag = #{dag}, usage_count = #{usageCount}, " +
            "status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int update(WfTemplate entity);

    @Update("UPDATE wf_template SET usage_count = usage_count + 1, updated_at = NOW() WHERE id = #{id}")
    int incrementUsageCount(String id);

    @Update("UPDATE wf_template SET status = 'DELETED', updated_at = NOW() WHERE id = #{id}")
    int softDelete(String id);
}
