package com.datang.aibase.platform.service.impl;

import com.datang.aibase.common.util.SnowflakeIdGenerator;
import com.datang.aibase.platform.entity.ApprovalRecord;
import com.datang.aibase.platform.entity.PromptVersion;
import com.datang.aibase.platform.mapper.ApprovalRecordMapper;
import com.datang.aibase.platform.mapper.PromptVersionMapper;
import com.datang.aibase.platform.service.PlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlatformServiceImpl implements PlatformService {

    private static final Logger log = LoggerFactory.getLogger(PlatformServiceImpl.class);

    private final PromptVersionMapper promptVersionMapper;
    private final ApprovalRecordMapper approvalRecordMapper;

    public PlatformServiceImpl(PromptVersionMapper promptVersionMapper,
                               ApprovalRecordMapper approvalRecordMapper) {
        this.promptVersionMapper = promptVersionMapper;
        this.approvalRecordMapper = approvalRecordMapper;
    }

    @Override
    public List<PromptVersion> listPrompts() {
        return promptVersionMapper.selectAll();
    }

    @Override
    public PromptVersion createPrompt(PromptVersion prompt) {
        prompt.setId(SnowflakeIdGenerator.nextId());
        if (prompt.getVersion() == null) prompt.setVersion(1);
        if (prompt.getStatus() == null) prompt.setStatus("DRAFT");
        promptVersionMapper.insert(prompt);
        return prompt;
    }

    @Override
    public PromptVersion publishPrompt(String id) {
        PromptVersion prompt = promptVersionMapper.selectById(id);
        if (prompt == null) throw new IllegalArgumentException("Prompt version not found: " + id);

        promptVersionMapper.clearCurrent(prompt.getRefType(), prompt.getRefId());
        promptVersionMapper.update(id, "PUBLISHED", true);
        log.info("Published prompt version {} for {}/{}", id, prompt.getRefType(), prompt.getRefId());
        return promptVersionMapper.selectById(id);
    }

    @Override
    public PromptVersion rollbackPrompt(String refType, String refId, int targetVersion) {
        PromptVersion current = promptVersionMapper.selectCurrent(refType, refId);
        if (current == null) throw new IllegalArgumentException("No current version found for " + refType + "/" + refId);

        List<PromptVersion> allVersions = promptVersionMapper.selectAll();
        PromptVersion target = allVersions.stream()
                .filter(v -> v.getRefType().equals(refType) && v.getRefId().equals(refId)
                        && v.getVersion() == targetVersion && !"DELETED".equals(v.getStatus()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Target version not found: " + targetVersion));

        promptVersionMapper.update(current.getId(), "ROLLED_BACK", false);
        promptVersionMapper.update(target.getId(), "PUBLISHED", true);
        log.info("Rolled back {}/{} from version {} to {}", refType, refId, current.getVersion(), targetVersion);
        return promptVersionMapper.selectById(target.getId());
    }

    @Override
    public List<ApprovalRecord> listApprovals() {
        return approvalRecordMapper.selectAll();
    }

    @Override
    public ApprovalRecord createApproval(ApprovalRecord record) {
        record.setId(SnowflakeIdGenerator.nextId());
        if (record.getStatus() == null) record.setStatus("PENDING");
        approvalRecordMapper.insert(record);
        return record;
    }

    @Override
    public ApprovalRecord approve(String id) {
        ApprovalRecord record = approvalRecordMapper.selectById(id);
        if (record == null) throw new IllegalArgumentException("Approval not found: " + id);
        if (!"PENDING".equals(record.getStatus())) {
            throw new IllegalStateException("Cannot approve record with status: " + record.getStatus());
        }
        approvalRecordMapper.updateStatus(id, "APPROVED");

        if ("PROMPT_VERSION".equals(record.getRefType()) && record.getRefId() != null) {
            try {
                publishPrompt(record.getRefId());
                log.info("Auto-published prompt {} after approval {}", record.getRefId(), id);
            } catch (Exception e) {
                log.warn("Failed to auto-publish prompt {} after approval: {}", record.getRefId(), e.getMessage());
            }
        }

        return approvalRecordMapper.selectById(id);
    }

    @Override
    public ApprovalRecord reject(String id, String reason) {
        ApprovalRecord record = approvalRecordMapper.selectById(id);
        if (record == null) throw new IllegalArgumentException("Approval not found: " + id);
        if (!"PENDING".equals(record.getStatus())) {
            throw new IllegalStateException("Cannot reject record with status: " + record.getStatus());
        }
        approvalRecordMapper.updateStatusWithReason(id, "REJECTED", reason != null ? reason : "");
        return approvalRecordMapper.selectById(id);
    }

    @Override
    public ApprovalRecord delegate(String id, String newApprover) {
        ApprovalRecord record = approvalRecordMapper.selectById(id);
        if (record == null) throw new IllegalArgumentException("Approval not found: " + id);
        if (!"PENDING".equals(record.getStatus())) {
            throw new IllegalStateException("Cannot delegate record with status: " + record.getStatus());
        }

        String previousApprovers = record.getApprovers() != null ? record.getApprovers() + "," : "";
        String updatedApprovers = previousApprovers + newApprover;
        approvalRecordMapper.updateAssignee(id, updatedApprovers);
        log.info("Delegated approval {} to {}", id, newApprover);
        return approvalRecordMapper.selectById(id);
    }
}
