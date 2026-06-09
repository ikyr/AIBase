package com.datang.aibase.platform.service.impl;

import com.datang.aibase.platform.entity.ApprovalRecord;
import com.datang.aibase.platform.entity.PromptVersion;
import com.datang.aibase.platform.mapper.ApprovalRecordMapper;
import com.datang.aibase.platform.mapper.PromptVersionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PlatformServiceImplTest {

    private PromptVersionMapper promptVersionMapper;
    private ApprovalRecordMapper approvalRecordMapper;
    private PlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        promptVersionMapper = mock(PromptVersionMapper.class);
        approvalRecordMapper = mock(ApprovalRecordMapper.class);
        service = new PlatformServiceImpl(promptVersionMapper, approvalRecordMapper);
    }

    @Test
    @DisplayName("approve non-PENDING record throws")
    void approve_nonPending_throws() {
        var record = new ApprovalRecord();
        record.setId("appr-1");
        record.setStatus("APPROVED");
        when(approvalRecordMapper.selectById("appr-1")).thenReturn(record);

        assertThatThrownBy(() -> service.approve("appr-1"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot approve record with status");
    }

    @Test
    @DisplayName("approve PENDING record updates status")
    void approve_pending_updates() {
        var record = new ApprovalRecord();
        record.setId("appr-1");
        record.setStatus("PENDING");
        when(approvalRecordMapper.selectById("appr-1")).thenReturn(record);

        service.approve("appr-1");

        verify(approvalRecordMapper).updateStatus("appr-1", "APPROVED");
    }

    @Test
    @DisplayName("reject non-PENDING record throws")
    void reject_nonPending_throws() {
        var record = new ApprovalRecord();
        record.setId("appr-1");
        record.setStatus("APPROVED");
        when(approvalRecordMapper.selectById("appr-1")).thenReturn(record);

        assertThatThrownBy(() -> service.reject("appr-1", "not applicable"))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("reject PENDING record updates status with reason")
    void reject_pending_updates() {
        var record = new ApprovalRecord();
        record.setId("appr-1");
        record.setStatus("PENDING");
        when(approvalRecordMapper.selectById("appr-1")).thenReturn(record);

        service.reject("appr-1", "needs revision");

        verify(approvalRecordMapper).updateStatusWithReason("appr-1", "REJECTED", "needs revision");
    }

    @Test
    @DisplayName("publishPrompt publishes existing prompt")
    void publishPrompt_marksPublished() {
        var prompt = new PromptVersion();
        prompt.setId("prompt-1");
        prompt.setRefType("agent");
        prompt.setRefId("agent-1");
        when(promptVersionMapper.selectById("prompt-1")).thenReturn(prompt);

        service.publishPrompt("prompt-1");

        verify(promptVersionMapper).clearCurrent("agent", "agent-1");
        verify(promptVersionMapper).update("prompt-1", "PUBLISHED", true);
    }

    @Test
    @DisplayName("publishPrompt throws for unknown prompt")
    void publishPrompt_unknown_throws() {
        when(promptVersionMapper.selectById("unknown")).thenReturn(null);

        assertThatThrownBy(() -> service.publishPrompt("unknown"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Prompt version not found");
    }
}
