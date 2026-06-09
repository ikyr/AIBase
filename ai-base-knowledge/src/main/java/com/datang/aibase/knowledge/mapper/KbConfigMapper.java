package com.datang.aibase.knowledge.mapper;

import com.datang.aibase.knowledge.entity.KbConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface KbConfigMapper {

    @Select("SELECT * FROM kb_config WHERE status = #{status}")
    List<KbConfig> selectByStatus(String status);

    @Select("SELECT * FROM kb_config WHERE id = #{id}")
    KbConfig selectById(String id);

    @Insert("INSERT INTO kb_config (id, name, description, kb_type, owner_id, owner_dept_id, " +
            "embedding_model, chunk_size, chunk_overlap, status, metadata, " +
            "created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{name}, #{description}, #{kbType}, #{ownerId}, #{ownerDeptId}, " +
            "#{embeddingModel}, #{chunkSize}, #{chunkOverlap}, #{status}, #{metadata}, " +
            "#{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(KbConfig entity);
}
