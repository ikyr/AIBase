package com.datang.aibase.eval.mapper;

import com.datang.aibase.eval.entity.AnnotationRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AnnotationRecordMapper {

    @Select("SELECT * FROM annotation_record WHERE eval_result_id = #{evalResultId} ORDER BY created_at DESC")
    List<AnnotationRecord> selectByEvalResultId(@Param("evalResultId") String evalResultId);

    @Select("SELECT * FROM annotation_record WHERE id = #{id}")
    AnnotationRecord selectById(String id);

    @Insert("INSERT INTO annotation_record (id, eval_result_id, annotator_id, score, tags, comment, is_golden, " +
            "status, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{evalResultId}, #{annotatorId}, #{score}, #{tags}, #{comment}, #{isGolden}, " +
            "#{status}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(AnnotationRecord entity);

    @Update("UPDATE annotation_record SET score = #{score}, tags = #{tags}, comment = #{comment}, is_golden = #{isGolden} WHERE id = #{id}")
    int update(AnnotationRecord entity);
}
