<!--
▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
█▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
    ASSASIN AntiCheat v1.0.0
    Mitigation-First Server-Side
    Target: Minecraft 1.21.11 "Mounts of Mayhem"
    Author: TyouDm
-->

# 🩸 ASSASIN — Changelog

All notable changes to ASSASIN AntiCheat are documented here.

---

## [1.0.0] — Initial Release

**Author:** TyouDm  
**Target:** Paper 1.21.11 "Mounts of Mayhem"

### Added

#### Core Infrastructure
- `ServiceContainer` — dependency-ordered module lifecycle (20 phases)
- `ModuleRegistry` — LIFO enable/disable with error isolation
- `LegitTechniqueRegistry` — 18 legit PvP technique tolerances (configurable)
- `PlayerData` — per-player state with granular `AtomicReference` fields
- `PlayerDataManager` — `ConcurrentHashMap` with lock-free reads

#### Latency Subsystem
- `TransactionManager` — FIFO queue, RTT measurement via keep-alive echo
- `BucketedPingHistory` — P50/P95/P99 over 600-tick rolling window
- `LagCompensatedWorld` — 40-tick position history for target rewind
- `KnockbackValidator` — pending KB vector with expiry window
- `TransactionBarrier` — setback confirmation with force-close protection
- `PingCompensator` — per-category compensation formulas (reach, velocity, movement)

#### Handlers & Trackers (Fase 4)
- 10 PacketEvents packet handlers (movement, rotation, combat, block, mount, keepalive, transaction, inventory, velocity)
- 4 Bukkit event handlers (player lifecycle, combat, world, mount)
- 12 per-player trackers: MovementTracker, RotationTracker, CombatTracker, VelocityTracker, LatencyTracker, BlockTracker, MountTracker, AttackTracker, InputTracker, InventoryTracker, ActionTracker, MacroStateTracker
- `AsyncProcessor` — custom `ForkJoinPool` with daemon threads and metrics

#### Physics Prediction (Fase 5)
- `PhysicsConstants` — all vanilla 1.21.11 constants + 13 mount types
- `CollisionEngine` — AABB slab test + Amanatides-Woo DDA voxel traversal
- `MovementPredictor` — tick-accurate vanilla movement simulation
- `MountPredictor` — per-EntityType speed thresholds
- `ElytraPredictor` — full Mojang elytra formula with firework boost support

#### Mitigation Engine (Fase 6)
- `MitigationEngine` — profile-based cascade execution with thread routing
- 10 strategies: SetbackSoft/Hard, CancelPacket, CancelDamage, CancelBlockAction, Velocity, Slow, Dismount, Freeze, Resync, Kick
- `ViolationBuffer` — exponential decay, per-check VL tracking
- `RateLimiter` — token-bucket for mitigation spam prevention
- `ReplayBuffer` — 200-tick position history for staff review
- 9 default profiles: soft, medium, hard, combat, world, mount, velocity, macro, badpackets

#### Checks (~80 total)
- **Movement (14):** SpeedA/B, FlyA/B, NoFallA, JesusA, StepA, TimerA, PhaseA, StrafeA, ElytraA, JumpResetA/B, MotionA
- **Mount (4):** MountSpeedA, NautilusA, ZombieHorseA (1.21.11), MountFlyA
- **Combat (21):** KillauraA-D, AimA-C, ReachA-B, HitboxA, AutoClickerA-C, VelocityA-C, CriticalsA, SpearA, MaceDmgA-C, AttributeSwapA
- **World (9):** ScaffoldA-C, TowerA, NukerA, FastBreakA, FastPlaceA, LiquidWalkA, AirPlaceA
- **Player (16):** InventoryA-B, BadPacketsA-F, PostA, CrashA, BookA, TimerPacketA, AutoTotemA-D, ChestStealerA, AutoArmorA, FastEatA
- **Macro (7):** MacroSequenceA (Rabin-Karp), MacroTimingA, MacroVarianceA, MacroInputA, MacroInventoryA (FSM), MacroClickerA (FFT), MacroCorrelationA (Pearson r²)
- **Misc (3):** NameSpoofA, ClientBrandA, GhostHandA

#### Storage (Fase 14)
- SQLite (default, zero-config), MySQL, MariaDB backends via HikariCP
- 4 schema migrations (V1–V4)
- 6 models: ViolationRecord, PlayerProfile, MitigationLog, AlertLog, AlertPreference, MacroEvidence
- `StorageFactory` — reads `config.yml storage.type`

#### Alert System (Fase 15)
- `AlertManager` — 5-channel delivery (chat, action bar, title, sound, Discord)
- `AlertFormatter` — Adventure components with HoverEvent + ClickEvent
- `DiscordWebhook` — async HTTP POST with severity-colored embeds, footer "by TyouDm"
- Per-staff preference persistence in `assasin_alert_preferences`

#### GUI (Fase 16)
- 9 screens: MainGui, CategoryGui, AlertsToggleGui, CheckManagerGui, ServerStatsGui, AboutGui, RecentFlagsGui, AlertFormatGui (Written Book), BulkActionsGui
- `GuiManager` — `ConcurrentHashMap<UUID, AssasinGui>` with event routing
- `GuiBorder`, `GuiItem`, `GuiAction`, `PaginationBar`, `ItemBuilder`, `GuiColors`

#### Commands (Fase 17)
- `/assasin` root with aliases `/ac`, `/anticheat` via Paper Brigadier
- 16 subcommands: gui, alerts, info, vl, logs, replay, test, check, exempt, debug, reload, db, ban, kick, help, version
- All outputs use blood-red prefix, HoverEvent details, contextual tab completion

#### Configuration (Fase 18)
- 9 YAML config files with inline `#` comments
- `ConfigManager` — hot-reload, copies defaults from JAR
- 9 typed config classes: CheckConfig, DatabaseConfig, AlertConfig, LatencyConfig, LegitConfig, MacroConfig, MitigationConfig, MessagesConfig, GuiConfig
- Full `CONFIG.md` and `COMMANDS.md` documentation

#### Benchmarks (Fase 19)
- 8 JMH benchmarks: RingBuffer, FFT (n=32 vs n=64), MovementCheck, CombatCheck, MacroCheck, Predictor (elytra 8s dive), MitigationEngine, Storage
- `me.champeau.jmh:0.7.2` Gradle plugin
- Results output to `build/reports/jmh/results.json`

### Technical highlights
- Zero autoboxing in all hot paths (primitive ring buffers, `long`/`double` everywhere)
- Lazy FFT/GCD/raytrace — only computed when preliminary suspicion is detected
- Elytra dive (2→40 b/s in ~8s) correctly modelled — never false-flags
- Conservative macro detection: VL 0-4 = silent log, kick only at VL 20+
- All checks exempt during: GAMEMODE, DEAD, BYPASS, RESPAWN, WORLD_CHANGE, setback barrier

---

*ASSASIN AntiCheat v1.0.0 — by TyouDm*
