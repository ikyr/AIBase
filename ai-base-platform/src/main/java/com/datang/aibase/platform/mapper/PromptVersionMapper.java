package com.datang.aibase.platform.mapper;

import com.datang.aibase.platform.entity.PromptVersion;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PromptVersionMapper {

    @Select("SELECT * FROM prompt_version WHERE status != 'DELETED' ORDER BY created_at DESC")
    List<PromptVersion> selectAll();

    @Select("SELECT * FROM prompt_version WHERE id = #{id}")
    PromptVersion selectById(String id);

    @Insert("INSERT INTO prompt_version (id, ref_type, ref_id, version, content, changelog, " +
            "is_current, status, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{refType}, #{refId}, #{version}, #{content}, #{changelog}, " +
            "#{isCurrent}, #{status}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(PromptVersion entity);

    @Update("UPDATE prompt_version SET status = #{status}, is_current = #{isCurrent}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int update(@Param("id") String id, @Param("status") String status, @Param("isCurrent") boolean isCurrent);

    @Update("UPDATE prompt_version SET is_current = false, updated_at = NOW() " +
            "WHERE ref_type = #{refType} AND ref_id = #{refId} AND is_current = true")
    int clearCurrent(@Param("refType") String refType, @Param("refId") String refId);

    @Select("SELECT * FROM prompt_version WHERE ref_type = #{refType} AND ref_id = #{refId} " +
            "AND is_current = true AND status != 'DELETED'")
    PromptVersion selectCurrent(@Param("refType") String refType, @Param("refId") String refId);
}
