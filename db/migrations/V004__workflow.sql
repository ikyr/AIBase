CREATE TABLE IF NOT EXISTS wf_definition (
    id              VARCHAR(19)  PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    version         INTEGER NOT NULL DEFAULT 1,
    dag             TEXT,
    timeout_seconds INTEGER NOT NULL DEFAULT 300,
    retry_policy    TEXT,
    status          VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS wf_instance (
    id                  VARCHAR(19)  PRIMARY KEY,
    definition_id       VARCHAR(19)  NOT NULL REFERENCES wf_definition(id),
    definition_version  INTEGER,
    status              VARCHAR(50)  NOT NULL DEFAULT 'RUNNING',
    input               TEXT,
    output              TEXT,
    context             TEXT,
    started_at          TIMESTAMP,
    completed_at        TIMESTAMP,
    error_msg           TEXT,
    trace_id            VARCHAR(100),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_wfi_def   ON wf_instance(definition_id);
CREATE INDEX IF NOT EXISTS idx_wfi_st    ON wf_instance(status);
CREATE INDEX IF NOT EXISTS idx_wfi_start ON wf_instance(started_at);
