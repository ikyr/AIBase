CREATE TABLE IF NOT EXISTS prompt_version (
    id         VARCHAR(19)  PRIMARY KEY,
    ref_type   VARCHAR(50)  NOT NULL,
    ref_id     VARCHAR(19)  NOT NULL,
    version    INTEGER NOT NULL DEFAULT 1,
    content    TEXT,
    changelog  TEXT,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    status     VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS approval_record (
    id         VARCHAR(19)  PRIMARY KEY,
    type       VARCHAR(50)  NOT NULL,
    ref_type   VARCHAR(50),
    ref_id     VARCHAR(19),
    ref_name   VARCHAR(500),
    requester  VARCHAR(100),
    approvers  TEXT,
    status     VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    reason     TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_pv_ref    ON prompt_version(ref_type, ref_id);
CREATE INDEX IF NOT EXISTS idx_pv_curr   ON prompt_version(ref_id, is_current);
CREATE INDEX IF NOT EXISTS idx_ar_status ON approval_record(status);
