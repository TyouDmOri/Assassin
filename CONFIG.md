<!--
▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
█▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
    ASSASIN AntiCheat v1.0.0
    Mitigation-First Server-Side
    Target: Minecraft 1.21.11 "Mounts of Mayhem"
    Author: TyouDm
-->

# 🩸 ASSASIN — Configuration Reference

**Author:** TyouDm  
**Version:** 1.0.0  
**Target:** Paper 1.21.11

> All config files live in `plugins/ASSASIN/`. They are created automatically on first run with commented defaults.

---

## 📚 Config Files

| File | Description |
|------|-------------|
| `config.yml` | General settings, storage backend, thread pool |
| `checks.yml` | Per-check enable/disable and threshold tuning |
| `mitigation.yml` | Mitigation profiles and VL cascade definitions |
| `latency.yml` | Ping compensation and transaction interval settings |
| `alerts.yml` | Alert formats, Discord webhook URL, sounds |
| `messages.yml` | All user-facing messages (i18n-ready, MiniMessage) |
| `legit-techniques.yml` | Legit PvP technique tolerances |
| `macro.yml` | Macro detection strictness and whitelist |
| `gui.yml` | GUI layout slot assignments and materials |

---

## ⚙️ config.yml

### `general`
| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `name` | String | `"ASSASIN"` | Plugin display name |
| `log-level` | String | `"INFO"` | Log level: INFO, WARNING, SEVERE |
| `debug` | Boolean | `false` | Verbose debug logging (performance impact) |

### `storage`
| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `type` | String | `"sqlite"` | Backend: `sqlite` \| `mysql` \| `mariadb` |
| `host` | String | `"localhost"` | MySQL/MariaDB host |
| `port` | Integer | `3306` | MySQL/MariaDB port |
| `database` | String | `"assasin"` | Database name |
| `username` | String | `"root"` | Database username |
| `password` | String | `""` | Database password |
| `pool.max-pool-size` | Integer | `10` | HikariCP max connections |
| `pool.min-idle` | Integer | `2` | HikariCP min idle connections |
| `pool.connection-timeout` | Long | `5000` | ms — max wait for connection |
| `pool.max-lifetime` | Long | `1800000` | ms — max connection lifetime (30 min) |
| `pool.leak-detection-threshold` | Long | `30000` | ms — log if connection held > 30s |

### `threads`
| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `async-parallelism` | Integer | `0` | AsyncProcessor threads (0 = auto) |

### `flags`
| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `global-decay-rate` | Double | `0.05` | VL decay per tick (all checks) |
| `default-max-vl` | Double | `10.0` | Default max VL per check |

---

## 🔍 checks.yml

Each check has the following structure:

```yaml
checks:
  SpeedA:
    enabled: true        # Enable/disable this check
    max-vl: 10.0         # VL cap before mitigation escalates
    decay-rate: 0.05     # VL decay per tick (optional, overrides global)
    # check-specific thresholds...
```

**Check-specific thresholds** are documented inline in the file with `#` comments.

---

## 🛡️ mitigation.yml

### Profiles
Each profile maps VL thresholds to strategy lists:

```yaml
profiles:
  medium:
    cascades:
      1.0: [CANCEL_PACKET, SETBACK_SOFT]
      8.0: [FREEZE]
      15.0: [KICK]
```

**Available strategies:**
`SETBACK_SOFT`, `SETBACK_HARD`, `CANCEL_PACKET`, `CANCEL_DAMAGE`, `CANCEL_BLOCK`,
`VELOCITY_ZERO`, `SLOW`, `DISMOUNT`, `FREEZE`, `RESYNC`, `KICK`

### `check-profiles`
Maps check names to profile names. Checks not listed use `"medium"`.

```yaml
check-profiles:
  SpeedA: medium
  FlyA: hard
  MacroSequenceA: macro
```

---

## ⏱️ latency.yml

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `ping-compensation.max-compensated-ping-ms` | Integer | `300` | Ping ceiling for compensation |
| `ping-compensation.reach-per-100ms` | Double | `0.05` | Extra reach blocks per 100ms ping |
| `ping-compensation.velocity-ticks-per-50ms` | Integer | `1` | Extra KB window ticks per 50ms |
| `transaction.interval-ticks` | Integer | `20` | Ticks between RTT measurements |
| `transaction.max-barrier-ticks` | Integer | `40` | Max ticks a setback barrier stays open |
| `lag-compensation.history-ticks` | Integer | `40` | Position history for target rewind |
| `lag-compensation.lag-spike-tps` | Double | `18.0` | TPS below which lag spike is detected |
| `lag-compensation.post-spike-suppress-ticks` | Integer | `5` | Ticks to suppress checks after spike |

---

## 🔔 alerts.yml

### `format`
| Key | Default | Placeholders |
|-----|---------|-------------|
| `chat` | `"{player} failed {check} (VL: {vl}/{maxvl})"` | player, check, vl, maxvl, ping, tps, world, x, y, z, time, details |
| `action-bar` | `"{player} » {check} VL:{vl}"` | same |
| `title` | `"{player} » {check}"` | same |
| `subtitle` | `"VL: {vl} \| ping: {ping}ms"` | same |

### `discord`
| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `webhook` | String | `""` | Discord webhook URL (empty = disabled) |
| `min-vl-for-discord` | Double | `5.0` | Minimum VL to send Discord alert |

### `sounds`
| Key | Default | Description |
|-----|---------|-------------|
| `alert` | `"UI_BUTTON_CLICK"` | Sound on alert receive |
| `toggle-on` | `"BLOCK_NOTE_BLOCK_PLING"` | GUI toggle ON sound |
| `toggle-off` | `"BLOCK_NOTE_BLOCK_BASS"` | GUI toggle OFF sound |

---

## 💬 messages.yml

All messages use **MiniMessage** format. Supported tags: `<red>`, `<bold>`, `<color:#RRGGBB>`, etc.

The `prefix` key is prepended to all messages automatically.

---

## 🛡️ legit-techniques.yml

Each technique entry:

```yaml
techniques:
  WTAP:
    exempt-ticks: 5        # Ticks to suppress related checks
    kb-multiplier: 1.0     # KB multiplier when active (0.5 = half KB)
  JUMP_RESET:
    min-samples: 8         # Minimum samples before flagging
    sigma-threshold: 1.5   # σ below this = suspicious
    success-rate-limit: 0.95
```

---

## 🤖 macro.yml

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `strictness` | String | `"medium"` | `low` \| `medium` \| `high` |
| `whitelist` | List | `[]` | UUID list exempt from macro checks |
| `disable-timing-above-ping` | Integer | `300` | Disable MacroTimingA above this ping |
| `post-lag-spike-suppress-ticks` | Integer | `5` | Suppress after lag spike |

**Strictness levels:**

| Level | min-samples | min-r-squared | min-sigma-ms |
|-------|-------------|---------------|--------------|
| low | 50 | 0.98 | 2.0 |
| medium | 20 | 0.95 | 1.5 |
| high | 10 | 0.90 | 1.0 |

---

## 🖥️ gui.yml

Slot assignments and materials for all GUI screens. See `gui.md` for full layout documentation.

---

<!-- 
═══════════════════════════════════════════════════════════════════════════
  ASSASIN AntiCheat v1.0.0 — Mitigation-First Server-Side
  Author: TyouDm
  All Rights Reserved
═══════════════════════════════════════════════════════════════════════════
-->
