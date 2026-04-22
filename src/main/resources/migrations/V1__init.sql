-- ASSASIN AntiCheat v1.0.0 — Initial schema
-- Author: TyouDm

CREATE TABLE IF NOT EXISTS assasin_players (
    uuid             TEXT    NOT NULL PRIMARY KEY,
    name             TEXT    NOT NULL,
    first_join       INTEGER NOT NULL,
    last_join        INTEGER NOT NULL,
    total_violations INTEGER NOT NULL DEFAULT 0,
    banned           INTEGER NOT NULL DEFAULT 0,
    ban_reason       TEXT    NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS assasin_violations (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    uuid                TEXT    NOT NULL,
    check_name          TEXT    NOT NULL,
    vl                  REAL    NOT NULL,
    timestamp_ms        INTEGER NOT NULL,
    ping_ms             INTEGER NOT NULL DEFAULT 0,
    tps                 REAL    NOT NULL DEFAULT 20.0,
    world               TEXT    NOT NULL DEFAULT 'world',
    x                   REAL    NOT NULL DEFAULT 0,
    y                   REAL    NOT NULL DEFAULT 0,
    z                   REAL    NOT NULL DEFAULT 0,
    mitigation_applied  TEXT    NOT NULL DEFAULT '',
    data_json           TEXT    NOT NULL DEFAULT '{}'
);

CREATE INDEX IF NOT EXISTS idx_violations_uuid      ON assasin_violations(uuid);
CREATE INDEX IF NOT EXISTS idx_violations_timestamp ON assasin_violations(timestamp_ms);
CREATE INDEX IF NOT EXISTS idx_violations_check     ON assasin_violations(check_name)
