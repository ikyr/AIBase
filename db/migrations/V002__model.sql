CREATE TABLE IF NOT EXISTS model_config (
    id          VARCHAR(19)  PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    provider    VARCHAR(50)  NOT NULL,
    endpoint    VARCHAR(500),
    api_key_ref VARCHAR(100),
    max_tokens  INTEGER,
    capabilities TEXT,
    priority    INTEGER NOT NULL DEFAULT 0,
    status      VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS model_route_rule (
    id               VARCHAR(19)  PRIMARY KEY,
    name             VARCHAR(200) NOT NULL,
    model_id         VARCHAR(19)  NOT NULL REFERENCES model_config(id),
    match_expression TEXT,
    priority         INTEGER NOT NULL DEFAULT 0,
    fallback_model_id VARCHAR(19),
    status           VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS model_call_log (
    id            VARCHAR(19)  PRIMARY KEY,
    model_name    VARCHAR(200) NOT NULL,
    caller_ref    VARCHAR(200),
    input_tokens  INTEGER,
    output_tokens INTEGER,
    duration_ms   BIGINT,
    cost          VARCHAR(50),
    call_status   VARCHAR(50),
    error_msg     TEXT,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by    VARCHAR(100),
    updated_by    VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_mcl_model   ON model_call_log(model_name);
CREATE INDEX IF NOT EXISTS idx_mcl_caller  ON model_call_log(caller_ref);
CREATE INDEX IF NOT EXISTS idx_mrr_active  ON model_route_rule(status, priority);
