<!--
▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
█▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
    ASSASIN AntiCheat v1.0.0
    Mitigation-First Server-Side
    Target: Minecraft 1.21.11 "Mounts of Mayhem"
    Author: TyouDm
-->

# 🩸 ASSASIN — Commands Reference

**Author:** TyouDm  
**Version:** 1.0.0

---

## 📚 Root Command

```
/assasin [subcommand] [args...]
```

**Aliases:** `/ac`, `/anticheat`  
**Base permission:** `assasin.command`

---

## 🎮 Subcommands

| Subcommand | Syntax | Permission | Description |
|------------|--------|------------|-------------|
| `gui` | `/assasin gui` | `assasin.command.gui` | Open the main GUI |
| `alerts` | `/assasin alerts [on\|off]` | `assasin.command.alerts` | Toggle alert visibility |
| `info` | `/assasin info <player>` | `assasin.command.info` | View player check data |
| `vl` | `/assasin vl <player> [check] [reset]` | `assasin.command.vl` | View/reset violation levels |
| `logs` | `/assasin logs <player> [page]` | `assasin.command.logs` | View player violation logs |
| `replay` | `/assasin replay <player>` | `assasin.command.replay` | View replay buffer |
| `test` | `/assasin test <check>` | `assasin.command.test` | Test a specific check |
| `check` | `/assasin check <name> <enable\|disable\|set>` | `assasin.command.check` | Manage checks |
| `exempt` | `/assasin exempt <player> <type> <seconds>` | `assasin.command.exempt` | Exempt a player |
| `debug` | `/assasin debug <player> [on\|off]` | `assasin.command.debug` | Toggle debug mode |
| `reload` | `/assasin reload [config\|messages\|checks\|all]` | `assasin.command.reload` | Reload configuration |
| `db` | `/assasin db <status\|migrate\|backup\|query>` | `assasin.command.db` | Database management |
| `ban` | `/assasin ban <player> [reason]` | `assasin.command.ban` | Ban a player |
| `kick` | `/assasin kick <player> [reason]` | `assasin.command.kick` | Kick a player |
| `help` | `/assasin help` | `assasin.command.help` | Show command reference |
| `version` | `/assasin version` | `assasin.command.version` | Show version info |

---

## 🔑 Permission Nodes

| Permission | Description | Default |
|------------|-------------|---------|
| `assasin.command` | Access to base command | op |
| `assasin.command.*` | Access to all subcommands | op |
| `assasin.command.gui` | Open GUI | op |
| `assasin.command.alerts` | Toggle alerts | op |
| `assasin.command.info` | View player info | op |
| `assasin.command.vl` | View/reset VLs | op |
| `assasin.command.logs` | View logs | op |
| `assasin.command.replay` | View replay | op |
| `assasin.command.test` | Test checks | op |
| `assasin.command.check` | Manage checks | op |
| `assasin.command.exempt` | Exempt players | op |
| `assasin.command.debug` | Debug mode | op |
| `assasin.command.reload` | Reload config | op |
| `assasin.command.db` | Database management | op |
| `assasin.command.ban` | Ban players | op |
| `assasin.command.kick` | Kick players | op |
| `assasin.command.help` | Show help | op |
| `assasin.command.version` | Show version | op |
| `assasin.alerts` | Receive alert notifications | op |
| `assasin.bypass` | Bypass all checks | false |
| `assasin.bypass.<check>` | Bypass a specific check | false |
| `assasin.admin` | Access admin GUI sections | op |

---

## 📖 Examples

```bash
# Open the main GUI
/assasin gui

# View violation levels for a player
/assasin vl Steve

# Reset a specific check's VL for a player
/assasin vl Steve SpeedA reset

# Exempt a player from speed checks for 60 seconds
/assasin exempt Steve STAFF_EXEMPT 60

# Enable debug output for a player
/assasin debug Steve on

# Reload all configuration files
/assasin reload all

# Show version
/assasin version
# Output: ASSASIN v1.0.0 by TyouDm

# View last 10 violations for a player (page 1)
/assasin logs Steve

# View page 2
/assasin logs Steve 2

# Ban a player with reason
/assasin ban Steve "Cheating detected by ASSASIN"

# Database status
/assasin db status
```

---

## 🔍 Exempt Types

Valid values for `/assasin exempt <player> <type> <seconds>`:

| Type | Description |
|------|-------------|
| `TELEPORT_PENDING` | Suppress checks during teleport |
| `SETBACK` | Suppress after setback |
| `RESPAWN` | Suppress after respawn |
| `WORLD_CHANGE` | Suppress during world change |
| `VELOCITY` | Suppress velocity checks |
| `ELYTRA_ACTIVE` | Suppress during elytra flight |
| `RIPTIDE` | Suppress during riptide |
| `PEARL` | Suppress PhaseA after pearl |
| `VEHICLE` | Suppress movement checks on mount |
| `STAFF_EXEMPT` | General staff exemption |
| `BYPASS` | Bypass all checks |
| `HIGH_PING` | Suppress latency-sensitive checks |
| `LAG_SPIKE` | Suppress all checks after lag spike |

---

<!-- 
═══════════════════════════════════════════════════════════════════════════
  ASSASIN AntiCheat v1.0.0 — Mitigation-First Server-Side
  Author: TyouDm
  All Rights Reserved
═══════════════════════════════════════════════════════════════════════════
-->
