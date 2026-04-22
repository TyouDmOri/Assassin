<!--
▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
█▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
    ASSASIN AntiCheat v1.0.0
    Mitigation-First Server-Side
    Target: Minecraft 1.21.11 "Mounts of Mayhem"
    Author: TyouDm
-->

<div align="center">

```
 ░█████╗░░██████╗░██████╗░█████╗░░██████╗██╗███╗░░██╗
 ██╔══██╗██╔════╝██╔════╝██╔══██╗██╔════╝██║████╗░██║
 ███████║╚█████╗░╚█████╗░███████║╚█████╗░██║██╔██╗██║
 ██╔══██║░╚═══██╗░╚═══██╗██╔══██║░╚═══██╗██║██║╚████║
 ██║░░██║██████╔╝██████╔╝██║░░██║██████╔╝██║██║░╚███║
 ╚═╝░░╚═╝╚═════╝░╚═════╝░╚═╝░░╚═╝╚═════╝╚═╝╚═╝░░╚══╝
```

**Mitigation-First Server-Side AntiCheat**  
*Target: Paper 1.21.11 "Mounts of Mayhem"*

![Version](https://img.shields.io/badge/version-1.0.0-8B0000?style=flat-square)
![Paper](https://img.shields.io/badge/Paper-1.21.11-8B0000?style=flat-square)
![Java](https://img.shields.io/badge/Java-21-8B0000?style=flat-square)
![License](https://img.shields.io/badge/license-Proprietary-8B0000?style=flat-square)

</div>

---

## 🩸 Overview

**ASSASIN** is a high-performance, mitigation-first server-side AntiCheat for Paper 1.21.11 "Mounts of Mayhem".  
Rather than banning on suspicion, ASSASIN silently corrects cheating behavior — setbacks, packet cancellation, velocity correction — making cheats ineffective without revealing detection.

**Author:** TyouDm  
**Version:** 1.0.0  
**License:** Proprietary — All Rights Reserved

---

## 🩸 Philosophy

> *"Don't ban. Mitigate. Make cheats useless."*

- **Mitigation-first** — setbacks, cancels, and velocity corrections before kicks/bans
- **Latency-aware** — every check is ping-compensated via transaction system
- **False-flag resistant** — extensive legit PvP technique whitelist (w-tap, jump-reset, butterfly click, elytra dive, etc.)
- **Performance-obsessed** — <0.1ms P99 per check, <1ms P99 per player/tick at 200 players
- **Transparent to staff** — rich alert system with hover details, Discord webhooks, and replay buffer

---

## 🩸 Features

### ~80 Checks across 7 categories

#### Movement (14 checks)
| Check | Description |
|-------|-------------|
| SpeedA | Horizontal speed violation (absolute threshold) |
| SpeedB | Friction-based movement prediction |
| FlyA | Gravity bypass detection |
| FlyB | Hover detection (near-zero Y velocity while airborne) |
| NoFallA | Fall damage bypass |
| JesusA | Water walking |
| StepA | Illegal step height |
| TimerA | Packet timer manipulation |
| PhaseA | Block phase-through (DDA collision) |
| StrafeA | Illegal strafe patterns |
| ElytraA | Elytra physics deviation (uses ElytraPredictor — never flags dive speed) |
| JumpResetA/B | Jump-reset cheat (false-flag safe: σ threshold + min samples) |
| MotionA | Illegal motion injection |

#### Mount (4 checks) — 1.21.11 "Mounts of Mayhem"
| Check | Description |
|-------|-------------|
| MountSpeedA | Mount speed violation (per-EntityType thresholds) |
| NautilusA | Aquatic mount speed abuse |
| ZombieHorseA | Untamed Zombie Horse exploit (new in 1.21.11) |
| MountFlyA | Mount fly bypass |

#### Combat (21 checks)
| Check | Description |
|-------|-------------|
| KillauraA-D | Rotation delta, multi-target, wall attack (DDA), angle difference |
| AimA-C | GCD yaw/pitch, sensitivity constant, pitch variance |
| ReachA-B | Ping-compensated distance, target rewind |
| HitboxA | AABB expansion |
| AutoClickerA-C | CPS variance (Welford), double-clicks, FFT radix-2 async |
| VelocityA-C | KB ratio horizontal/vertical, timing |
| CriticalsA | Fake critical hits |
| SpearA | Trident trajectory + cooldown |
| MaceDmgA-C | Mace damage scaling, density smash, cooldown bypass |
| AttributeSwapA | Attribute swap validator (informs VelocityA, MaceDmgA, AutoClickerA) |

#### World (9 checks)
| Check | Description |
|-------|-------------|
| ScaffoldA-C | Raytrace validation, godbridge bot detection, backward placement |
| TowerA | Tower hack via jump+place timing variance |
| NukerA | Multi-block break (>1/tick) |
| FastBreakA | Instant block break (precomputed break-time table) |
| FastPlaceA | Block placement rate violation |
| LiquidWalkA | Placement on liquid |
| AirPlaceA | Placement without support |

#### Player (16 checks)
| Check | Description |
|-------|-------------|
| InventoryA-B | Movement with inventory open, click without OPEN_WINDOW |
| BadPacketsA-F | NaN/Inf, out-of-bounds, extreme Y, invalid rotation, bad slot, duplicates |
| PostA | Action before teleport confirmation |
| CrashA | Crash packets (extreme position delta) |
| BookA | BOOK_EDIT payload >8KB |
| TimerPacketA | Overall packet rate violation |
| AutoTotemA-D | Reswap time, variance, no OPEN_WINDOW, multitasking |
| ChestStealerA | Chest click rate |
| AutoArmorA | Automatic armor equip |
| FastEatA | Eating speed bypass |

#### Macro Detection 🤖 (7 checks)
| Check | Description |
|-------|-------------|
| MacroSequenceA | N-gram Rabin-Karp sequence detection (trigrams + tetragrams) |
| MacroTimingA | Sub-human reaction time (<150ms) — disabled for high-ping |
| MacroVarianceA | Input interval variance (σ < 1.5ms, n≥20) |
| MacroInputA | 4+ actions in 1 tick (>3×/min) |
| MacroInventoryA | Auto-gapple/pot/soup/armor/chest-stealer FSM |
| MacroClickerA | FFT kurtosis + bimodal analysis (async) |
| MacroCorrelationA | Pearson r²>0.95 event→action correlation |

#### Misc (3 checks)
| Check | Description |
|-------|-------------|
| NameSpoofA | Invalid username characters |
| ClientBrandA | Spoofed/empty client brand |
| GhostHandA | Interact without prior arm swing |

---

## 🩸 Installation

1. Download `ASSASIN-1.0.0.jar` from releases
2. Place in your server's `plugins/` folder
3. Restart the server — config files are created automatically
4. Configure in `plugins/ASSASIN/config.yml`

**Requirements:**
- Paper 1.21.11 (or compatible fork)
- Java 21+
- PacketEvents 2.9.x (bundled via shadowJar — no separate install needed)

---

## 🩸 Commands

See [COMMANDS.md](COMMANDS.md) for the full reference.

| Command | Description |
|---------|-------------|
| `/assasin gui` | Open the main GUI |
| `/assasin info <player>` | View player check data |
| `/assasin vl <player> [check] [reset]` | View/reset violation levels |
| `/assasin alerts [on\|off]` | Toggle alert visibility |
| `/assasin logs <player> [page]` | View violation logs |
| `/assasin exempt <player> <type> <seconds>` | Exempt a player |
| `/assasin debug <player>` | Toggle debug mode |
| `/assasin reload [all]` | Reload configuration |
| `/assasin version` | Show version info |

---

## 🩸 Configuration

See [CONFIG.md](CONFIG.md) for the full reference.

| File | Description |
|------|-------------|
| `config.yml` | General settings, storage backend, thread pool |
| `checks.yml` | Per-check enable/disable and threshold tuning |
| `mitigation.yml` | Mitigation profiles and VL cascade definitions |
| `latency.yml` | Ping compensation settings |
| `alerts.yml` | Alert formats, Discord webhook, sounds |
| `messages.yml` | All user-facing messages (i18n-ready, MiniMessage) |
| `legit-techniques.yml` | Legit PvP technique tolerances |
| `macro.yml` | Macro detection strictness and whitelist |
| `gui.yml` | GUI layout slot assignments and materials |

---

## 🩸 Legit PvP Techniques

ASSASIN is designed to **never** false-flag legitimate PvP techniques:

- ✅ W-tap / S-tap / A/D-tap
- ✅ Jump-reset (legit, high σ — requires n≥8, σ≥1.5)
- ✅ Block-hit with shield (KB multiplier 0.5)
- ✅ Crit-tapping
- ✅ Butterfly / Jitter / Drag click (bimodal distribution)
- ✅ Attribute-swap between attacks (axe/sword/mace combo)
- ✅ Elytra dive (2 → 40 b/s in ~8s is vanilla — ElytraA only flags deviation)
- ✅ Riptide trident (20t exempt)
- ✅ Godbridge / Speed-bridge / Ninja-bridge (raytrace validated)
- ✅ Pearl phase (60t exempt)
- ✅ Legit totem-swap (σ>1.5, n≥5)

---

## 🩸 Performance

| Metric | Target |
|--------|--------|
| P99 per check | < 0.1ms |
| P99 per player/tick (200 players) | < 1ms |
| Async overhead | < 50µs |

Key optimizations:
- Ring buffers with primitive arrays — zero autoboxing in hot paths
- Early-exit on exempts/disabled checks (first line of every check)
- Lazy computation — GCD/FFT/raytrace only when preliminary suspicion
- Stateless design with granular `AtomicReference` fields
- `PacketType` enum switch — no `instanceof` in packet handlers
- DB writes, Discord, FFT offloaded to `AsyncProcessor` (custom `ForkJoinPool`)
- `distanceSq` instead of `sqrt` for reach checks

---

## 🩸 Architecture

```
ASSASIN
├── core/           ServiceContainer, ModuleRegistry, LegitTechniqueRegistry
├── config/         ConfigManager + 9 typed config classes (hot-reload)
├── data/
│   ├── tracker/    12 per-player trackers (movement, combat, macro, etc.)
│   └── prediction/ PhysicsConstants, CollisionEngine (DDA), MovementPredictor,
│                   MountPredictor, ElytraPredictor
├── latency/        TransactionManager, PingCompensator, LagCompensatedWorld,
│                   KnockbackValidator, TransactionBarrier, BucketedPingHistory
├── handler/
│   ├── packet/     10 PacketEvents handlers (movement, combat, inventory, etc.)
│   ├── event/      4 Bukkit event handlers (player, combat, world, mount)
│   └── async/      AsyncProcessor (ForkJoinPool, daemon threads)
├── check/
│   ├── impl/movement/   14 movement checks
│   ├── impl/mount/      4 mount checks
│   ├── impl/combat/     21 combat checks
│   ├── impl/world/      9 world checks
│   ├── impl/player/     16 player checks
│   ├── impl/macro/      7 macro checks
│   └── impl/misc/       3 misc checks
├── mitigation/     MitigationEngine, 10 strategies, ViolationBuffer, RateLimiter,
│                   ReplayBuffer (200 ticks)
├── storage/        SQLite/MySQL/MariaDB with HikariCP, 4 migrations, 6 models
├── alert/          AlertManager, AlertFormatter, DiscordWebhook, AlertContext
├── gui/            GuiManager, AssasinGui base, 9 screens, 4 components, 2 utils
└── command/        AssasinCommand (Brigadier) + 16 subcommands
```

---

## 🩸 Building

```bash
# Build the plugin JAR
./gradlew shadowJar

# Output: build/libs/ASSASIN-1.0.0.jar

# Run JMH benchmarks
./gradlew jmh
# Results: build/reports/jmh/results.json
```

**Requirements:**
- JDK 21+
- Gradle 8.x (wrapper included)

---

## 🩸 License

**Proprietary — All Rights Reserved**  
Copyright © TyouDm. Unauthorized distribution, modification, or use is prohibited.

---

<div align="center">

*ASSASIN AntiCheat v1.0.0 — by TyouDm*

</div>
