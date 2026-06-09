package com.datang.aibase.agent.memory;

import com.datang.aibase.agent.entity.AgentMessage;
import com.datang.aibase.agent.mapper.AgentMessageMapper;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ConversationTree {

    private final AgentMessageMapper messageMapper;

    public ConversationTree(AgentMessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    public List<AgentMessage> getPath(String sessionId, String messageId) {
        List<AgentMessage> all = messageMapper.selectBySessionId(sessionId);
        Map<String, AgentMessage> byId = new LinkedHashMap<>();
        for (AgentMessage m : all) byId.put(m.getId(), m);

        List<AgentMessage> path = new ArrayList<>();
        String current = messageId;
        while (current != null) {
            AgentMessage msg = byId.get(current);
            if (msg != null) {
                path.addFirst(msg);
                current = msg.getParentId();
            } else {
                break;
            }
        }
        return path;
    }

    public Map<String, List<AgentMessage>> getBranches(String sessionId) {
        List<AgentMessage> all = messageMapper.selectBySessionId(sessionId);
        Map<String, List<AgentMessage>> childrenByParent = new LinkedHashMap<>();

        for (AgentMessage m : all) {
            String parentId = m.getParentId() != null ? m.getParentId() : "root";
            childrenByParent.computeIfAbsent(parentId, k -> new ArrayList<>()).add(m);
        }

        Map<String, List<AgentMessage>> branches = new LinkedHashMap<>();
        for (var entry : childrenByParent.entrySet()) {
            if (entry.getValue().size() > 1) {
                branches.put(entry.getKey(), entry.getValue());
            }
        }
        return branches;
    }

    public String getRootId(String sessionId) {
        List<AgentMessage> all = messageMapper.selectBySessionId(sessionId);
        return all.stream()
                .filter(m -> m.getParentId() == null)
                .findFirst()
                .map(AgentMessage::getId)
                .orElse(null);
    }
}
