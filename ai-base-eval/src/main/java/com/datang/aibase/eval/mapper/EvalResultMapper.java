package com.datang.aibase.eval.mapper;

import com.datang.aibase.eval.entity.EvalResult;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface EvalResultMapper {

    @Select("SELECT * FROM eval_result WHERE task_id = #{taskId} ORDER BY created_at DESC")
    List<EvalResult> selectByTaskId(String taskId);

    @Select("SELECT * FROM eval_result WHERE id = #{id}")
    EvalResult selectById(String id);

    @Insert("INSERT INTO eval_result (id, task_id, item_id, actual_output, metrics, passed, " +
            "error_msg, duration_ms, created_at) " +
            "VALUES (#{id}, #{taskId}, #{itemId}, #{actualOutput}, #{metrics}, #{passed}, " +
            "#{errorMsg}, #{durationMs}, #{createdAt})")
    int insert(EvalResult entity);
}
