package com.datang.aibase.eval.entity;
import com.datang.aibase.common.entity.BaseEntity;

public class EvalDatasetItem extends BaseEntity {
    private String datasetId;
    private String question;
    private String expectedAnswer;
    private String context;
    private String metadata;

    public String getDatasetId() { return datasetId; }
    public void setDatasetId(String datasetId) { this.datasetId = datasetId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getExpectedAnswer() { return expectedAnswer; }
    public void setExpectedAnswer(String expectedAnswer) { this.expectedAnswer = expectedAnswer; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
