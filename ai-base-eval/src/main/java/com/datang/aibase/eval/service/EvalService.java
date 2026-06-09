package com.datang.aibase.eval.service;

import com.datang.aibase.eval.entity.*;

import java.util.List;
import java.util.Map;

public interface EvalService {

    // Datasets
    List<EvalDataset> listDatasets();
    EvalDataset getDataset(String id);
    EvalDataset createDataset(EvalDataset dataset);
    void deleteDataset(String id);

    // Dataset items
    List<EvalDatasetItem> getDatasetItems(String datasetId);
    EvalDatasetItem addDatasetItem(String datasetId, EvalDatasetItem item);

    // Tasks
    List<EvalTask> listTasks(int limit);
    EvalTask getTask(String id);
    EvalTask createTask(EvalTask task);
    Map<String, Object> executeTask(String taskId);

    // Results
    List<EvalResult> getResults(String taskId);

    // Annotations
    AnnotationRecord addAnnotation(String resultId, AnnotationRecord annotation);
    List<AnnotationRecord> getAnnotations(String resultId);
}
