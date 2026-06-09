CREATE TABLE IF NOT EXISTS wf_node_exec (
    id              VARCHAR(19)  PRIMARY KEY,
    wf_exec_id      VARCHAR(19)  NOT NULL REFERENCES wf_instance(id),
    node_id         VARCHAR(100) NOT NULL,
    node_name       VARCHAR(200),
    node_type       VARCHAR(50)  NOT NULL,
    status          VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    input           TEXT,
    output          TEXT,
    error           TEXT,
    started_at      TIMESTAMP,
    finished_at     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_wfne_exec  ON wf_node_exec(wf_exec_id);
CREATE INDEX IF NOT EXISTS idx_wfne_st   ON wf_node_exec(status);
