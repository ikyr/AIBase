package com.datang.aibase.knowledge.mapper;

import com.datang.aibase.knowledge.entity.KbChunk;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface KbChunkMapper {

    @Select("SELECT * FROM kb_chunk WHERE doc_id = #{docId} ORDER BY chunk_index")
    List<KbChunk> selectByDocId(String docId);

    @Select("SELECT * FROM kb_chunk WHERE kb_id = #{kbId} ORDER BY created_at DESC")
    List<KbChunk> selectByKbId(String kbId);

    @Insert("INSERT INTO kb_chunk (id, doc_id, kb_id, chunk_index, content, token_count, vector_id, " +
            "metadata, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{docId}, #{kbId}, #{chunkIndex}, #{content}, #{tokenCount}, #{vectorId}, " +
            "#{metadata}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(KbChunk entity);

    @Insert("<script>" +
            "INSERT INTO kb_chunk (id, doc_id, kb_id, chunk_index, content, token_count, vector_id, " +
            "metadata, created_at, updated_at, created_by, updated_by) VALUES " +
            "<foreach collection='list' item='c' separator=','>" +
            "(#{c.id}, #{c.docId}, #{c.kbId}, #{c.chunkIndex}, #{c.content}, #{c.tokenCount}, #{c.vectorId}, " +
            "#{c.metadata}, #{c.createdAt}, #{c.updatedAt}, #{c.createdBy}, #{c.updatedBy})" +
            "</foreach>" +
            "</script>")
    int batchInsert(List<KbChunk> chunks);

    @Delete("DELETE FROM kb_chunk WHERE doc_id = #{docId}")
    int deleteByDocId(String docId);

    @Select("SELECT COUNT(*) FROM kb_chunk WHERE kb_id = #{kbId}")
    int countByKbId(String kbId);

    @Select("<script>SELECT * FROM kb_chunk WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<KbChunk> selectByIds(@Param("ids") List<String> ids);

    @Select("SELECT * FROM kb_chunk WHERE kb_id = #{kbId} AND content ILIKE CONCAT('%', #{query}, '%') ORDER BY created_at DESC LIMIT #{limit}")
    List<KbChunk> keywordSearch(@Param("kbId") String kbId, @Param("query") String query, @Param("limit") int limit);
}
