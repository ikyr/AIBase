package com.datang.aibase.agent.memory;

import com.datang.aibase.agent.entity.AgentMessage;
import com.datang.aibase.agent.mapper.AgentMessageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConversationTreeTest {

    private AgentMessageMapper messageMapper;
    private ConversationTree tree;

    @BeforeEach
    void setUp() {
        messageMapper = mock(AgentMessageMapper.class);
        tree = new ConversationTree(messageMapper);
    }

    @Test
    @DisplayName("getPath returns messages from root to target")
    void getPath_returnsChain() {
        AgentMessage root = createMsg("m1", null);
        AgentMessage child = createMsg("m2", "m1");
        AgentMessage grandchild = createMsg("m3", "m2");
        when(messageMapper.selectBySessionId("s1")).thenReturn(List.of(root, child, grandchild));

        List<AgentMessage> path = tree.getPath("s1", "m3");

        assertThat(path).hasSize(3);
        assertThat(path.get(0).getId()).isEqualTo("m1");
        assertThat(path.get(2).getId()).isEqualTo("m3");
    }

    @Test
    @DisplayName("getPath returns single message for root")
    void getPath_rootOnly_returnsSingle() {
        AgentMessage root = createMsg("m1", null);
        when(messageMapper.selectBySessionId("s1")).thenReturn(List.of(root));

        List<AgentMessage> path = tree.getPath("s1", "m1");

        assertThat(path).hasSize(1);
    }

    @Test
    @DisplayName("getPath returns empty list for unknown messageId")
    void getPath_unknownMessageId_returnsEmpty() {
        when(messageMapper.selectBySessionId("s1")).thenReturn(List.of());

        List<AgentMessage> path = tree.getPath("s1", "unknown");

        assertThat(path).isEmpty();
    }

    @Test
    @DisplayName("getBranches returns map of parentId to children when branching detected")
    void getBranches_branching_returnsBranches() {
        AgentMessage root = createMsg("m1", null);
        AgentMessage branchA = createMsg("m2", "m1");
        AgentMessage branchB = createMsg("m3", "m1");
        when(messageMapper.selectBySessionId("s1")).thenReturn(List.of(root, branchA, branchB));

        Map<String, List<AgentMessage>> branches = tree.getBranches("s1");

        assertThat(branches).hasSize(1);
        assertThat(branches.get("m1")).hasSize(2);
    }

    @Test
    @DisplayName("getBranches returns empty when no branching")
    void getBranches_noBranching_returnsEmpty() {
        AgentMessage root = createMsg("m1", null);
        AgentMessage child = createMsg("m2", "m1");
        when(messageMapper.selectBySessionId("s1")).thenReturn(List.of(root, child));

        Map<String, List<AgentMessage>> branches = tree.getBranches("s1");

        assertThat(branches).isEmpty();
    }

    @Test
    @DisplayName("getRootId returns message with null parentId")
    void getRootId_returnsRoot() {
        AgentMessage root = createMsg("root", null);
        AgentMessage child = createMsg("child", "root");
        when(messageMapper.selectBySessionId("s1")).thenReturn(List.of(root, child));

        String rootId = tree.getRootId("s1");

        assertThat(rootId).isEqualTo("root");
    }

    @Test
    @DisplayName("getRootId returns null when no root found")
    void getRootId_noRoot_returnsNull() {
        when(messageMapper.selectBySessionId("s1")).thenReturn(List.of());

        assertThat(tree.getRootId("s1")).isNull();
    }

    private AgentMessage createMsg(String id, String parentId) {
        AgentMessage msg = new AgentMessage();
        msg.setId(id);
        msg.setParentId(parentId);
        msg.setRole("user");
        msg.setContent("message " + id);
        return msg;
    }
}
