package com.datang.aibase.api.dto;
import com.datang.aibase.common.enums.SourceType;
import java.util.Map;
public class IngestRequest {
    private String kbId;
    private String title;
    private String content;
    private SourceType sourceType;
    private Map<String,Object> metadata;
    public String getKbId() { return kbId; }
    public void setKbId(String kbId) { this.kbId = kbId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public SourceType getSourceType() { return sourceType; }
    public void setSourceType(SourceType sourceType) { this.sourceType = sourceType; }
    public Map<String,Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String,Object> metadata) { this.metadata = metadata; }
}
