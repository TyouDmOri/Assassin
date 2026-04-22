-- ASSASIN AntiCheat v1.0.0 — Add alert log and preferences tables
-- Author: TyouDm

CREATE TABLE IF NOT EXISTS assasin_alerts (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    staff_uuid   TEXT,
    player_uuid  TEXT    NOT NULL,
    check_name   TEXT    NOT NULL,
    vl           REAL    NOT NULL,
    timestamp_ms INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_alerts_player    ON assasin_alerts(player_uuid);
CREATE INDEX IF NOT EXISTS idx_alerts_timestamp ON assasin_alerts(timestamp_ms);

CREATE TABLE IF NOT EXISTS assasin_alert_preferences (
    uuid             TEXT    NOT NULL,
    check_name       TEXT    NOT NULL DEFAULT '*',
    enabled          INTEGER NOT NULL DEFAULT 1,
    channels_bitmask INTEGER NOT NULL DEFAULT 11,
    PRIMARY KEY (uuid, check_name)
)
