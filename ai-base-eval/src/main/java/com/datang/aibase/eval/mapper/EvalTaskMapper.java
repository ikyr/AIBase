package com.datang.aibase.eval.mapper;

import com.datang.aibase.eval.entity.EvalTask;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface EvalTaskMapper {

    @Select("SELECT * FROM eval_task ORDER BY started_at DESC LIMIT #{limit}")
    List<EvalTask> selectRecent(@Param("limit") int limit);

    @Select("SELECT * FROM eval_task WHERE id = #{id}")
    EvalTask selectById(String id);

    @Insert("INSERT INTO eval_task (id, dataset_id, target_id, target_type, status, metrics, " +
            "total_items, passed_items, started_at, completed_at, trace_id, " +
            "created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{datasetId}, #{targetId}, #{targetType}, #{status}, #{metrics}, " +
            "#{totalItems}, #{passedItems}, #{startedAt}, #{completedAt}, #{traceId}, " +
            "#{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(EvalTask entity);

    @Update("UPDATE eval_task SET status = #{status}, metrics = #{metrics}, " +
            "total_items = #{totalItems}, passed_items = #{passedItems}, " +
            "started_at = #{startedAt}, completed_at = #{completedAt}, updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int update(EvalTask entity);

    @Delete("DELETE FROM eval_task WHERE id = #{id}")
    int delete(String id);
}
