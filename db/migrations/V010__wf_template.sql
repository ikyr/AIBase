-- V010: workflow template
CREATE TABLE IF NOT EXISTS wf_template (
    id              VARCHAR(19)  PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    category        VARCHAR(50),
    dag             TEXT         NOT NULL,
    usage_count     INTEGER      NOT NULL DEFAULT 0,
    status          VARCHAR(50)  NOT NULL DEFAULT 'PUBLISHED',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);
