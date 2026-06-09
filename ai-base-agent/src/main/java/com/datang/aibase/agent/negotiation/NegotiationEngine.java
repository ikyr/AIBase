package com.datang.aibase.agent.negotiation;

import com.datang.aibase.agent.client.ModelGatewayClient;
import com.datang.aibase.agent.entity.AgentDef;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class NegotiationEngine {

    private static final Logger log = LoggerFactory.getLogger(NegotiationEngine.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final ModelGatewayClient llmClient;
    private final ConcurrentMap<String, NegotiationSession> sessions = new ConcurrentHashMap<>();

    public NegotiationEngine(ModelGatewayClient llmClient) {
        this.llmClient = llmClient;
    }

    public Map<String, Object> negotiate(String sessionId, List<AgentDef> participants,
                                          String topic, String context) {
        NegotiationSession session = new NegotiationSession(sessionId, participants, topic);
        sessions.put(sessionId, session);

        try {
            // Phase 1: PROPOSE — each agent proposes
            for (AgentDef agent : participants) {
                String proposal = generateProposal(agent, topic, context);
                NegotiationMessage msg = new NegotiationMessage(
                        agent.getId(), agent.getName(), "PROPOSE", proposal, 0.5);
                session.addMessage(msg);
                log.info("Negotiation {} — {} proposed: {}", sessionId, agent.getName(),
                        proposal.length() > 80 ? proposal.substring(0, 80) + "..." : proposal);
            }

            // Phase 2: VOTE — each agent votes on all proposals
            List<NegotiationMessage> proposals = session.getMessagesByType("PROPOSE");
            Map<String, Integer> votes = new LinkedHashMap<>();

            for (AgentDef agent : participants) {
                for (NegotiationMessage proposal : proposals) {
                    String vote = generateVote(agent, topic, proposal.content());
                    NegotiationMessage voteMsg = new NegotiationMessage(
                            agent.getId(), agent.getName(), "VOTE",
                            vote, proposal.fromAgentId().equals(agent.getId()) ? 0.8 : 0.3);
                    session.addMessage(voteMsg);

                    if (vote.contains("AGREE") || vote.contains("agree")) {
                        votes.merge(proposal.fromAgentId(), 1, Integer::sum);
                    }
                }
            }

            // Phase 3: ARBITRATE — select winner
            String winnerId = votes.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(participants.get(0).getId());

            NegotiationMessage winnerProposal = proposals.stream()
                    .filter(p -> p.fromAgentId().equals(winnerId))
                    .findFirst().orElse(null);

            String decision = generateArbitration(participants.get(0), topic,
                    proposals, votes);
            NegotiationMessage decisionMsg = new NegotiationMessage(
                    "arbitrator", "Arbitrator", "ARBITRATE", decision, 0.9);
            session.addMessage(decisionMsg);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("sessionId", sessionId);
            result.put("topic", topic);
            result.put("winnerAgentId", winnerId);
            result.put("winnerProposal", winnerProposal != null ? winnerProposal.content() : "");
            result.put("decision", decision);
            result.put("voteCounts", votes);
            result.put("rounds", session.getMessages().size());
            result.put("round", session.getRound());

            return result;
        } finally {
            sessions.remove(sessionId);
        }
    }

    private String generateProposal(AgentDef agent, String topic, String context) {
        String prompt = agent.getSystemPrompt() != null ? agent.getSystemPrompt() : "You are an AI assistant.";
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", prompt +
                        " You are participating in a negotiation. Make a concise proposal."),
                Map.of("role", "user", "content", "Topic: " + topic + "\nContext: " + context +
                        "\n\nMake your best proposal. Be specific and actionable.")
        );
        try {
            return llmClient.chat(messages);
        } catch (Exception e) {
            return "Proposal from " + agent.getName() + ": " + topic;
        }
    }

    private String generateVote(AgentDef agent, String topic, String proposalContent) {
        String prompt = agent.getSystemPrompt() != null ? agent.getSystemPrompt() : "You are an AI assistant.";
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", prompt +
                        " You are voting on a proposal. Respond with AGREE or REFUTE and a brief reason."),
                Map.of("role", "user", "content", "Topic: " + topic + "\nProposal: " + proposalContent +
                        "\n\nYour vote (AGREE/REFUTE with reason):")
        );
        try {
            return llmClient.chat(messages);
        } catch (Exception e) {
            return "AGREE — default vote";
        }
    }

    private String generateArbitration(AgentDef arbitrator, String topic,
                                        List<NegotiationMessage> proposals,
                                        Map<String, Integer> votes) {
        StringBuilder sb = new StringBuilder("Proposals:\n");
        for (var p : proposals) {
            sb.append("- ").append(p.fromAgentName()).append(": ")
                    .append(p.content().length() > 100 ? p.content().substring(0, 100) + "..." : p.content())
                    .append(" (votes: ").append(votes.getOrDefault(p.fromAgentId(), 0)).append(")\n");
        }

        String prompt = arbitrator.getSystemPrompt() != null ? arbitrator.getSystemPrompt() : "You are an AI arbitrator.";
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", prompt + " You are the arbitrator. Make a final decision."),
                Map.of("role", "user", "content", "Topic: " + topic + "\n" + sb +
                        "\nAs arbitrator, what is your final decision? Be clear and definitive.")
        );
        try {
            return llmClient.chat(messages);
        } catch (Exception e) {
            return "Arbitration decision: Select proposal with most votes";
        }
    }

    // ---- Inner types ----

    public record NegotiationMessage(String fromAgentId, String fromAgentName,
                                      String msgType, String content, double confidence) {}

    public static class NegotiationSession {
        private final String id;
        private final List<AgentDef> participants;
        private final String topic;
        private final List<NegotiationMessage> messages = new ArrayList<>();
        private int round = 1;
        private final LocalDateTime startedAt = LocalDateTime.now();

        public NegotiationSession(String id, List<AgentDef> participants, String topic) {
            this.id = id;
            this.participants = participants;
            this.topic = topic;
        }

        public void addMessage(NegotiationMessage msg) { messages.add(msg); }
        public List<NegotiationMessage> getMessagesByType(String type) {
            return messages.stream().filter(m -> type.equals(m.msgType())).toList();
        }
        public List<NegotiationMessage> getMessages() { return List.copyOf(messages); }
        public int getRound() { return round; }
        public String getId() { return id; }
        public String getTopic() { return topic; }
    }
}
