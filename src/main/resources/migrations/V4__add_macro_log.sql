-- ASSASIN AntiCheat v1.0.0 — Add macro evidence table
-- Author: TyouDm

CREATE TABLE IF NOT EXISTS assasin_macro_evidence (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    uuid          TEXT    NOT NULL,
    pattern_hash  INTEGER NOT NULL,
    occurrences   INTEGER NOT NULL DEFAULT 1,
    avg_delta_ms  REAL    NOT NULL DEFAULT 0,
    std_dev       REAL    NOT NULL DEFAULT 0,
    last_seen     INTEGER NOT NULL,
    evidence_json TEXT    NOT NULL DEFAULT '{}',
    UNIQUE (uuid, pattern_hash)
);

CREATE INDEX IF NOT EXISTS idx_macro_uuid         ON assasin_macro_evidence(uuid);
CREATE INDEX IF NOT EXISTS idx_macro_pattern_hash ON assasin_macro_evidence(pattern_hash)
