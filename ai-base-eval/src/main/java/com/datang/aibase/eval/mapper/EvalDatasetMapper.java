package com.datang.aibase.eval.mapper;

import com.datang.aibase.eval.entity.EvalDataset;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface EvalDatasetMapper {

    @Select("SELECT * FROM eval_dataset WHERE status != 'DELETED' ORDER BY created_at DESC")
    List<EvalDataset> selectAll();

    @Select("SELECT * FROM eval_dataset WHERE id = #{id}")
    EvalDataset selectById(String id);

    @Insert("INSERT INTO eval_dataset (id, name, description, eval_type, item_count, " +
            "status, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{name}, #{description}, #{evalType}, #{itemCount}, " +
            "#{status}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(EvalDataset entity);

    @Update("UPDATE eval_dataset SET status = 'DELETED', updated_at = NOW() WHERE id = #{id}")
    int softDelete(String id);

    @Update("UPDATE eval_dataset SET item_count = (SELECT COUNT(*) FROM eval_dataset_item WHERE dataset_id = #{id} AND status != 'DELETED'), updated_at = NOW() WHERE id = #{id}")
    int updateItemCount(String id);
}
