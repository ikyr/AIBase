package com.datang.aibase.platform.service;

import com.datang.aibase.platform.entity.ApprovalRecord;
import com.datang.aibase.platform.entity.PromptVersion;

import java.util.List;

public interface PlatformService {

    List<PromptVersion> listPrompts();

    PromptVersion createPrompt(PromptVersion prompt);

    PromptVersion publishPrompt(String id);

    PromptVersion rollbackPrompt(String refType, String refId, int targetVersion);

    List<ApprovalRecord> listApprovals();

    ApprovalRecord createApproval(ApprovalRecord record);

    ApprovalRecord approve(String id);

    ApprovalRecord reject(String id, String reason);

    ApprovalRecord delegate(String id, String newApprover);
}
