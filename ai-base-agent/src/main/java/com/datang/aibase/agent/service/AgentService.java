package com.datang.aibase.agent.service;

import com.datang.aibase.agent.entity.AgentDef;
import com.datang.aibase.agent.entity.AgentMessage;
import com.datang.aibase.agent.entity.AgentSession;
import com.datang.aibase.api.dto.AgentChatResponse;

import java.util.List;

public interface AgentService {

    List<AgentDef> listAll();

    AgentDef getById(String id);

    AgentDef create(AgentDef agent);

    AgentDef update(String id, AgentDef agent);

    void delete(String id);

    List<AgentSession> listSessions(int limit);

    AgentSession createSession(String agentId, String title);

    List<AgentMessage> getMessages(String sessionId);

    AgentMessage addMessage(String sessionId, AgentMessage message);

    AgentChatResponse chat(String agentId, String sessionId, String userMessage);

    AgentMessage branch(String sessionId, String parentMessageId);

    java.util.Map<String, java.util.List<com.datang.aibase.agent.entity.AgentMessage>> getBranchPoints(String sessionId);
}
