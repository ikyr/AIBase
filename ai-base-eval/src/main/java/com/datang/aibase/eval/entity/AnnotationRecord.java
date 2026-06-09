package com.datang.aibase.eval.entity;

import com.datang.aibase.common.entity.BaseEntity;

public class AnnotationRecord extends BaseEntity {
    private String evalResultId;
    private String annotatorId;
    private Integer score;
    private String tags;
    private String comment;
    private Boolean isGolden = false;

    public String getEvalResultId() { return evalResultId; }
    public void setEvalResultId(String evalResultId) { this.evalResultId = evalResultId; }
    public String getAnnotatorId() { return annotatorId; }
    public void setAnnotatorId(String annotatorId) { this.annotatorId = annotatorId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Boolean getIsGolden() { return isGolden; }
    public void setIsGolden(Boolean isGolden) { this.isGolden = isGolden; }
}
