package com.datang.aibase.knowledge.mapper;

import com.datang.aibase.knowledge.entity.KbDocument;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface KbDocumentMapper {

    @Select("SELECT * FROM kb_document WHERE kb_id = #{kbId} AND status != 'DELETED' ORDER BY created_at DESC")
    List<KbDocument> selectByKbId(String kbId);

    @Select("SELECT * FROM kb_document WHERE id = #{id}")
    KbDocument selectById(String id);

    @Insert("INSERT INTO kb_document (id, kb_id, title, source_type, source_ref, file_type, file_size, " +
            "status, chunk_count, checksum, ingested_at, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{kbId}, #{title}, #{sourceType}, #{sourceRef}, #{fileType}, #{fileSize}, " +
            "#{status}, #{chunkCount}, #{checksum}, #{ingestedAt}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(KbDocument entity);

    @Update("UPDATE kb_document SET status = #{status}, chunk_count = #{chunkCount}, " +
            "checksum = #{checksum}, ingested_at = #{ingestedAt}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateStatus(KbDocument entity);

    @Update("UPDATE kb_document SET status = 'DELETED', updated_at = NOW() WHERE id = #{id}")
    int softDelete(String id);
}
