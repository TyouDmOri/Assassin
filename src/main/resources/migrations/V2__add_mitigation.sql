-- ASSASIN AntiCheat v1.0.0 — Add mitigation log table
-- Author: TyouDm

CREATE TABLE IF NOT EXISTS assasin_mitigations (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    violation_id INTEGER NOT NULL,
    strategy     TEXT    NOT NULL,
    result       TEXT    NOT NULL DEFAULT 'ok',
    timestamp_ms INTEGER NOT NULL,
    FOREIGN KEY (violation_id) REFERENCES assasin_violations(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_mitigations_violation ON assasin_mitigations(violation_id)
