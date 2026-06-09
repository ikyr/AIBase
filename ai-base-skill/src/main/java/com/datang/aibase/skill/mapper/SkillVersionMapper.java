package com.datang.aibase.skill.mapper;

import com.datang.aibase.skill.entity.SkillVersion;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SkillVersionMapper {

    @Select("SELECT * FROM skill_version WHERE skill_id = #{skillId} AND status != 'DELETED' ORDER BY created_at DESC")
    List<SkillVersion> selectBySkillId(String skillId);

    @Select("SELECT * FROM skill_version WHERE id = #{id}")
    SkillVersion selectById(String id);

    @Select("SELECT * FROM skill_version WHERE skill_id = #{skillId} AND is_latest = true")
    SkillVersion selectLatest(String skillId);

    @Insert("INSERT INTO skill_version (id, skill_id, version, changelog, definition, is_latest, " +
            "status, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{skillId}, #{version}, #{changelog}, #{definition}, #{isLatest}, " +
            "#{status}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(SkillVersion entity);

    @Update("UPDATE skill_version SET status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(SkillVersion entity);

    @Update("UPDATE skill_version SET is_latest = false WHERE skill_id = #{skillId} AND is_latest = true")
    int clearLatest(String skillId);

    @Update("UPDATE skill_version SET is_latest = true, status = 'PUBLISHED', updated_at = NOW() WHERE id = #{id}")
    int promote(String id);

    @Update("UPDATE skill_version SET status = 'ROLLED_BACK', updated_at = NOW() WHERE id = #{id}")
    int rollback(String id);

    default int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int maxLen = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLen; i++) {
            int n1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int n2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (n1 != n2) return n1 - n2;
        }
        return 0;
    }
}
