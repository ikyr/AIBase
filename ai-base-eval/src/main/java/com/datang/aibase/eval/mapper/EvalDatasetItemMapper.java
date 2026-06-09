package com.datang.aibase.eval.mapper;

import com.datang.aibase.eval.entity.EvalDatasetItem;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface EvalDatasetItemMapper {

    @Select("SELECT * FROM eval_dataset_item WHERE dataset_id = #{datasetId} AND status != 'DELETED' ORDER BY created_at")
    List<EvalDatasetItem> selectByDatasetId(@Param("datasetId") String datasetId);

    @Select("SELECT * FROM eval_dataset_item WHERE id = #{id}")
    EvalDatasetItem selectById(String id);

    @Insert("INSERT INTO eval_dataset_item (id, dataset_id, question, expected_answer, context, metadata, " +
            "status, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{datasetId}, #{question}, #{expectedAnswer}, #{context}, #{metadata}, " +
            "#{status}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(EvalDatasetItem entity);

    @Insert("<script>" +
            "INSERT INTO eval_dataset_item (id, dataset_id, question, expected_answer, context, metadata, " +
            "status, created_at, updated_at, created_by, updated_by) VALUES " +
            "<foreach collection='items' item='item' separator=','>" +
            "(#{item.id}, #{item.datasetId}, #{item.question}, #{item.expectedAnswer}, #{item.context}, #{item.metadata}, " +
            "#{item.status}, #{item.createdAt}, #{item.updatedAt}, #{item.createdBy}, #{item.updatedBy})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("items") List<EvalDatasetItem> items);

    @Update("UPDATE eval_dataset_item SET status = 'DELETED' WHERE id = #{id}")
    int softDelete(String id);

    @Update("UPDATE eval_dataset_item SET status = 'DELETED' WHERE dataset_id = #{datasetId}")
    int softDeleteByDatasetId(@Param("datasetId") String datasetId);
}
