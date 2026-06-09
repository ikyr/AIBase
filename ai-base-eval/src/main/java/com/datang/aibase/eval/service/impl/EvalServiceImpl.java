package com.datang.aibase.eval.service.impl;

import com.datang.aibase.common.util.SnowflakeIdGenerator;
import com.datang.aibase.eval.engine.EvalExecutor;
import com.datang.aibase.eval.entity.*;
import com.datang.aibase.eval.mapper.*;
import com.datang.aibase.eval.service.EvalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EvalServiceImpl implements EvalService {

    private static final Logger log = LoggerFactory.getLogger(EvalServiceImpl.class);

    private final EvalDatasetMapper datasetMapper;
    private final EvalTaskMapper taskMapper;
    private final EvalResultMapper resultMapper;
    private final EvalDatasetItemMapper itemMapper;
    private final AnnotationRecordMapper annotationMapper;
    private final EvalExecutor evalExecutor;

    public EvalServiceImpl(EvalDatasetMapper datasetMapper,
                           EvalTaskMapper taskMapper,
                           EvalResultMapper resultMapper,
                           EvalDatasetItemMapper itemMapper,
                           AnnotationRecordMapper annotationMapper,
                           EvalExecutor evalExecutor) {
        this.datasetMapper = datasetMapper;
        this.taskMapper = taskMapper;
        this.resultMapper = resultMapper;
        this.itemMapper = itemMapper;
        this.annotationMapper = annotationMapper;
        this.evalExecutor = evalExecutor;
    }

    @Override
    public List<EvalDataset> listDatasets() {
        return datasetMapper.selectAll();
    }

    @Override
    public EvalDataset getDataset(String id) {
        return datasetMapper.selectById(id);
    }

    @Override
    public EvalDataset createDataset(EvalDataset dataset) {
        dataset.setId(SnowflakeIdGenerator.nextId());
        datasetMapper.insert(dataset);
        return dataset;
    }

    @Override
    public void deleteDataset(String id) {
        datasetMapper.softDelete(id);
        itemMapper.softDeleteByDatasetId(id);
    }

    @Override
    public List<EvalDatasetItem> getDatasetItems(String datasetId) {
        return itemMapper.selectByDatasetId(datasetId);
    }

    @Override
    public EvalDatasetItem addDatasetItem(String datasetId, EvalDatasetItem item) {
        item.setId(SnowflakeIdGenerator.nextId());
        item.setDatasetId(datasetId);
        itemMapper.insert(item);
        return item;
    }

    @Override
    public List<EvalTask> listTasks(int limit) {
        return taskMapper.selectRecent(limit);
    }

    @Override
    public EvalTask getTask(String id) {
        return taskMapper.selectById(id);
    }

    @Override
    public EvalTask createTask(EvalTask task) {
        task.setId(SnowflakeIdGenerator.nextId());
        task.setStatus("PENDING");
        taskMapper.insert(task);
        return task;
    }

    @Override
    public Map<String, Object> executeTask(String taskId) {
        evalExecutor.execute(taskId);
        EvalTask updated = taskMapper.selectById(taskId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskId", taskId);
        result.put("status", updated != null ? updated.getStatus() : "UNKNOWN");
        result.put("metrics", updated != null && updated.getMetrics() != null ? updated.getMetrics() : "{}");
        return result;
    }

    @Override
    public List<EvalResult> getResults(String taskId) {
        return resultMapper.selectByTaskId(taskId);
    }

    @Override
    public AnnotationRecord addAnnotation(String resultId, AnnotationRecord annotation) {
        annotation.setId(SnowflakeIdGenerator.nextId());
        annotation.setEvalResultId(resultId);
        annotationMapper.insert(annotation);
        return annotation;
    }

    @Override
    public List<AnnotationRecord> getAnnotations(String resultId) {
        return annotationMapper.selectByEvalResultId(resultId);
    }
}
