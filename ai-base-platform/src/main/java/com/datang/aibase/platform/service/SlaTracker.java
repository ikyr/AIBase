package com.datang.aibase.platform.service;

import com.datang.aibase.platform.entity.ApprovalRecord;
import com.datang.aibase.platform.mapper.ApprovalRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class SlaTracker {

    private static final Logger log = LoggerFactory.getLogger(SlaTracker.class);
    private static final Duration DEFAULT_SLA = Duration.ofHours(24);

    private final ApprovalRecordMapper approvalRecordMapper;

    public SlaTracker(ApprovalRecordMapper approvalRecordMapper) {
        this.approvalRecordMapper = approvalRecordMapper;
    }

    @Scheduled(fixedDelay = 60_000)
    public void checkSla() {
        List<ApprovalRecord> pending = approvalRecordMapper.selectByStatus("PENDING");
        LocalDateTime now = LocalDateTime.now();

        for (ApprovalRecord record : pending) {
            if (record.getCreatedAt() == null) continue;

            Duration elapsed = Duration.between(record.getCreatedAt(), now);
            long elapsedHours = elapsed.toHours();

            if (elapsedHours >= 48) {
                log.warn("SLA CRITICAL: Approval {} has been pending for {} hours, escalating",
                        record.getId(), elapsedHours);
                approvalRecordMapper.updateStatusWithReason(record.getId(), "ESCALATED",
                        "Auto-escalated after 48h pending, elapsed: " + elapsedHours + "h");
            } else if (elapsedHours >= 24) {
                log.warn("SLA WARNING: Approval {} has been pending for {} hours",
                        record.getId(), elapsedHours);
            } else if (elapsedHours >= 12) {
                log.info("SLA NOTICE: Approval {} pending for {} hours, step {}/{}",
                        record.getId(), elapsedHours,
                        record.getChainStep() != null ? record.getChainStep() : 1,
                        record.getTotalSteps() != null ? record.getTotalSteps() : 1);
            }
        }
    }
}
