package com.datang.aibase.agent.service.impl;

import com.datang.aibase.agent.bridge.GraphBridge;
import com.datang.aibase.agent.client.ModelGatewayClient;
import com.datang.aibase.agent.engine.ReActLoop;
import com.datang.aibase.agent.entity.AgentDef;
import com.datang.aibase.agent.entity.AgentMessage;
import com.datang.aibase.agent.entity.AgentSession;
import com.datang.aibase.agent.mapper.AgentDefMapper;
import com.datang.aibase.agent.mapper.AgentMessageMapper;
import com.datang.aibase.agent.mapper.AgentSessionMapper;
import com.datang.aibase.agent.memory.ContextWindowManager;
import com.datang.aibase.agent.memory.ConversationTree;
import com.datang.aibase.agent.negotiation.NegotiationEngine;
import com.datang.aibase.agent.service.AgentService;
import com.datang.aibase.agent.tool.ToolRegistry;
import com.datang.aibase.api.dto.AgentChatResponse;
import com.datang.aibase.common.util.SnowflakeIdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AgentServiceImpl implements AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentServiceImpl.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final AgentDefMapper agentDefMapper;
    private final AgentSessionMapper sessionMapper;
    private final AgentMessageMapper messageMapper;
    private final ReActLoop reActLoop;
    private final ModelGatewayClient modelGatewayClient;
    private final ToolRegistry toolRegistry;
    private final ContextWindowManager contextWindowManager;
    private final ConversationTree conversationTree;
    private final GraphBridge graphBridge;
    private final NegotiationEngine negotiationEngine;

    public AgentServiceImpl(AgentDefMapper agentDefMapper,
                            AgentSessionMapper sessionMapper,
                            AgentMessageMapper messageMapper,
                            ReActLoop reActLoop,
                            ModelGatewayClient modelGatewayClient,
                            ToolRegistry toolRegistry,
                            ContextWindowManager contextWindowManager,
                            ConversationTree conversationTree,
                            GraphBridge graphBridge,
                            NegotiationEngine negotiationEngine) {
        this.agentDefMapper = agentDefMapper;
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.reActLoop = reActLoop;
        this.modelGatewayClient = modelGatewayClient;
        this.toolRegistry = toolRegistry;
        this.contextWindowManager = contextWindowManager;
        this.conversationTree = conversationTree;
        this.graphBridge = graphBridge;
        this.negotiationEngine = negotiationEngine;
    }

    @Override
    public List<AgentDef> listAll() {
        return agentDefMapper.selectAll();
    }

    @Override
    public AgentDef getById(String id) {
        return agentDefMapper.selectById(id);
    }

    @Override
    public AgentDef create(AgentDef agent) {
        agent.setId(SnowflakeIdGenerator.nextId());
        agentDefMapper.insert(agent);
        return agent;
    }

    @Override
    public List<AgentSession> listSessions(int limit) {
        return sessionMapper.selectRecent(limit);
    }

    @Override
    public AgentSession createSession(String agentId, String title) {
        AgentSession session = new AgentSession();
        session.setId(SnowflakeIdGenerator.nextId());
        session.setAgentId(agentId);
        session.setTitle(title != null ? title : "New Session");
        session.setStatus("ACTIVE");
        session.setStartedAt(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    @Override
    public List<AgentMessage> getMessages(String sessionId) {
        return messageMapper.selectBySessionId(sessionId);
    }

    @Override
    public AgentMessage addMessage(String sessionId, AgentMessage message) {
        message.setId(SnowflakeIdGenerator.nextId());
        message.setSessionId(sessionId);
        messageMapper.insert(message);
        return message;
    }

    @Override
    public AgentMessage branch(String sessionId, String parentMessageId) {
        AgentMessage branchMsg = new AgentMessage();
        branchMsg.setId(SnowflakeIdGenerator.nextId());
        branchMsg.setSessionId(sessionId);
        branchMsg.setParentId(parentMessageId);
        branchMsg.setRole("system");
        branchMsg.setContent("[Branch point]");
        branchMsg.setContentType("text");
        messageMapper.insert(branchMsg);
        return branchMsg;
    }

    @Override
    public Map<String, List<AgentMessage>> getBranchPoints(String sessionId) {
        return conversationTree.getBranches(sessionId);
    }

    @Override
    public AgentChatResponse chat(String agentId, String sessionId, String userMessage) {
        AgentDef agent = agentDefMapper.selectById(agentId);
        if (agent == null) throw new IllegalArgumentException("Agent not found: " + agentId);

        AgentSession session;
        if (sessionId != null && !sessionId.isBlank()) {
            session = sessionMapper.selectById(sessionId);
            if (session == null) throw new IllegalArgumentException("Session not found: " + sessionId);
        } else {
            session = createSession(agentId, "Chat " + System.currentTimeMillis());
        }

        String mode = agent.getCoordinationMode() != null ? agent.getCoordinationMode().toUpperCase() : "REACT";

        return switch (mode) {
            case "GRAPH" -> chatWithGraph(agent, session, userMessage);
            case "NEGOTIATION" -> chatWithNegotiation(agent, session, userMessage);
            default -> chatWithReAct(agent, session, userMessage);
        };
    }

    private AgentChatResponse chatWithReAct(AgentDef agent, AgentSession session, String userMessage) {
        Set<String> allowedToolNames = parseAllowedTools(agent);

        List<AgentMessage> fullHistory = messageMapper.selectBySessionId(session.getId());
        List<AgentMessage> history = contextWindowManager.trim(fullHistory, 2000);

        List<AgentMessage> newMessages = reActLoop.run(agent, session, history, userMessage,
                modelGatewayClient::chat, allowedToolNames);

        for (AgentMessage msg : newMessages) {
            msg.setId(SnowflakeIdGenerator.nextId());
            messageMapper.insert(msg);
        }

        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.update(session);

        return buildResponse(session.getId(), newMessages);
    }

    private AgentChatResponse chatWithGraph(AgentDef agent, AgentSession session, String userMessage) {
        String workflowDefId = agent.getConstraints() != null ? extractWorkflowId(agent.getConstraints()) : null;
        if (workflowDefId == null) {
            log.warn("Agent {} has GRAPH mode but no workflow definition in constraints", agent.getId());
            return chatWithReAct(agent, session, userMessage);
        }

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("message", userMessage);
        input.put("agentId", agent.getId());
        input.put("sessionId", session.getId());

        Map<String, Object> result = graphBridge.submit(workflowDefId, input);

        AgentMessage userMsg = new AgentMessage();
        userMsg.setId(SnowflakeIdGenerator.nextId());
        userMsg.setSessionId(session.getId());
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        userMsg.setContentType("text");
        messageMapper.insert(userMsg);

        AgentMessage resultMsg = new AgentMessage();
        resultMsg.setId(SnowflakeIdGenerator.nextId());
        resultMsg.setSessionId(session.getId());
        resultMsg.setParentId(userMsg.getId());
        resultMsg.setRole("assistant");
        resultMsg.setContent(String.valueOf(result.getOrDefault("output",
                result.getOrDefault("error", "Workflow completed"))));
        resultMsg.setContentType("text");
        messageMapper.insert(resultMsg);

        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.update(session);

        AgentChatResponse response = new AgentChatResponse();
        response.setSessionId(session.getId());
        response.setMessageId(resultMsg.getId());
        response.setContent(resultMsg.getContent());
        response.setFinishReason("graph");
        return response;
    }

    private AgentChatResponse chatWithNegotiation(AgentDef agent, AgentSession session, String userMessage) {
        List<AgentDef> participants = new ArrayList<>();
        participants.add(agent);

        // Parse additional participants from constraints if specified
        if (agent.getConstraints() != null) {
            try {
                Map<String, Object> constraints = mapper.readValue(agent.getConstraints(), Map.class);
                Object participantIds = constraints.get("participants");
                if (participantIds instanceof List<?> ids) {
                    for (Object id : ids) {
                        AgentDef participant = agentDefMapper.selectById(String.valueOf(id));
                        if (participant != null && !participant.getId().equals(agent.getId())) {
                            participants.add(participant);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse negotiation participants: {}", e.getMessage());
            }
        }

        String topic = userMessage;
        String context = agent.getSystemPrompt() != null ? agent.getSystemPrompt() : "";

        Map<String, Object> negotiationResult = negotiationEngine.negotiate(
                session.getId(), participants, topic, context);

        AgentMessage userMsg = new AgentMessage();
        userMsg.setId(SnowflakeIdGenerator.nextId());
        userMsg.setSessionId(session.getId());
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        userMsg.setContentType("text");
        messageMapper.insert(userMsg);

        String content = "Winner: " + negotiationResult.get("winnerAgentId") +
                "\nDecision: " + negotiationResult.get("decision");

        AgentMessage resultMsg = new AgentMessage();
        resultMsg.setId(SnowflakeIdGenerator.nextId());
        resultMsg.setSessionId(session.getId());
        resultMsg.setParentId(userMsg.getId());
        resultMsg.setRole("assistant");
        resultMsg.setContent(content);
        resultMsg.setContentType("text");
        messageMapper.insert(resultMsg);

        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.update(session);

        AgentChatResponse response = new AgentChatResponse();
        response.setSessionId(session.getId());
        response.setMessageId(resultMsg.getId());
        response.setContent(content);
        response.setFinishReason("negotiation");
        return response;
    }

    private String extractWorkflowId(String constraintsJson) {
        try {
            Map<String, Object> constraints = mapper.readValue(constraintsJson, Map.class);
            Object workflowId = constraints.get("workflowDefId");
            return workflowId != null ? workflowId.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Set<String> parseAllowedTools(AgentDef agent) {
        Set<String> names = new LinkedHashSet<>();

        if (agent.getTools() != null && !agent.getTools().isBlank()) {
            for (String name : agent.getTools().split(",")) {
                String trimmed = name.trim();
                if (!trimmed.isEmpty()) {
                    names.add(trimmed);
                    if (!toolRegistry.getToolNames().contains(trimmed)) {
                        log.warn("Agent '{}' references unknown tool: {}", agent.getName(), trimmed);
                    }
                }
            }
        }

        if (agent.getSkillIds() != null && !agent.getSkillIds().isBlank()) {
            names.add("skill_executor");
        }

        return names;
    }

    private AgentChatResponse buildResponse(String sessionId, List<AgentMessage> messages) {
        AgentChatResponse response = new AgentChatResponse();
        response.setSessionId(sessionId);

        List<AgentChatResponse.ToolCallRecord> toolCalls = new ArrayList<>();
        String finalContent = "";
        int totalTokens = 0;

        for (AgentMessage msg : messages) {
            totalTokens += msg.getTokenCount() != null ? msg.getTokenCount() : 0;

            if ("assistant".equals(msg.getRole())) {
                response.setMessageId(msg.getId());
                finalContent = msg.getContent();
            }

            if ("tool_call".equals(msg.getContentType()) && msg.getToolCalls() != null) {
                try {
                    Map<String, Object> tc = mapper.readValue(msg.getToolCalls(), Map.class);
                    AgentChatResponse.ToolCallRecord record = new AgentChatResponse.ToolCallRecord();
                    record.setToolName((String) tc.get("tool"));
                    record.setInput(tc.get("parameters") != null ? tc.get("parameters").toString() : "");
                    record.setOutput((String) tc.get("result"));
                    toolCalls.add(record);
                } catch (Exception e) {
                    log.warn("Failed to parse tool call record: {}", e.getMessage());
                }
            }
        }

        response.setContent(finalContent);
        response.setToolCalls(toolCalls);
        response.setTokensUsed(totalTokens);
        response.setFinishReason(toolCalls.isEmpty() ? "direct" : "tool_chain");

        return response;
    }
}
