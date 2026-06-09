CREATE TABLE IF NOT EXISTS skill_def (
    id             VARCHAR(19)  PRIMARY KEY,
    name           VARCHAR(200) NOT NULL,
    description    TEXT,
    tags           TEXT,
    skill_level    VARCHAR(50),
    prompt_template TEXT,
    params         TEXT,
    input_schema   TEXT,
    output_schema  TEXT,
    execution_mode VARCHAR(50),
    timeout_ms     INTEGER,
    agent_ref_id   VARCHAR(19),
    status         VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS skill_version (
    id         VARCHAR(19)  PRIMARY KEY,
    skill_id   VARCHAR(19)  NOT NULL REFERENCES skill_def(id),
    version    VARCHAR(50)  NOT NULL,
    changelog  TEXT,
    definition TEXT,
    is_latest  BOOLEAN NOT NULL DEFAULT FALSE,
    status     VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS skill_execution_log (
    id            VARCHAR(19)  PRIMARY KEY,
    skill_id      VARCHAR(19)  NOT NULL,
    skill_version VARCHAR(50),
    session_id    VARCHAR(19),
    input         TEXT,
    output        TEXT,
    status        VARCHAR(50)  NOT NULL DEFAULT 'RUNNING',
    duration_ms   INTEGER,
    error_msg     TEXT,
    trace_id      VARCHAR(100),
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_skv_skill ON skill_version(skill_id);
CREATE INDEX IF NOT EXISTS idx_skv_latest ON skill_version(skill_id, is_latest);
CREATE INDEX IF NOT EXISTS idx_sel_skill ON skill_execution_log(skill_id);
