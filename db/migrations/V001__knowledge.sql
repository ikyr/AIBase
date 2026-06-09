CREATE TABLE IF NOT EXISTS kb_config (
    id         VARCHAR(19)  PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    description TEXT,
    kb_type    VARCHAR(50)  NOT NULL DEFAULT 'PUBLIC',
    owner_id   VARCHAR(19),
    owner_dept_id VARCHAR(19),
    embedding_model VARCHAR(100),
    chunk_size   INTEGER NOT NULL DEFAULT 800,
    chunk_overlap INTEGER NOT NULL DEFAULT 100,
    status     VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    metadata   TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS kb_document (
    id          VARCHAR(19)  PRIMARY KEY,
    kb_id       VARCHAR(19)  NOT NULL REFERENCES kb_config(id),
    title       VARCHAR(500) NOT NULL,
    source_type VARCHAR(50)  NOT NULL,
    source_ref  TEXT,
    file_type   VARCHAR(50),
    file_size   BIGINT,
    status      VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    chunk_count INTEGER NOT NULL DEFAULT 0,
    checksum    VARCHAR(64),
    ingested_at TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS kb_chunk (
    id          VARCHAR(19)  PRIMARY KEY,
    doc_id      VARCHAR(19)  NOT NULL REFERENCES kb_document(id),
    kb_id       VARCHAR(19)  NOT NULL REFERENCES kb_config(id),
    chunk_index INTEGER NOT NULL DEFAULT 0,
    content     TEXT         NOT NULL,
    token_count INTEGER,
    vector_id   VARCHAR(100),
    metadata    TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_kb_doc_kb  ON kb_document(kb_id);
CREATE INDEX IF NOT EXISTS idx_kb_doc_st  ON kb_document(status);
CREATE INDEX IF NOT EXISTS idx_kb_chk_doc ON kb_chunk(doc_id);
CREATE INDEX IF NOT EXISTS idx_kb_chk_kb  ON kb_chunk(kb_id);
