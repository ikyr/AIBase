package com.datang.aibase.platform.mapper;

import com.datang.aibase.platform.entity.ApprovalRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ApprovalRecordMapper {

    @Select("SELECT * FROM approval_record ORDER BY created_at DESC")
    List<ApprovalRecord> selectAll();

    @Select("SELECT * FROM approval_record WHERE id = #{id}")
    ApprovalRecord selectById(String id);

    @Insert("INSERT INTO approval_record (id, type, ref_type, ref_id, ref_name, requester, " +
            "approvers, status, reason, created_at, updated_at, created_by, updated_by) " +
            "VALUES (#{id}, #{type}, #{refType}, #{refId}, #{refName}, #{requester}, " +
            "#{approvers}, #{status}, #{reason}, #{createdAt}, #{updatedAt}, #{createdBy}, #{updatedBy})")
    int insert(ApprovalRecord entity);

    @Update("UPDATE approval_record SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("status") String status);

    @Update("UPDATE approval_record SET status = #{status}, reason = #{reason}, updated_at = NOW() WHERE id = #{id}")
    int updateStatusWithReason(@Param("id") String id, @Param("status") String status, @Param("reason") String reason);

    @Select("SELECT * FROM approval_record WHERE status = #{status} ORDER BY created_at DESC")
    List<ApprovalRecord> selectByStatus(@Param("status") String status);

    @Update("UPDATE approval_record SET approvers = #{approvers}, updated_at = NOW() WHERE id = #{id}")
    int updateAssignee(@Param("id") String id, @Param("approvers") String approvers);
}
