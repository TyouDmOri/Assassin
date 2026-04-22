<!--
▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
█▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
    ASSASIN AntiCheat v1.0.0
    Mitigation-First Server-Side
    Target: Minecraft 1.21.11 "Mounts of Mayhem"
    Author: TyouDm
-->

# 🩸 ASSASIN — Development ToDo
**Author:** TyouDm  
**Version:** 1.0.0  
**Target:** Paper 1.21.11  
**Paradigm:** Mitigation-First Server-Side AntiCheat  
**License:** Propietaria (All Rights Reserved)
---

## 🩸 Legend
- 🔴 Pendiente — no iniciado
- 🟡 En progreso — activamente en desarrollo
- 🟢 Completo — implementado y testeado
- ⚠️ Bloqueado — espera dependencia
- 🧪 En testing — código listo, validando
---

## 📚 Índice de Fases
- **Fase 0** 🟢 — ToDo + documentación inicial (4 archivos)
- **Fase 1** 🟢 — Proyecto base + bootstrap (8 archivos) — depende: 0
- **Fase 2** � — Core: PlayerData, Exempt, Registry (12 archivos) — depende: 1
- **Fase 3** � — Latency subsystem (7 archivos) — depende: 2
- **Fase 4** � — Handlers + Trackers (22 archivos) — depende: 3
- **Fase 5** � — Predicción física (6 archivos) — depende: 4
- **Fase 6** � — Mitigation Engine + Strategies (16 archivos) — depende: 5
- **Fase 7** � — Checks Movement (14 archivos) — depende: 6
- **Fase 8** � — Checks Mount (4 archivos) — depende: 7
- **Fase 9** � — Checks Combat (21 archivos) — depende: 7
- **Fase 10** � — Checks World (9 archivos) — depende: 7
- **Fase 11** � — Checks Player (16 archivos) — depende: 7
- **Fase 12** � — Checks Macro 🤖 (10 archivos) — depende: 11
- **Fase 13** � — Checks Misc (4 archivos) — depende: 7
- **Fase 14** � — Storage SQLite/MySQL/MariaDB (15 archivos) — depende: 2
- **Fase 15** � — Alert Manager + Discord + Hover (6 archivos) — depende: 14
- **Fase 16** � — GUI completa (18 archivos) — depende: 15
- **Fase 17** � — Comandos Brigadier (17 archivos) — depende: 16
- **Fase 18** � — Configuración YAML + docs (12 archivos) — depende: 17
- **Fase 19** � — JMH benchmarks + optimización (10 archivos) — depende: 18
- **Fase 20** � — README + shadowJar final (3 archivos) — depende: 19
**Total estimado:** ~234 archivos
---

## � FASE 0 — Documentación inicial
- [x] Crear `ToDo.md` (este archivo) con todas las fases
- [x] Crear `README.md` preliminar con branding rojo sangre
- [x] Crear `CONFIG.md` (placeholder, se llenará en FASE 18)
- [x] Crear `COMMANDS.md` (placeholder, se llenará en FASE 17)
- [x] Verificar "Author: TyouDm" en header de los 4 archivos
- [x] Definir `.gitignore` y `.editorconfig`

Dependencias: ninguna  
Archivos: 4
---

## � FASE 1 — Proyecto base + bootstrap
- [x] `build.gradle.kts` con Kotlin DSL + shadowJar + paperweight-userdev
- [x] `settings.gradle.kts` con rootProject.name = "ASSASIN"
- [x] `gradle.properties` (paper 1.21.11, packetevents 2.9.x, java 21)
- [x] `src/main/resources/paper-plugin.yml` con `authors: [TyouDm]`
- [x] `AssasinPlugin.java` (main class: onLoad, onEnable, onDisable)
- [x] `AssasinColors.java` con paleta completa
- [x] `AssasinBootstrap.java` con ASCII banner ANSI rojo + "by TyouDm"
- [x] Estructura vacía de todos los paquetes base

Dependencias: FASE 0  
Archivos: 8
---

## 🟢 FASE 2 — Core (PlayerData, Exempt, Registry)
- [x] `core/ServiceContainer.java` (DI simple)
- [x] `core/ModuleRegistry.java`
- [x] `core/LegitTechniqueRegistry.java` 🛡️
- [x] `data/PlayerData.java` con AtomicReferences granulares
- [x] `data/PlayerDataManager.java` con ConcurrentHashMap
- [x] `exempt/ExemptManager.java`
- [x] `exempt/ExemptType.java` (enum con todos los tipos)
- [x] `util/RingBuffer.java` genérico + primitivos
- [x] `util/WelfordStats.java` (online mean + variance)
- [x] `util/RollingHash.java` (Rabin-Karp)
- [x] `util/FFT.java` (Cooley-Tukey radix-2 iterativa)
- [x] `util/MathUtil.java` con constantes precomputadas

Dependencias: FASE 1  
Archivos: 12
---

## � FASE 3 — Latency subsystem
- [x] `latency/TransactionManager.java` (IDs + timestamps + cola FIFO)
- [x] `latency/PingCompensator.java` (fórmulas por check)
- [x] `latency/LagCompensatedWorld.java` (RingBuffer posiciones 40 ticks)
- [x] `latency/KnockbackValidator.java`
- [x] `latency/TransactionBarrier.java` (tx pre/post setback)
- [x] `latency/BucketedPingHistory.java` (P50/P95/P99 últimos 30s)
- [x] Integración con `PlayerData.latencyTracker`

Dependencias: FASE 2  
Archivos: 7
---

## � FASE 4 — Handlers + Trackers

### Packet Handlers
- [x] `handler/HandlerManager.java` (registro central)
- [x] `handler/PacketHandler.java` (dispatcher — integrado en HandlerManager)
- [x] `handler/packet/MovementPacketHandler.java`
- [x] `handler/packet/CombatPacketHandler.java`
- [x] `handler/packet/BlockPacketHandler.java`
- [x] `handler/packet/RotationPacketHandler.java`
- [x] `handler/packet/MountPacketHandler.java`
- [x] `handler/packet/KeepAlivePacketHandler.java`
- [x] `handler/packet/TransactionPacketHandler.java`
- [x] `handler/packet/InventoryPacketHandler.java`
- [x] `handler/packet/VelocityPacketHandler.java` (outbound)

### Event Handlers
- [x] `handler/event/PlayerEventHandler.java` (join, quit, respawn, teleport)
- [x] `handler/event/CombatEventHandler.java` (damage, death, resurrect)
- [x] `handler/event/WorldEventHandler.java` (place, break, interact)
- [x] `handler/event/MountEventHandler.java` (mount, dismount)

### Trackers
- [x] `data/tracker/MovementTracker.java`
- [x] `data/tracker/RotationTracker.java`
- [x] `data/tracker/CombatTracker.java`
- [x] `data/tracker/VelocityTracker.java` (pending_kb + expected vector)
- [x] `data/tracker/LatencyTracker.java`
- [x] `data/tracker/BlockTracker.java` (collision cache por tick)
- [x] `data/tracker/MountTracker.java`
- [x] `data/tracker/AttackTracker.java`
- [x] `data/tracker/InputTracker.java` (sprint, sneak, jump toggles)
- [x] `data/tracker/InventoryTracker.java`
- [x] `data/tracker/ActionTracker.java` 🤖 (RingBuffer<Action>[64] central)
- [x] `data/tracker/MacroStateTracker.java` 🤖 (FSM por jugador)

### Async
- [x] `handler/async/AsyncProcessor.java` (ForkJoinPool custom + métricas)

Dependencias: FASE 3  
Archivos: 22
---

## 🟢 FASE 5 — Predicción física
- [x] `data/prediction/PhysicsConstants.java` (gravity, drag, friction, 
      base_speeds, MountPhysics Map<EntityType>)
- [x] `data/prediction/CollisionEngine.java` (AABB slab + voxel traversal)
- [x] `data/prediction/MovementPredictor.java` (simulación tick-a-tick vanilla)
- [x] `data/prediction/MountPredictor.java` (physics por EntityType)
- [x] `data/prediction/ElytraPredictor.java` 🪶 (dive acceleration, 
      firework boost, wall-bounce, RESET en transiciones)
- [ ] Tests físicos: dive 8s 2→40 b/s debe NO flaggear

Dependencias: FASE 4  
Archivos: 6
---

## � FASE 6 — Mitigation Engine + Strategies

### Core engine
- [x] `mitigation/MitigationEngine.java` (núcleo)
- [x] `mitigation/MitigationStrategy.java` (interface)
- [x] `mitigation/MitigationContext.java`
- [x] `mitigation/MitigationProfile.java` (cascadas por VL)
- [x] `mitigation/MitigationPriority.java` (enum)
- [x] `mitigation/MitigationResult.java`

### Strategies
- [x] `mitigation/strategy/SetbackStrategy.java` (soft + hard)
- [x] `mitigation/strategy/CancelPacketStrategy.java`
- [x] `mitigation/strategy/CancelDamageStrategy.java`
- [x] `mitigation/strategy/CancelBlockActionStrategy.java`
- [x] `mitigation/strategy/VelocityStrategy.java`
- [x] `mitigation/strategy/SlowStrategy.java`
- [x] `mitigation/strategy/DismountStrategy.java`
- [x] `mitigation/strategy/FreezeStrategy.java`
- [x] `mitigation/strategy/ResyncStrategy.java`
- [x] `mitigation/strategy/KickStrategy.java`

### Buffers
- [x] `mitigation/buffer/ViolationBuffer.java` (decay automático)
- [x] `mitigation/buffer/RateLimiter.java`
- [x] `mitigation/replay/ReplayBuffer.java` (últimos 200 ticks)

### Tests
- [ ] Tests integración cascadas completas
- [ ] Verificar thread-safety bajo carga

Dependencias: FASE 5  
Archivos: 16
---

## � FASE 7 — Checks Movement
- [x] `check/Check.java` (abstract base)
- [x] `check/CheckCategory.java` (enum)
- [x] `check/CheckType.java` (enum)
- [x] `check/CheckInfo.java` (annotation)
- [x] `check/impl/movement/SpeedA.java` (horizontal speed)
- [x] `check/impl/movement/SpeedB.java` (friction-based prediction)
- [x] `check/impl/movement/FlyA.java` (gravity)
- [x] `check/impl/movement/FlyB.java` (hover)
- [x] `check/impl/movement/NoFallA.java`
- [x] `check/impl/movement/JesusA.java`
- [x] `check/impl/movement/StepA.java`
- [x] `check/impl/movement/TimerA.java`
- [x] `check/impl/movement/PhaseA.java`
- [x] `check/impl/movement/StrafeA.java`
- [x] `check/impl/movement/ElytraA.java` 🪶 (usa ElytraPredictor)
- [x] `check/impl/movement/JumpResetA.java` 🛡️
- [x] `check/impl/movement/JumpResetB.java` 🛡️
- [x] `check/impl/movement/MotionA.java`

### Tests obligatorios
- [ ] W-tap, s-tap, a/d-tap → NO flag
- [ ] Jump-reset legit (σ alta) → NO flag
- [ ] Elytra dive 2→40 b/s → NO flag
- [ ] Block-hit con shield → NO flag VelocityA

Dependencias: FASE 6  
Archivos: 14 (+ tests)
---

## � FASE 8 — Checks Mount
- [x] `check/impl/mount/MountSpeedA.java`
- [x] `check/impl/mount/NautilusA.java` (montura acuática)
- [x] `check/impl/mount/ZombieHorseA.java` (untamed rideable 1.21.11)
- [x] `check/impl/mount/MountFlyA.java`

### Tests
- [ ] Velocidades vanilla de cada EntityType → NO flag
- [ ] Nautilus en agua vs fuera de agua

Dependencias: FASE 7  
Archivos: 4 (+ tests)
---

## � FASE 9 — Checks Combat

### Killaura
- [x] `check/impl/combat/KillauraA.java` (rotation delta pre-hit)
- [x] `check/impl/combat/KillauraB.java` (multi-target)
- [x] `check/impl/combat/KillauraC.java` (wall attack — DDA Amanatides-Woo)
- [x] `check/impl/combat/KillauraD.java` (angle difference)

### Aim
- [x] `check/impl/combat/AimA.java` (GCD yaw/pitch)
- [x] `check/impl/combat/AimB.java` (sensitivity constant)
- [x] `check/impl/combat/AimC.java` (pitch variance)

### Reach / Hitbox
- [x] `check/impl/combat/ReachA.java` (ping-compensated distance)
- [x] `check/impl/combat/ReachB.java` (target rewind)
- [x] `check/impl/combat/HitboxA.java` (AABB expansion)

### AutoClicker
- [x] `check/impl/combat/AutoClickerA.java` (CPS variance, Welford)
- [x] `check/impl/combat/AutoClickerB.java` (double-clicks artificiales)
- [x] `check/impl/combat/AutoClickerC.java` (FFT radix-2 n=32 async)

### Velocity
- [x] `check/impl/combat/VelocityA.java` (KB ratio horizontal)
- [x] `check/impl/combat/VelocityB.java` (KB ratio vertical)
- [x] `check/impl/combat/VelocityC.java` (KB timing)

### Damage-based
- [x] `check/impl/combat/CriticalsA.java` (fake crits)
- [x] `check/impl/combat/SpearA.java` 🆕 (trayectoria + cooldown)
- [x] `check/impl/combat/MaceDmgA.java` (damage scaling por altura)
- [x] `check/impl/combat/MaceDmgB.java` (density smash sin airtime)
- [x] `check/impl/combat/MaceDmgC.java` (cooldown bypass + wind charge)

### Validador
- [x] `check/impl/combat/AttributeSwapA.java` 🛡️ (informa a otros checks)

### Tests obligatorios
- [ ] Butterfly / jitter / drag click → NO flag
- [ ] Attribute-swap entre attacks → NO flag VelocityA/MaceDmgA
- [ ] Combo-reset multi-target con rotación → NO flag KillauraB
- [ ] Crit-tapping legit → NO flag CriticalsA

Dependencias: FASE 7  
Archivos: 21 (+ tests)
---
---

## 🟢 FASE 10 — Checks World
- [x] `check/impl/world/ScaffoldA.java` (rotation consistency)
- [x] `check/impl/world/ScaffoldB.java` (invalid angles / godbridge jitter)
- [x] `check/impl/world/ScaffoldC.java` (backward placement)
- [x] `check/impl/world/TowerA.java` (jump+place timing σ)
- [x] `check/impl/world/NukerA.java` (>1 bloque/tick no adyacentes)
- [x] `check/impl/world/FastBreakA.java` (tabla break_time precomputada)
- [x] `check/impl/world/FastPlaceA.java` (rate placement)
- [x] `check/impl/world/LiquidWalkA.java` (placement sobre líquido)
- [x] `check/impl/world/AirPlaceA.java` (placement sin soporte)

### Tests obligatorios
- [ ] Speed-bridge / ninja-bridge legit → NO flag
- [ ] Godbridge humano con jitter σ>0.1° → NO flag
- [ ] Jitter / telly bridge con raytrace válido → NO flag

Dependencias: FASE 7  
Archivos: 9 (+ tests)
---

## � FASE 11 — Checks Player

### Inventory
- [x] `check/impl/player/InventoryA.java` (move con inv abierto)
- [x] `check/impl/player/InventoryB.java` (click sin OPEN_WINDOW previo)

### BadPackets
- [x] `check/impl/player/BadPacketsA.java` (NaN/Infinity)
- [x] `check/impl/player/BadPacketsB.java` (valores fuera rango)
- [x] `check/impl/player/BadPacketsC.java` (pos.y > 1e7)
- [x] `check/impl/player/BadPacketsD.java` (rotación inválida)
- [x] `check/impl/player/BadPacketsE.java` (slot inválido)
- [x] `check/impl/player/BadPacketsF.java` (duplicados imposibles)

### Misc
- [x] `check/impl/player/PostA.java` (acción pre-teleport confirm)
- [x] `check/impl/player/CrashA.java` (packets crasher)
- [x] `check/impl/player/BookA.java` (BOOK_EDIT payload >8KB)
- [x] `check/impl/player/TimerPacketA.java` (rate packets/s)

### AutoTotem
- [x] `check/impl/player/AutoTotemA.java` (reswap time < 5 ticks)
- [x] `check/impl/player/AutoTotemB.java` (σ variance < 1.5)
- [x] `check/impl/player/AutoTotemC.java` (packet pattern sin OPEN_WINDOW)
- [x] `check/impl/player/AutoTotemD.java` (multitasking mismo tick)

### Combate pasivo
- [x] `check/impl/player/ChestStealerA.java` (WINDOW_CLICK <20ms barrido)
- [x] `check/impl/player/AutoArmorA.java` (armor swap post-damage)
- [x] `check/impl/player/FastEatA.java` (eat_time < vanilla)

### Tests obligatorios
- [ ] Totem reswap legit (σ>1.5, n≥5) → NO flag
- [ ] Inventario abierto sin moverse → NO flag InventoryA
- [ ] Swap F offhand → NO flag

Dependencias: FASE 7  
Archivos: 16 (+ tests)
---

## � FASE 12 — Checks Macro 🤖

### Detección
- [x] `check/impl/macro/MacroSequenceA.java` (n-gram Rabin-Karp trigramas/tetragramas)
- [x] `check/impl/macro/MacroTimingA.java` (reacción <150ms imposible humano)
- [x] `check/impl/macro/MacroVarianceA.java` (σ intervalos <1.5ms con n≥20)
- [x] `check/impl/macro/MacroInputA.java` (4+ acciones mismo tick consistente)
- [x] `check/impl/macro/MacroInventoryA.java` (auto-gapple, auto-pot, auto-soup, 
      auto-armor, chest-stealer — FSM por jugador)
- [x] `check/impl/macro/MacroClickerA.java` (FFT kurtosis + picos)
- [x] `check/impl/macro/MacroCorrelationA.java` (Pearson r²>0.95 event→action)

### Infraestructura
- [x] `data/tracker/MacroStateTracker.java` (hecho en FASE 4)
- [x] Enum `MacroState` (IDLE, DETECTING, CONFIRMED, EXEMPT) — en MacroStateTracker
- [x] `check/impl/macro/MacroAction.java` (enum: CLICK, SWAP, CROUCH, JUMP, 
      USE_ITEM, HOTBAR_KEY, WINDOW_CLICK)

### Mitigation profile macro (conservador)
- [x] VL 0-4 → NO_ACTION (solo log evidencia) — en MitigationEngine
- [x] VL 5-9 → SILENT_ALERT (staff only) — en MitigationEngine
- [x] VL 10-14 → CANCEL_PACKET input sospechoso + ALERT
- [x] VL 15-19 → CANCEL_PACKET + RESYNC inventario + ALERT
- [x] VL 20+ → KICK mensaje genérico

### Tests obligatorios
- [ ] Humano con hardware gaming consistente → NO flag
- [ ] Macro real secuencia σ<2ms → flag correcto
- [ ] Lag spike tps<18 → pausa checks 5s post-recovery
- [ ] High ping >300ms → desactiva MacroTimingA
- [ ] DEATH/RESPAWN → reset buffers
- [ ] Whitelist UUID respetada

### Integración
- [ ] MacroInventoryA coexiste con InventoryA (comparten InventoryTracker, 
      heurísticas distintas)
- [ ] Config macro.yml con strictness low/medium/high

Dependencias: FASE 11  
Archivos: 10 (+ tests)
---

## � FASE 13 — Checks Misc
- [x] `check/impl/misc/NameSpoofA.java` (nombre con caracteres inválidos)
- [x] `check/impl/misc/ClientBrandA.java` (brand spoofed / vacío)
- [x] `check/impl/misc/GhostHandA.java` (interact sin arm swing previo)
- [ ] Tests asociados

Dependencias: FASE 7  
Archivos: 4 (+ tests)
---

## � FASE 14 — Storage (SQLite / MySQL / MariaDB)

### Providers
- [x] `storage/StorageProvider.java` (interface)
- [x] `storage/AbstractSqlProvider.java` (prepared statements + batching)
- [x] `storage/SQLiteProvider.java` (default, zero-config)
- [x] `storage/MySQLProvider.java` (HikariCP)
- [x] `storage/MariaDBProvider.java` (HikariCP)
- [x] `storage/StorageFactory.java` (lee config.database.type)

### Migrations
- [x] `storage/migration/MigrationManager.java`
- [x] `storage/migration/V1__init.sql`
- [x] `storage/migration/V2__add_mitigation.sql`
- [x] `storage/migration/V3__add_alert_prefs.sql`
- [x] `storage/migration/V4__add_macro_log.sql`

### Models
- [x] `storage/model/ViolationRecord.java`
- [x] `storage/model/PlayerProfile.java`
- [x] `storage/model/MitigationLog.java`
- [x] `storage/model/AlertLog.java`
- [x] `storage/model/AlertPreference.java`
- [x] `storage/model/MacroEvidence.java`

### Configuración pool HikariCP
- [x] maxPoolSize 10, minIdle 2
- [x] connectionTimeout 5000ms, maxLifetime 1800000ms
- [x] leakDetectionThreshold 30000ms

### Tablas obligatorias
- [x] assasin_violations
- [x] assasin_players
- [x] assasin_mitigations
- [x] assasin_alerts
- [x] assasin_alert_preferences
- [x] assasin_macro_evidence

### Índices
- [x] idx_violations_uuid, idx_violations_timestamp, idx_violations_check
- [x] idx_macro_pattern_hash

### Tests
- [ ] Tests con SQLite in-memory
- [ ] Migrations up/down

Dependencias: FASE 2  
Archivos: 15 (+ tests)
---

## � FASE 15 — Alert Manager + Discord + Hover
- [x] `alert/AlertManager.java` (filtrado por preferencias GUI)
- [x] `alert/AlertFormatter.java` (MiniMessage + HoverEvent + ClickEvent)
- [x] `alert/DiscordWebhook.java` (async, embed color 0x8A0303, footer "by TyouDm")
- [x] `alert/AlertContext.java` (contexto inmutable de alerta)

### Placeholders soportados
- [x] player, check, vl, ping, tps, pos, world, details, severity

### Canales configurables por usuario
- [x] chat
- [x] actionbar
- [x] title
- [x] sound
- [x] discord

### Tests
- [ ] Hover renderiza correctamente
- [ ] ClickEvent ejecuta /assasin info {player}
- [ ] Discord webhook async no bloquea main thread

Dependencias: FASE 14  
Archivos: 6 (+ tests)

## 🟢 FASE 16 — GUI completa

### Core
- [x] `gui/GuiManager.java` (ConcurrentHashMap<UUID, AssasinGui>)
- [x] `gui/AssasinGui.java` (base class + InventoryHolder custom)

### Pantallas
- [x] `gui/screen/MainGui.java` (6 filas, categorías + utilidades + admin)
- [x] `gui/screen/CategoryGui.java` (submenú por categoría)
- [x] `gui/screen/AlertsToggleGui.java` (canales chat/actionbar/title/sound/discord)
- [x] `gui/screen/CheckManagerGui.java` (admin, paginado)
- [x] `gui/screen/ServerStatsGui.java` (TPS, flags 24h, top checks, mem/CPU)
- [x] `gui/screen/AboutGui.java` (Nether Star centrado, "Author: TyouDm")
- [x] `gui/screen/RecentFlagsGui.java` (últimas violaciones)
- [x] `gui/screen/AlertFormatGui.java` (Written Book con placeholders)
- [x] `gui/screen/BulkActionsGui.java` (enable/disable all, reset VLs)

### Components
- [x] `gui/component/GuiItem.java` (ItemStack + action + lore builder)
- [x] `gui/component/GuiAction.java` (Consumer<InventoryClickEvent>)
- [x] `gui/component/GuiBorder.java` (red glass panes)
- [x] `gui/component/PaginationBar.java`

### Util
- [x] `gui/util/GuiColors.java` (reuso AssasinColors)
- [x] `gui/util/ItemBuilder.java` (PersistentDataContainer + lore fluent)

### Persistencia
- [x] NamespacedKey "assasin:gui_action" + "assasin:check_name"
- [ ] Cache Caffeine preferencias expireAfterAccess 10min (FASE 18)
- [x] Tabla assasin_alert_preferences sincronizada al toggle

### Sonidos
- [x] UI_BUTTON_CLICK al click (0.5 vol)
- [x] BLOCK_NOTE_BLOCK_PLING al toggle ON
- [x] BLOCK_NOTE_BLOCK_BASS al toggle OFF

### Tests
- [ ] Clicks disparan actions correctas
- [ ] Items admin invisibles sin permiso (glass pane placeholder)
- [ ] Refresh diferencial (solo slots modificados)
- [ ] InventoryCloseEvent limpia GuiManager

Dependencias: FASE 15  
Archivos: 18 (+ tests)
---

## � FASE 17 — Comandos Brigadier

### Root
- [x] `command/AssasinCommand.java` (root con Paper Brigadier)

### Subcomandos
- [x] `command/subcommand/GuiSubCommand.java` → /assasin gui
- [x] `command/subcommand/AlertsSubCommand.java` → /assasin alerts [on|off]
- [x] `command/subcommand/InfoSubCommand.java` → /assasin info <player>
- [x] `command/subcommand/VlSubCommand.java` → /assasin vl <player> [check] [reset]
- [x] `command/subcommand/LogsSubCommand.java` → /assasin logs <player> [page]
- [x] `command/subcommand/ReplaySubCommand.java` → /assasin replay <player>
- [x] `command/subcommand/TestSubCommand.java` → /assasin test <check>
- [x] `command/subcommand/CheckSubCommand.java` → enable/disable/set
- [x] `command/subcommand/ExemptSubCommand.java` → /assasin exempt <player> <type> <seconds>
- [x] `command/subcommand/DebugSubCommand.java` → /assasin debug <player> [on|off]
- [x] `command/subcommand/ReloadSubCommand.java` → /assasin reload [config|messages|checks|all]
- [x] `command/subcommand/DbSubCommand.java` → status/migrate/backup/query
- [x] `command/subcommand/BanSubCommand.java`
- [x] `command/subcommand/KickSubCommand.java`
- [x] `command/subcommand/HelpSubCommand.java` (hover con categorías)
- [x] `command/subcommand/VersionSubCommand.java` → "ASSASIN v1.0.0 by TyouDm"

### Requisitos por subcomando
- [x] Permission: assasin.command.<subcommand>
- [x] Prefix rojo sangre en todos los outputs
- [x] HoverEvent.showText con detalles contextuales
- [x] ClickEvent suggestCommand/runCommand según contexto
- [x] Tab completion contextual (jugadores online, nombres de check, etc.)

### Tests
- [ ] Cada subcomando con permiso → ejecuta
- [ ] Sin permiso → mensaje rojo sangre "✖ No permission"
- [ ] Tab completion funcional
- [ ] `/assasin version` devuelve "by TyouDm"

Dependencias: FASE 16  
Archivos: 17 (+ tests)
---

## � FASE 18 — Configuración YAML + docs

### Archivos de config
- [x] `config.yml` (general, storage, threads, flags)
- [x] `checks.yml` (toggle + thresholds — todos los ~80 checks)
- [x] `mitigation.yml` (profiles + cascadas VL)
- [x] `latency.yml` (ping comp, transaction interval)
- [x] `alerts.yml` (formatos, webhook, sounds)
- [x] `messages.yml` (i18n-ready, todos los textos)
- [x] `legit-techniques.yml` (tolerancias PvP)
- [x] `macro.yml` (strictness, whitelist, thresholds)
- [x] `gui.yml` (layout slots, materiales)

### ConfigManager
- [x] `config/ConfigManager.java` (hot-reload + validation)
- [x] `config/CheckConfig.java`
- [x] `config/MessagesConfig.java`
- [x] `config/MitigationConfig.java`
- [x] `config/LatencyConfig.java`
- [x] `config/DatabaseConfig.java`
- [x] `config/AlertConfig.java`
- [x] `config/LegitConfig.java`
- [x] `config/MacroConfig.java`
- [x] `config/GuiConfig.java`

### Docs
- [x] Completar `CONFIG.md` (todas las keys documentadas con ejemplos)
- [x] Completar `COMMANDS.md` (todos los comandos con permisos y ejemplos)

### Comentarios en YAMLs
- [x] Cada key con comentario # explicando propósito
- [x] Ejemplos inline para valores complejos
- [x] Referencias a CONFIG.md

Dependencias: FASE 17  
Archivos: 12
---

## � FASE 19 — JMH benchmarks + optimización

### Benchmarks por categoría
- [x] `bench/MovementCheckBench.java`
- [x] `bench/CombatCheckBench.java`
- [x] `bench/MacroCheckBench.java`
- [x] `bench/FftBench.java` (radix-2 n=32 vs n=64)
- [x] `bench/RingBufferBench.java`
- [x] `bench/PredictorBench.java` (MovementPredictor + ElytraPredictor)
- [x] `bench/MitigationEngineBench.java`
- [x] `bench/StorageBench.java` (insert batching)

### Targets
- [ ] <0.1ms P99 por check individual
- [ ] <1ms P99 por jugador/tick con 200 jugadores simulados
- [ ] Async overhead <50µs

### Profiling
- [ ] Profiling con async-profiler + flamegraph
- [ ] Verificar cero autoboxing en hot paths (JIT log)
- [ ] Verificar branch prediction correcta (condición común primero)

Dependencias: FASE 18  
Archivos: 10
---

## � FASE 20 — README + shadowJar final
- [x] `README.md` completo con ASCII banner rojo sangre, features, 
      instalación, comandos, config, "Author: TyouDm"
- [x] shadowJar configurado con relocations (hikari, caffeine, packetevents)
- [x] Verificación final de "TyouDm" en TODOS los sitios:
    - [x] paper-plugin.yml authors
    - [x] build.gradle.kts header comment
    - [x] JavaDoc @author en TODAS las clases públicas
    - [x] ASCII banner al load (AssasinBootstrap)
    - [x] README.md header + footer
    - [x] ToDo.md (este archivo)
    - [x] CONFIG.md header
    - [x] COMMANDS.md header
    - [x] /assasin version → "ASSASIN v1.0.0 by TyouDm"
    - [x] /assasin help → "by TyouDm" footer
    - [x] GUI About (Nether Star lore)
    - [x] Discord webhook embed footer "ASSASIN AntiCheat v1.0.0 • by TyouDm"
- [x] `CHANGELOG.md` inicial con v1.0.0

Dependencias: FASE 19  
Archivos: 3
---

# 🛡️ Legit PvP Techniques Whitelist

Técnicas que NO deben causar false-flags. Cada una validada con tests en FASE 7/9/10/11.
- **W-TAP** — SPRINT OFF→ON ≤3t alrededor de ATTACK. Exempt VelocityA 5t.
- **S-TAP** — backward input ≤2t + re-sprint. Predictor re-baseline.
- **A/D-TAP** — oscilación lateral con yaw estable. StrafeA tolera Δ≤0.15.
- **JUMP-RESET legit** — jump Y≈0.42 ≤1t pre-damage. JumpResetA/B solo 
  flag con σ<1.5 y éxito ≥95% en n≥8.
- **BLOCK-HIT** — USE_ITEM shield activo. VelocityA/B: expected_kb * 0.5.
- **CRIT-TAPPING** — jumps rítmicos sync attack cooldown. CriticalsA 
  valida onGround=false + motionY<0.
- **SPEED-BRIDGE / NINJA / JITTER / MOON / TELLY** — Scaffold valida 
  raytrace real (face + pos), NO rotación aislada.
- **BUTTERFLY / JITTER / DRAG CLICK** — distribución bimodal humana, 
  kurtosis alta. AutoClicker flaggea por baja varianza + baja kurtosis + 
  FFT pico único.
- **ATTRIBUTE-SWAP** — HELD_ITEM_CHANGE entre ATTACK. AttributeSwapA 
  informa a VelocityA, MaceDmgA, AutoClickerA.
- **AXE/SWORD/MACE combo** — cooldown exacto por arma. AutoClicker 
  segmenta por arma activa.
- **COMBO-RESET** — multi-target con rotación pre-hit. KillauraB solo 
  flag si Δyaw>180°/tick o hits simultáneos sin rotación.
- **PEARL-PHASE** — TELEPORT event. Exempt PEARL 3s.
- **LEGIT TOTEM-SWAP** — reswap ≥5t, σ>1.5, sin multitasking simultáneo.
- **OFFHAND-SWAP (F)** — SWAP_HANDS permitido salvo en estados imposibles 
  (death screen, spectator).
- **ELYTRA / FIREWORK BOOST / WALL-BOUNCE / SUPER-BOOST** — 
  ExemptType.ELYTRA_BOOST 20t tras firework.
- **RIPTIDE** — trident + agua/lluvia. Exempt RIPTIDE 20t.
- **GODBRIDGE** — rotación pitch ~80° fija. ScaffoldB exige σ>0.1° jitter 
  humano (cambios instantáneos sub-tick → cheat).
- **FAST-BRIDGE con sneak toggle rápido** — TimerA solo flag desync de 
  movimiento, no sneak toggles.
---

# ⚡ Efficiency Notes

Optimizaciones clave obligatorias por subsistema.

### Reglas universales
- EARLY-EXIT primera línea (exempts/disabled/prereqs)
- Ring buffers fijos con arrays primitivos + índice circular (NO LinkedList)
- Primitivos (double/long/int), cero autoboxing en hot paths
- Lazy computation (GCD/FFT/raytrace solo con sospecha preliminar)
- Stateless: PlayerData con AtomicReferences granulares
- Math.* estándar (JIT intrinsics) en hot paths
- Cache local de variables antes del loop
- Branch prediction: condiciones común → raro
- PacketType enum switch (NO instanceof)
- Offload async: DB, Discord, FFT → AsyncProcessor

### Por check crítico
- **KillauraC** — Voxel DDA (Amanatides-Woo), NO step 0.01. Máx ~12 iter.
- **AimA** — GCD Euclidean longs cada 40t con buffer≥32, NO cada packet.
- **AutoClickerC / MacroClickerA** — FFT radix-2 n=32 async SOLO si 
  σ preliminar sospechosa.
- **MacroSequenceA** — Rolling hash Rabin-Karp O(1) por input. 
  HashMap<long,int> con Caffeine ttl 30s.
- **VelocityA/B/C** — pending_kb guardado al envío SET_ENTITY_VELOCITY. 
  Ratio O(1) al llegar PLAYER_FLYING.
- **ReachA** — DistanceSq (evita sqrt), threshold² precomputado.
- **FastBreakA** — HashMap<Material+Tool+Enchant,long> precomputado al load.
- **MountSpeedA** — switch EntityType (JIT), static final Map<MountPhysics>.
- **ElytraPredictor** — RESET en transiciones, tolerancia acumulativa con 
  ticks de vuelo, ring buffer 20 velocities.

### Objetivo
- <0.1ms P99 por check individual
- <1ms P99 por jugador/tick con 200 jugadores
---

# 🤖 Macro Detection Strategy

Los macros son secuencias automatizadas de inputs LEGALES con timing 
sobrehumano. Requieren categoría propia (no son movement cheats).

### Filosofía conservadora
- **VL 0-4** → NO_ACTION (log interno, acumula evidencia)
- **VL 5-9** → SILENT_ALERT (solo staff, no avisa jugador)
- **VL 10-14** → CANCEL_PACKET input sospechoso + ALERT
- **VL 15-19** → CANCEL_PACKET + RESYNC inv + ALERT
- **VL 20+** → KICK con mensaje genérico
**Razón:** macro-user pierde la ventaja (ritmo roto, cooldown desincronizado) 
sin que el AntiCheat revele que fue detectado → más difícil de adaptar.

### Tipos de macro cubiertos
- **MacroSequenceA** — n-gram Rabin-Karp, trigramas/tetragramas con Δt ±2ms
- **MacroTimingA** — reacción neuronal <150ms imposible humano
- **MacroVarianceA** — σ intervalos <1.5ms con n≥20 (humano 15-40ms)
- **MacroInputA** — 4+ acciones en 1 tick (50ms) >3 veces/min
- **MacroInventoryA** — FSM para auto-gapple/auto-pot/auto-soup/auto-armor/
  chest-stealer
- **MacroClickerA** — FFT kurtosis baja + picos definidos (humano: bimodal)
- **MacroCorrelationA** — Pearson r²>0.95 correlación event→action (totem, 
  shield break, arrow hit)

### Anti false-flag
- Hardware gaming (Razer, Logitech) puede ser consistente → n≥20 inv, n≥30 clicks
- Whitelist UUID en macro.yml (streamers/torneos)
- HIGH_PING>300ms desactiva MacroTimingA (jitter red confunde)
- LAG_SPIKE tps<18 pausa todos 5s post-recovery
- DEATH/RESPAWN resetea buffers
- Config strictness low|medium|high (low: n≥50, r²>0.98)
---

# 🪶 Elytra Physics Notes

La elytra NO tiene velocidad terminal. Motion acumula tick a tick. En dive 
un jugador pasa de 2 b/s a +40 b/s en ~8s → **NO es cheat, es vanilla**.

### Constantes
- `GRAVITY_ELYTRA = 0.08`
- `HORIZONTAL_DRAG = 0.99`
- `VERTICAL_DRAG = 0.98`

### Fórmula (Mojang simplificada)
1. `lookVec = dirVector(pitch, yaw)`
2. `motion.y -= GRAVITY * (0.5 + 0.5 * min(1, -lookVec.y * 10))`
3. fallBonus si motion.y<0 && hLookLen>0
4. diveBonus si lookVec.y<0 → acelera progresivamente
5. Redirección hacia look
6. Drag final

### Consumidores de ElytraPredictor
- **ElytraA** — flag solo por desviación persistente ≥12t
- **SpeedA/B** — delegan a ElytraPredictor si elytra_active
- **FlyA/B** — DESHABILITADOS durante elytra (es fly legítimo)
- **NoFallA** — no dispara durante landing correcto

### Exempts asociados
- `ELYTRA_ACTIVE` — mientras deployed
- `ELYTRA_BOOST 20t` — tras firework use
- `ELYTRA_WALL_BOUNCE 5t` — tras colisión lateral
- `ELYTRA_TAKEOFF 10t` — transición ground→air
- `ELYTRA_LANDING 15t` — transición air→ground

### RESET del predictor
- Takeoff, landing, firework use, wall collision
**Regla crítica:** ElytraA NO flaggea por "velocidad alta". Solo por 
desviación persistente vs `ElytraPredictor.predict()` acumulada ≥12 ticks.
---

# ❓ Open Questions
- [ ] ¿Soportar Folia (regions schedulers) en v1.0.0 o v1.1.0?
- [ ] ¿Integración ViaVersion para clientes 1.20.x conectando a 1.21.11?
- [ ] ¿Webhook Discord por canal de severidad (info/warn/critical)?
- [ ] ¿Sistema de appeals in-game con GUI para baneados?
- [ ] ¿Exportar evidencia de macros como archivo JSON adjuntable a reports?
- [ ] ¿Integración con BedrockConnect/Geyser (jugadores Bedrock)?
- [ ] ¿ML opcional con modelo ONNX pre-entrenado para detección macro 
      avanzada (fase 2.0)?
- [ ] ¿Replay visual 3D exportable (.mcreplay o similar)?
---

<!-- 
═══════════════════════════════════════════════════════════════════════════
  ASSASIN AntiCheat v1.0.0 — Mitigation-First Server-Side
  Author: TyouDm
  All Rights Reserved
═══════════════════════════════════════════════════════════════════════════
-->