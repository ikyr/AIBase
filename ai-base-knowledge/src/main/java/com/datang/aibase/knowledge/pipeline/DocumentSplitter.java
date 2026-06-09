package com.datang.aibase.knowledge.pipeline;

import java.util.ArrayList;
import java.util.List;

public class DocumentSplitter {

    private final int chunkSize;
    private final int chunkOverlap;

    public DocumentSplitter(int chunkSize, int chunkOverlap) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    public List<String> split(String content) {
        List<String> chunks = new ArrayList<>();
        if (content == null || content.isBlank()) return chunks;

        int start = 0;
        while (start < content.length()) {
            int end = Math.min(start + chunkSize, content.length());
            if (end < content.length()) {
                int lastPeriod = content.lastIndexOf('。', end);
                int lastNewline = content.lastIndexOf('\n', end);
                int breakPoint = Math.max(lastPeriod, lastNewline);
                if (breakPoint > start) {
                    end = breakPoint + 1;
                }
            }
            chunks.add(content.substring(start, end).trim());
            start = end - chunkOverlap;
            if (start < 0) start = 0;
            if (end >= content.length()) break;
        }
        return chunks;
    }
}
