CREATE TABLE IF NOT EXISTS agent_def (
    id                VARCHAR(19)  PRIMARY KEY,
    name              VARCHAR(200) NOT NULL,
    description       TEXT,
    system_prompt     TEXT,
    model             VARCHAR(100),
    tools             TEXT,
    skill_ids         TEXT,
    kb_ids            TEXT,
    coordination_mode VARCHAR(50),
    constraints       TEXT,
    status            VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by        VARCHAR(100),
    updated_by        VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS agent_session (
    id           VARCHAR(19)  PRIMARY KEY,
    agent_id     VARCHAR(19)  NOT NULL REFERENCES agent_def(id),
    user_id      VARCHAR(100),
    title        VARCHAR(500),
    status       VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    context      TEXT,
    trace_id     VARCHAR(100),
    started_at   TIMESTAMP,
    completed_at TIMESTAMP,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS agent_message (
    id           VARCHAR(19)  PRIMARY KEY,
    session_id   VARCHAR(19)  NOT NULL REFERENCES agent_session(id),
    parent_id    VARCHAR(19),
    role         VARCHAR(50)  NOT NULL,
    content      TEXT,
    content_type VARCHAR(50)  NOT NULL DEFAULT 'TEXT',
    attachments  TEXT,
    tool_calls   TEXT,
    token_count  INTEGER,
    metadata     TEXT,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_as_agent   ON agent_session(agent_id);
CREATE INDEX IF NOT EXISTS idx_as_start   ON agent_session(started_at);
CREATE INDEX IF NOT EXISTS idx_am_session ON agent_message(session_id);
