CREATE TABLE IF NOT EXISTS eval_dataset (
    id          VARCHAR(19)  PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    eval_type   VARCHAR(50),
    item_count  INTEGER NOT NULL DEFAULT 0,
    status      VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS eval_dataset_item (
    id              VARCHAR(19)  PRIMARY KEY,
    dataset_id      VARCHAR(19)  NOT NULL REFERENCES eval_dataset(id),
    question        TEXT,
    expected_answer TEXT,
    context         TEXT,
    metadata        TEXT,
    status          VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS eval_task (
    id            VARCHAR(19)  PRIMARY KEY,
    dataset_id    VARCHAR(19)  NOT NULL REFERENCES eval_dataset(id),
    target_id     VARCHAR(19),
    target_type   VARCHAR(50),
    status        VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    metrics       TEXT,
    total_items   INTEGER,
    passed_items  INTEGER,
    started_at    TIMESTAMP,
    completed_at  TIMESTAMP,
    trace_id      VARCHAR(100),
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS eval_result (
    id            VARCHAR(19) PRIMARY KEY,
    task_id       VARCHAR(19) NOT NULL REFERENCES eval_task(id),
    item_id       VARCHAR(19),
    actual_output TEXT,
    metrics       TEXT,
    passed        BOOLEAN,
    error_msg     TEXT,
    duration_ms   INTEGER,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS annotation_record (
    id              VARCHAR(19) PRIMARY KEY,
    eval_result_id  VARCHAR(19) NOT NULL REFERENCES eval_result(id),
    annotator_id    VARCHAR(100),
    score           INTEGER,
    tags            TEXT,
    comment         TEXT,
    is_golden       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_edi_dataset ON eval_dataset_item(dataset_id);
CREATE INDEX IF NOT EXISTS idx_et_st       ON eval_task(status);
CREATE INDEX IF NOT EXISTS idx_er_task     ON eval_result(task_id);
