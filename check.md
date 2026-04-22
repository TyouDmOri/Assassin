<!--
▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
█▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
    ASSASIN AntiCheat v1.0.0
    Mitigation-First Server-Side
    Target: Minecraft 1.21.11 "Mounts of Mayhem"
    Author: TyouDm
-->

# 🩸 ASSASIN — Master Document
**Author:** TyouDm  
**Version:** 1.0.0  
**Target:** Paper 1.21.11  
**Paradigm:** Mitigation-First Server-Side AntiCheat  
**License:** Propietaria (All Rights Reserved)

> Este documento es la **fuente única de verdad**: plan de desarrollo + 
> catálogo de checks + logging + config + comandos. El único archivo 
> complementario es `README.md` (portada pública).
---

## 🩸 Legend
- 🔴 Pendiente — no iniciado
- 🟡 En progreso — activamente en desarrollo
- 🟢 Completo — implementado y testeado
- ⚠️ Bloqueado — espera dependencia
- 🧪 En testing — código listo, validando
**Severidad de checks:** 🟢 Baja · 🟡 Media · 🟠 Alta · 🔴 Crítica
---

## 📚 Tabla de Contenidos

### Plan de desarrollo
- [Fase 0 — Documentación inicial](#fase-0)
- [Fase 1 — Proyecto base + bootstrap](#fase-1)
- [Fase 2 — Core (PlayerData, Exempt, Registry)](#fase-2)
- [Fase 3 — Latency subsystem](#fase-3)
- [Fase 4 — Handlers + Trackers](#fase-4)
- [Fase 5 — Predicción física](#fase-5)
- [Fase 6 — Mitigation Engine + Strategies](#fase-6)
- [Fase 7 — Checks Movement](#fase-7)
- [Fase 8 — Checks Mount](#fase-8)
- [Fase 9 — Checks Combat](#fase-9)
- [Fase 10 — Checks World](#fase-10)
- [Fase 11 — Checks Player](#fase-11)
- [Fase 12 — Checks Macro 🤖](#fase-12)
- [Fase 13 — Checks Misc](#fase-13)
- [Fase 14 — Storage SQLite/MySQL/MariaDB](#fase-14)
- [Fase 15 — Alert Manager + Discord](#fase-15)
- [Fase 16 — GUI completa](#fase-16)
- [Fase 17 — Comandos Brigadier](#fase-17)
- [Fase 18 — Configuración YAML](#fase-18)
- [Fase 19 — JMH Benchmarks](#fase-19)
- [Fase 20 — README + shadowJar final](#fase-20)

### Referencias operativas
- [🛡️ Legit PvP Techniques Whitelist](#legit)
- [⚡ Efficiency Notes](#efficiency)
- [🤖 Macro Detection Strategy](#macro-strategy)
- [🪶 Elytra Physics Notes](#elytra)
- [📖 Check Catalog (~80 checks)](#checks)
- [📜 Logging Specification](#logging)
- [⚙️ Config Reference](#config)
- [🎮 Commands Reference](#commands)
- [❓ Open Questions](#open)
**Total archivos estimados:** ~234
---

<a id="fase-0"></a>
## 🔴 FASE 0 — Documentación inicial
- [ ] Crear `ToDo.md` (este archivo, master document)
- [ ] Crear `README.md` con banner rojo sangre + "Author: TyouDm"
- [ ] Definir `.gitignore` y `.editorconfig`
- [ ] Verificar "Author: TyouDm" en header del ToDo.md y README.md
**Dependencias:** ninguna · **Archivos:** 2
---

<a id="fase-1"></a>
## 🔴 FASE 1 — Proyecto base + bootstrap
- [ ] `build.gradle.kts` con Kotlin DSL + shadowJar + paperweight-userdev
- [ ] `settings.gradle.kts` con rootProject.name = "ASSASIN"
- [ ] `gradle.properties` (paper 1.21.11, packetevents 2.9.x, java 21)
- [ ] `src/main/resources/paper-plugin.yml` con `authors: [TyouDm]`
- [ ] `AssasinPlugin.java` (main class: onLoad, onEnable, onDisable)
- [ ] `AssasinColors.java` con paleta completa rojo sangre
- [ ] `AssasinBootstrap.java` con ASCII banner ANSI rojo + "by TyouDm"
- [ ] Estructura vacía de todos los paquetes base
**Dependencias:** FASE 0 · **Archivos:** 8
---

<a id="fase-2"></a>
## 🔴 FASE 2 — Core (PlayerData, Exempt, Registry)
- [ ] `core/ServiceContainer.java` (DI simple)
- [ ] `core/ModuleRegistry.java`
- [ ] `core/LegitTechniqueRegistry.java` 🛡️
- [ ] `data/PlayerData.java` con AtomicReferences granulares
- [ ] `data/PlayerDataManager.java` con ConcurrentHashMap
- [ ] `exempt/ExemptManager.java`
- [ ] `exempt/ExemptType.java` (enum con todos los tipos)
- [ ] `util/RingBuffer.java` genérico + primitivos
- [ ] `util/WelfordStats.java` (online mean + variance)
- [ ] `util/RollingHash.java` (Rabin-Karp)
- [ ] `util/FFT.java` (Cooley-Tukey radix-2 iterativa)
- [ ] `util/MathUtil.java` con constantes precomputadas
**Dependencias:** FASE 1 · **Archivos:** 12
---

<a id="fase-3"></a>
## 🔴 FASE 3 — Latency subsystem
- [ ] `latency/TransactionManager.java` (IDs + timestamps + cola FIFO)
- [ ] `latency/PingCompensator.java` (fórmulas por check)
- [ ] `latency/LagCompensatedWorld.java` (RingBuffer posiciones 40 ticks)
- [ ] `latency/KnockbackValidator.java`
- [ ] `latency/TransactionBarrier.java` (tx pre/post setback)
- [ ] `latency/BucketedPingHistory.java` (P50/P95/P99 últimos 30s)
- [ ] Integración con `PlayerData.latencyTracker`
**Dependencias:** FASE 2 · **Archivos:** 7
---

<a id="fase-4"></a>
## 🔴 FASE 4 — Handlers + Trackers

### Packet Handlers
- [ ] `handler/HandlerManager.java` (registro central)
- [ ] `handler/PacketHandler.java` (dispatcher)
- [ ] `handler/packet/MovementPacketHandler.java`
- [ ] `handler/packet/CombatPacketHandler.java`
- [ ] `handler/packet/BlockPacketHandler.java`
- [ ] `handler/packet/RotationPacketHandler.java`
- [ ] `handler/packet/MountPacketHandler.java`
- [ ] `handler/packet/KeepAlivePacketHandler.java`
- [ ] `handler/packet/TransactionPacketHandler.java`
- [ ] `handler/packet/InventoryPacketHandler.java`

### Event Handlers
- [ ] `handler/event/PlayerEventHandler.java` (join, quit, respawn, teleport)
- [ ] `handler/event/CombatEventHandler.java` (damage, death, resurrect)
- [ ] `handler/event/WorldEventHandler.java` (place, break, interact)
- [ ] `handler/event/MountEventHandler.java` (mount, dismount)

### Trackers
- [ ] `data/tracker/MovementTracker.java`
- [ ] `data/tracker/RotationTracker.java`
- [ ] `data/tracker/CombatTracker.java`
- [ ] `data/tracker/VelocityTracker.java` (pending_kb + expected vector)
- [ ] `data/tracker/LatencyTracker.java`
- [ ] `data/tracker/BlockTracker.java` (collision cache por tick)
- [ ] `data/tracker/MountTracker.java`
- [ ] `data/tracker/AttackTracker.java`
- [ ] `data/tracker/InputTracker.java` (sprint, sneak, jump toggles)
- [ ] `data/tracker/InventoryTracker.java`
- [ ] `data/tracker/ActionTracker.java` 🤖 (RingBuffer<Action>[64] central)
- [ ] `data/tracker/MacroStateTracker.java` 🤖 (FSM por jugador)

### Async
- [ ] `handler/async/AsyncProcessor.java` (ForkJoinPool custom + métricas)
**Dependencias:** FASE 3 · **Archivos:** 22
---

<a id="fase-5"></a>
## 🔴 FASE 5 — Predicción física
- [ ] `data/prediction/PhysicsConstants.java` (gravity, drag, friction, 
      base_speeds, MountPhysics Map<EntityType>)
- [ ] `data/prediction/CollisionEngine.java` (AABB slab + voxel traversal)
- [ ] `data/prediction/MovementPredictor.java` (simulación tick-a-tick vanilla)
- [ ] `data/prediction/MountPredictor.java` (physics por EntityType)
- [ ] `data/prediction/ElytraPredictor.java` 🪶 (dive acceleration, 
      firework boost, wall-bounce, RESET en transiciones)
- [ ] Tests físicos: dive 8s 2→40 b/s debe NO flaggear
**Dependencias:** FASE 4 · **Archivos:** 6
---

<a id="fase-6"></a>
## 🔴 FASE 6 — Mitigation Engine + Strategies

### Core engine
- [ ] `mitigation/MitigationEngine.java` (núcleo)
- [ ] `mitigation/MitigationStrategy.java` (interface)
- [ ] `mitigation/MitigationContext.java`
- [ ] `mitigation/MitigationProfile.java` (cascadas por VL)
- [ ] `mitigation/MitigationPriority.java` (enum)
- [ ] `mitigation/MitigationResult.java`

### Strategies
- [ ] `mitigation/strategy/SetbackStrategy.java` (soft + hard)
- [ ] `mitigation/strategy/CancelPacketStrategy.java`
- [ ] `mitigation/strategy/CancelDamageStrategy.java`
- [ ] `mitigation/strategy/CancelBlockActionStrategy.java`
- [ ] `mitigation/strategy/VelocityStrategy.java`
- [ ] `mitigation/strategy/SlowStrategy.java`
- [ ] `mitigation/strategy/DismountStrategy.java`
- [ ] `mitigation/strategy/FreezeStrategy.java`
- [ ] `mitigation/strategy/ResyncStrategy.java`
- [ ] `mitigation/strategy/KickStrategy.java`

### Buffers
- [ ] `mitigation/buffer/ViolationBuffer.java` (decay automático)
- [ ] `mitigation/buffer/RateLimiter.java`
- [ ] `mitigation/replay/ReplayBuffer.java` (últimos 200 ticks)

### Tests
- [ ] Tests integración cascadas completas
- [ ] Verificar thread-safety bajo carga
**Dependencias:** FASE 5 · **Archivos:** 16
---

<a id="fase-7"></a>
## 🔴 FASE 7 — Checks Movement
- [ ] `check/Check.java` (abstract base)
- [ ] `check/CheckCategory.java` (enum)
- [ ] `check/CheckType.java` (enum)
- [ ] `check/CheckInfo.java` (annotation con descripción para JavaDoc)
- [ ] `check/impl/movement/SpeedA.java`
- [ ] `check/impl/movement/SpeedB.java`
- [ ] `check/impl/movement/FlyA.java`
- [ ] `check/impl/movement/FlyB.java`
- [ ] `check/impl/movement/NoFallA.java`
- [ ] `check/impl/movement/JesusA.java`
- [ ] `check/impl/movement/StepA.java`
- [ ] `check/impl/movement/TimerA.java`
- [ ] `check/impl/movement/PhaseA.java`
- [ ] `check/impl/movement/StrafeA.java`
- [ ] `check/impl/movement/ElytraA.java` 🪶
- [ ] `check/impl/movement/JumpResetA.java` 🛡️
- [ ] `check/impl/movement/JumpResetB.java` 🛡️
- [ ] `check/impl/movement/MotionA.java`

### Tests obligatorios
- [ ] W-tap, s-tap, a/d-tap → NO flag
- [ ] Jump-reset legit (σ alta) → NO flag
- [ ] Elytra dive 2→40 b/s → NO flag
- [ ] Block-hit con shield → NO flag VelocityA

> 📖 Ver [Check Catalog § Movement](#cat-movement) para specs detalladas.
**Dependencias:** FASE 6 · **Archivos:** 14 (+ tests)
---

<a id="fase-8"></a>
## 🔴 FASE 8 — Checks Mount
- [ ] `check/impl/mount/MountSpeedA.java`
- [ ] `check/impl/mount/NautilusA.java` (montura acuática)
- [ ] `check/impl/mount/ZombieHorseA.java` (untamed rideable 1.21.11)
- [ ] `check/impl/mount/MountFlyA.java`

### Tests
- [ ] Velocidades vanilla de cada EntityType → NO flag
- [ ] Nautilus en agua vs fuera de agua

> 📖 Ver [Check Catalog § Mount](#cat-mount) para specs detalladas.
**Dependencias:** FASE 7 · **Archivos:** 4 (+ tests)
---

<a id="fase-9"></a>
## 🔴 FASE 9 — Checks Combat

### Killaura
- [ ] `check/impl/combat/KillauraA.java` (rotation delta pre-hit)
- [ ] `check/impl/combat/KillauraB.java` (multi-target)
- [ ] `check/impl/combat/KillauraC.java` (wall attack — DDA Amanatides-Woo)
- [ ] `check/impl/combat/KillauraD.java` (angle difference)

### Aim
- [ ] `check/impl/combat/AimA.java` (GCD yaw/pitch)
- [ ] `check/impl/combat/AimB.java` (sensitivity constant)
- [ ] `check/impl/combat/AimC.java` (pitch variance)

### Reach / Hitbox
- [ ] `check/impl/combat/ReachA.java` (ping-compensated distance)
- [ ] `check/impl/combat/ReachB.java` (target rewind)
- [ ] `check/impl/combat/HitboxA.java` (AABB expansion)

### AutoClicker
- [ ] `check/impl/combat/AutoClickerA.java` (CPS variance, Welford)
- [ ] `check/impl/combat/AutoClickerB.java` (double-clicks artificiales)
- [ ] `check/impl/combat/AutoClickerC.java` (FFT radix-2 n=32 async)

### Velocity
- [ ] `check/impl/combat/VelocityA.java` (KB ratio horizontal)
- [ ] `check/impl/combat/VelocityB.java` (KB ratio vertical)
- [ ] `check/impl/combat/VelocityC.java` (KB timing)

### Damage-based
- [ ] `check/impl/combat/CriticalsA.java` (fake crits)
- [ ] `check/impl/combat/SpearA.java` 🆕
- [ ] `check/impl/combat/MaceDmgA.java` (scaling por altura)
- [ ] `check/impl/combat/MaceDmgB.java` (density smash sin airtime)
- [ ] `check/impl/combat/MaceDmgC.java` (cooldown bypass + wind charge)

### Validador
- [ ] `check/impl/combat/AttributeSwapA.java` 🛡️

### Tests obligatorios
- [ ] Butterfly / jitter / drag click → NO flag
- [ ] Attribute-swap entre attacks → NO flag VelocityA/MaceDmgA
- [ ] Combo-reset multi-target con rotación → NO flag KillauraB
- [ ] Crit-tapping legit → NO flag CriticalsA

> 📖 Ver [Check Catalog § Combat](#cat-combat) para specs detalladas.
**Dependencias:** FASE 7 · **Archivos:** 21 (+ tests)
---

<a id="fase-10"></a>
## 🔴 FASE 10 — Checks World
- [ ] `check/impl/world/ScaffoldA.java` (rotation consistency)
- [ ] `check/impl/world/ScaffoldB.java` (invalid angles / godbridge jitter)
- [ ] `check/impl/world/ScaffoldC.java` (backward placement)
- [ ] `check/impl/world/TowerA.java` (jump+place timing σ)
- [ ] `check/impl/world/NukerA.java` (>1 bloque/tick no adyacentes)
- [ ] `check/impl/world/FastBreakA.java` (tabla break_time precomputada)
- [ ] `check/impl/world/FastPlaceA.java` (rate placement)
- [ ] `check/impl/world/LiquidWalkA.java` (placement sobre líquido)
- [ ] `check/impl/world/AirPlaceA.java` (placement sin soporte)

### Tests obligatorios
- [ ] Speed-bridge / ninja-bridge legit → NO flag
- [ ] Godbridge humano con jitter σ>0.1° → NO flag
- [ ] Jitter / telly bridge con raytrace válido → NO flag

> 📖 Ver [Check Catalog § World](#cat-world) para specs detalladas.
**Dependencias:** FASE 7 · **Archivos:** 9 (+ tests)
---

<a id="fase-11"></a>
## 🔴 FASE 11 — Checks Player

### Inventory
- [ ] `check/impl/player/InventoryA.java` (move con inv abierto)
- [ ] `check/impl/player/InventoryB.java` (click sin OPEN_WINDOW previo)

### BadPackets
- [ ] `check/impl/player/BadPacketsA.java` (NaN/Infinity)
- [ ] `check/impl/player/BadPacketsB.java` (valores fuera rango)
- [ ] `check/impl/player/BadPacketsC.java` (pos.y > 1e7)
- [ ] `check/impl/player/BadPacketsD.java` (rotación inválida)
- [ ] `check/impl/player/BadPacketsE.java` (slot inválido)
- [ ] `check/impl/player/BadPacketsF.java` (duplicados imposibles)

### Misc
- [ ] `check/impl/player/PostA.java` (acción pre-teleport confirm)
- [ ] `check/impl/player/CrashA.java` (packets crasher)
- [ ] `check/impl/player/BookA.java` (BOOK_EDIT payload >8KB)
- [ ] `check/impl/player/TimerPacketA.java` (rate packets/s)

### AutoTotem
- [ ] `check/impl/player/AutoTotemA.java` (reswap time < 5 ticks)
- [ ] `check/impl/player/AutoTotemB.java` (σ variance < 1.5)
- [ ] `check/impl/player/AutoTotemC.java` (packet pattern sin OPEN_WINDOW)
- [ ] `check/impl/player/AutoTotemD.java` (multitasking mismo tick)

### Combate pasivo
- [ ] `check/impl/player/ChestStealerA.java`
- [ ] `check/impl/player/AutoArmorA.java`
- [ ] `check/impl/player/FastEatA.java`

### Tests obligatorios
- [ ] Totem reswap legit (σ>1.5, n≥5) → NO flag
- [ ] Inventario abierto sin moverse → NO flag InventoryA
- [ ] Swap F offhand → NO flag

> 📖 Ver [Check Catalog § Player](#cat-player) para specs detalladas.
**Dependencias:** FASE 7 · **Archivos:** 16 (+ tests)
---

<a id="fase-12"></a>
## 🔴 FASE 12 — Checks Macro 🤖

### Detección
- [ ] `check/impl/macro/MacroSequenceA.java` (n-gram Rabin-Karp)
- [ ] `check/impl/macro/MacroTimingA.java` (reacción <150ms)
- [ ] `check/impl/macro/MacroVarianceA.java` (σ <1.5ms con n≥20)
- [ ] `check/impl/macro/MacroInputA.java` (4+ acciones mismo tick)
- [ ] `check/impl/macro/MacroInventoryA.java` (FSM auto-gapple/pot/soup/armor)
- [ ] `check/impl/macro/MacroClickerA.java` (FFT kurtosis + picos)
- [ ] `check/impl/macro/MacroCorrelationA.java` (Pearson r²>0.95)

### Infraestructura
- [ ] Enum `MacroState` (IDLE, DETECTING, CONFIRMED, EXEMPT)
- [ ] `check/impl/macro/MacroAction.java` (enum: CLICK, SWAP, CROUCH, 
      JUMP, USE_ITEM, HOTBAR_KEY, WINDOW_CLICK)

### Mitigation profile macro (conservador)
- [ ] VL 0-4 → NO_ACTION (solo log evidencia)
- [ ] VL 5-9 → SILENT_ALERT (staff only)
- [ ] VL 10-14 → CANCEL_PACKET input sospechoso + ALERT
- [ ] VL 15-19 → CANCEL_PACKET + RESYNC inv + ALERT
- [ ] VL 20+ → KICK mensaje genérico

### Tests obligatorios
- [ ] Humano con hardware gaming consistente → NO flag
- [ ] Macro real σ<2ms → flag correcto
- [ ] Lag spike tps<18 → pausa 5s post-recovery
- [ ] High ping >300ms → desactiva MacroTimingA
- [ ] DEATH/RESPAWN → reset buffers
- [ ] Whitelist UUID respetada

### Integración
- [ ] MacroInventoryA coexiste con InventoryA (comparten InventoryTracker)
- [ ] Config `macro.yml` con strictness low/medium/high

> 📖 Ver [Check Catalog § Macro](#cat-macro) para specs detalladas.
**Dependencias:** FASE 11 · **Archivos:** 10 (+ tests)
---

<a id="fase-13"></a>
## 🔴 FASE 13 — Checks Misc
- [ ] `check/impl/misc/NameSpoofA.java` (caracteres inválidos en nombre)
- [ ] `check/impl/misc/ClientBrandA.java` (brand spoofed / vacío)
- [ ] `check/impl/misc/GhostHandA.java` (interact sin arm swing previo)
- [ ] Tests asociados

> 📖 Ver [Check Catalog § Misc](#cat-misc) para specs detalladas.
**Dependencias:** FASE 7 · **Archivos:** 4 (+ tests)
---

<a id="fase-14"></a>
## 🔴 FASE 14 — Storage (SQLite / MySQL / MariaDB)

### Providers
- [ ] `storage/StorageProvider.java` (interface)
- [ ] `storage/AbstractSqlProvider.java` (prepared statements + batching)
- [ ] `storage/SQLiteProvider.java` (default, zero-config)
- [ ] `storage/MySQLProvider.java` (HikariCP)
- [ ] `storage/MariaDBProvider.java` (HikariCP)
- [ ] `storage/StorageFactory.java` (lee config.database.type)

### Migrations
- [ ] `storage/migration/MigrationManager.java`
- [ ] `storage/migration/V1__init.sql`
- [ ] `storage/migration/V2__add_mitigation.sql`
- [ ] `storage/migration/V3__add_alert_prefs.sql`
- [ ] `storage/migration/V4__add_macro_log.sql`

### Models
- [ ] `storage/model/ViolationRecord.java`
- [ ] `storage/model/PlayerProfile.java`
- [ ] `storage/model/MitigationLog.java`
- [ ] `storage/model/AlertLog.java`
- [ ] `storage/model/AlertPreference.java`
- [ ] `storage/model/MacroEvidence.java`

### Pool HikariCP
- [ ] maxPoolSize 10, minIdle 2
- [ ] connectionTimeout 5000ms, maxLifetime 1800000ms
- [ ] leakDetectionThreshold 30000ms

### Tablas obligatorias
- [ ] `assasin_violations` (id, uuid, check_name, vl, timestamp, ping, tps, 
      world, x, y, z, mitigation_applied, data_json)
- [ ] `assasin_players` (uuid, name, first_join, last_join, total_violations, 
      banned, ban_reason)
- [ ] `assasin_mitigations` (id, violation_id, strategy, result, timestamp)
- [ ] `assasin_alerts` (id, staff_uuid, player_uuid, check_name, vl, timestamp)
- [ ] `assasin_alert_preferences` (uuid, check_name, enabled, channels_bitmask)
- [ ] `assasin_macro_evidence` (id, uuid, pattern_hash, occurrences, 
      avg_delta_ms, std_dev, last_seen, evidence_json)

### Índices
- [ ] idx_violations_uuid, idx_violations_timestamp, idx_violations_check
- [ ] idx_macro_pattern_hash

### Tests
- [ ] Tests con SQLite in-memory
- [ ] Migrations up/down
**Dependencias:** FASE 2 · **Archivos:** 15 (+ tests)
---

<a id="fase-15"></a>
## 🔴 FASE 15 — Alert Manager + Discord + Hover
- [ ] `alert/AlertManager.java` (filtrado por preferencias GUI)
- [ ] `alert/AlertFormatter.java` (MiniMessage + HoverEvent + ClickEvent)
- [ ] `alert/DiscordWebhook.java` (async, embed color 0x8A0303, 
      footer "by TyouDm")
- [ ] `util/HoverBuilder.java` (tooltip multilínea estandarizado)

### Placeholders soportados
- [ ] `{player}` `{check}` `{vl}` `{maxVl}` `{ping}` `{jitter}` `{packet_loss}`
- [ ] `{tps}` `{mitigation}` `{pos}` `{world}` `{brand}` `{category}`

### Canales configurables por usuario
- [ ] chat · actionbar · title · sound · discord

### Tests
- [ ] Hover renderiza correctamente
- [ ] ClickEvent ejecuta `/assasin info {player}`
- [ ] Discord webhook async no bloquea main thread
**Dependencias:** FASE 14 · **Archivos:** 6 (+ tests)
---

<a id="fase-16"></a>
## 🔴 FASE 16 — GUI completa

### Core
- [ ] `gui/GuiManager.java` (ConcurrentHashMap<UUID, AssasinGui>)
- [ ] `gui/AssasinGui.java` (base class + InventoryHolder custom)

### Pantallas
- [ ] `gui/screen/MainGui.java` (6 filas: categorías + utilidades + admin)
- [ ] `gui/screen/CategoryGui.java` (submenú por categoría)
- [ ] `gui/screen/AlertsToggleGui.java` (chat/actionbar/title/sound/discord)
- [ ] `gui/screen/CheckManagerGui.java` (admin, paginado)
- [ ] `gui/screen/ServerStatsGui.java` (TPS, flags 24h, top checks)
- [ ] `gui/screen/AboutGui.java` (Nether Star centrado, "Author: TyouDm")
- [ ] `gui/screen/RecentFlagsGui.java` (últimas violaciones)
- [ ] `gui/screen/AlertFormatGui.java` (Written Book con placeholders)
- [ ] `gui/screen/BulkActionsGui.java` (enable/disable all, reset VLs)

### Components
- [ ] `gui/component/GuiItem.java` (ItemStack + action + lore builder)
- [ ] `gui/component/GuiAction.java` (Consumer<InventoryClickEvent>)
- [ ] `gui/component/GuiBorder.java` (red glass panes)
- [ ] `gui/component/PaginationBar.java`

### Util
- [ ] `gui/util/GuiColors.java` (reuso AssasinColors)
- [ ] `gui/util/ItemBuilder.java` (PersistentDataContainer + lore fluent)

### Persistencia
- [ ] NamespacedKey `assasin:gui_action` + `assasin:check_name`
- [ ] Cache Caffeine preferencias expireAfterAccess 10min
- [ ] Tabla `assasin_alert_preferences` sincronizada al toggle

### Sonidos
- [ ] UI_BUTTON_CLICK al click (0.5 vol)
- [ ] BLOCK_NOTE_BLOCK_PLING al toggle ON
- [ ] BLOCK_NOTE_BLOCK_BASS al toggle OFF

### Tests
- [ ] Clicks disparan actions correctas
- [ ] Items admin invisibles sin permiso (glass pane placeholder)
- [ ] Refresh diferencial (solo slots modificados)
- [ ] InventoryCloseEvent limpia GuiManager
**Dependencias:** FASE 15 · **Archivos:** 18 (+ tests)
---

<a id="fase-17"></a>
## 🔴 FASE 17 — Comandos Brigadier

### Root
- [ ] `command/AssasinCommand.java` (root con Paper Brigadier)

### Subcomandos
- [ ] `GuiSubCommand` → `/assasin gui`
- [ ] `AlertsSubCommand` → `/assasin alerts [on|off]`
- [ ] `InfoSubCommand` → `/assasin info <player>`
- [ ] `VlSubCommand` → `/assasin vl <player> [check] [reset]`
- [ ] `LogsSubCommand` → `/assasin logs <player> [page]`
- [ ] `ReplaySubCommand` → `/assasin replay <player>`
- [ ] `TestSubCommand` → `/assasin test <check>`
- [ ] `CheckSubCommand` → enable/disable/set
- [ ] `ExemptSubCommand` → `/assasin exempt <player> <type> <seconds>`
- [ ] `DebugSubCommand` → `/assasin debug <player> [on|off]`
- [ ] `ReloadSubCommand` → `/assasin reload [config|messages|checks|all]`
- [ ] `DbSubCommand` → status/migrate/backup/query
- [ ] `BanSubCommand` · `KickSubCommand`
- [ ] `HelpSubCommand` (hover con categorías)
- [ ] `VersionSubCommand` → "ASSASIN v1.0.0 by TyouDm"

### Requisitos
- [ ] Permission `assasin.command.<subcommand>` por subcomando
- [ ] Prefix rojo sangre en todos los outputs
- [ ] HoverEvent.showText con detalles contextuales
- [ ] ClickEvent suggestCommand/runCommand según contexto
- [ ] Tab completion contextual

### Tests
- [ ] Con permiso → ejecuta; sin permiso → mensaje `✖ No permission`
- [ ] Tab completion funcional
- [ ] `/assasin version` devuelve "by TyouDm"
**Dependencias:** FASE 16 · **Archivos:** 17 (+ tests)
---

<a id="fase-18"></a>
## 🔴 FASE 18 — Configuración YAML

### Archivos
- [ ] `config.yml` (general, storage, threads, flags)
- [ ] `checks.yml` (toggle + thresholds)
- [ ] `mitigation.yml` (profiles + cascadas VL)
- [ ] `latency.yml` (ping comp, transaction interval)
- [ ] `alerts.yml` (formatos, webhook, sounds)
- [ ] `messages.yml` (i18n-ready, todos los textos)
- [ ] `legit-techniques.yml` (tolerancias PvP)
- [ ] `macro.yml` (strictness, whitelist, thresholds)
- [ ] `gui.yml` (layout slots, materiales)

### ConfigManager
- [ ] `config/ConfigManager.java` (hot-reload + validation)
- [ ] Per-file config classes: `CheckConfig` `MessagesConfig` 
      `MitigationConfig` `LatencyConfig` `DatabaseConfig` `AlertConfig` 
      `LegitConfig` `MacroConfig` `GuiConfig`

### Comentarios en YAMLs
- [ ] Cada key con comentario `#` explicando propósito
- [ ] Ejemplos inline para valores complejos
- [ ] Referencias al anchor [Config Reference](#config) del ToDo.md
**Dependencias:** FASE 17 · **Archivos:** 12
---

<a id="fase-19"></a>
## 🔴 FASE 19 — JMH Benchmarks + optimización

### Benchmarks
- [ ] `bench/MovementCheckBench.java`
- [ ] `bench/CombatCheckBench.java`
- [ ] `bench/MacroCheckBench.java`
- [ ] `bench/FftBench.java` (radix-2 n=32 vs n=64)
- [ ] `bench/RingBufferBench.java`
- [ ] `bench/PredictorBench.java`
- [ ] `bench/MitigationEngineBench.java`
- [ ] `bench/StorageBench.java`

### Targets
- [ ] <0.1ms P99 por check individual
- [ ] <1ms P99 por jugador/tick con 200 jugadores simulados
- [ ] Async overhead <50µs

### Profiling
- [ ] async-profiler + flamegraph
- [ ] Cero autoboxing en hot paths (JIT log)
- [ ] Branch prediction correcta
**Dependencias:** FASE 18 · **Archivos:** 10
---

<a id="fase-20"></a>
## 🔴 FASE 20 — README + shadowJar final
- [ ] `README.md` completo con ASCII banner rojo sangre + "Author: TyouDm"
- [ ] shadowJar configurado con relocations (hikari, caffeine)
- [ ] Verificación final de "TyouDm":
    - [ ] `paper-plugin.yml` authors
    - [ ] `build.gradle.kts` metadata
    - [ ] JavaDoc `@author` en TODAS las clases públicas
    - [ ] ASCII banner al load
    - [ ] `README.md` header + footer
    - [ ] ToDo.md (este archivo)
    - [ ] `/assasin version` · `/assasin help`
    - [ ] GUI About (Nether Star)
    - [ ] Discord webhook embed footer
- [ ] `CHANGELOG.md` inicial con v1.0.0 (sección dentro del ToDo.md)
**Dependencias:** FASE 19 · **Archivos:** 3
---
---

<a id="legit"></a>
# 🛡️ Legit PvP Techniques Whitelist

Técnicas que NO deben causar false-flags. Cada una validada con tests en 
FASE 7/9/10/11.
- **W-TAP** — SPRINT OFF→ON ≤3t alrededor de ATTACK. Exempt VelocityA 5t.
- **S-TAP** — backward input ≤2t + re-sprint. Predictor re-baseline.
- **A/D-TAP** — oscilación lateral con yaw estable. StrafeA tolera Δ≤0.15.
- **JUMP-RESET legit** — jump Y≈0.42 ≤1t pre-damage. JumpResetA/B solo 
  flag con σ<1.5 y éxito ≥95% en n≥8.
- **BLOCK-HIT** — USE_ITEM shield activo. VelocityA/B: expected_kb × 0.5.
- **CRIT-TAPPING** — jumps rítmicos sync attack cooldown. CriticalsA 
  valida `onGround=false + motionY<0`.
- **SPEED/NINJA/JITTER/MOON/TELLY BRIDGE** — Scaffold valida raytrace real 
  (face + pos), NO rotación aislada.
- **BUTTERFLY/JITTER/DRAG CLICK** — distribución bimodal humana con 
  kurtosis alta. AutoClicker flag por baja varianza + baja kurtosis + 
  FFT pico único.
- **ATTRIBUTE-SWAP** — HELD_ITEM_CHANGE entre ATTACK. AttributeSwapA 
  informa a VelocityA, MaceDmgA, AutoClickerA.
- **AXE/SWORD/MACE combo** — cooldown por arma. AutoClicker segmenta.
- **COMBO-RESET** — multi-target con rotación pre-hit. KillauraB solo 
  flag si Δyaw>180°/tick o hits simultáneos sin rotación.
- **PEARL-PHASE** — TELEPORT event. Exempt PEARL 3s.
- **LEGIT TOTEM-SWAP** — reswap ≥5t, σ>1.5, sin multitasking.
- **OFFHAND-SWAP (F)** — SWAP_HANDS permitido salvo estados imposibles.
- **ELYTRA / FIREWORK / WALL-BOUNCE / SUPER-BOOST** — 
  ExemptType.ELYTRA_BOOST 20t tras firework.
- **RIPTIDE** — trident + agua/lluvia. Exempt RIPTIDE 20t.
- **GODBRIDGE** — pitch ~80° fijo. ScaffoldB exige σ>0.1° jitter humano 
  (cambios instantáneos sub-tick → cheat).
- **FAST-BRIDGE con sneak toggle rápido** — TimerA solo flag desync de 
  movimiento, no sneak toggles.
---

<a id="efficiency"></a>
# ⚡ Efficiency Notes

### Reglas universales
- EARLY-EXIT primera línea (exempts/disabled/prereqs)
- Ring buffers fijos con arrays primitivos + índice circular (NO LinkedList)
- Primitivos (double/long/int), cero autoboxing en hot paths
- Lazy computation (GCD/FFT/raytrace solo con sospecha preliminar)
- Stateless: PlayerData con AtomicReferences granulares
- `Math.*` estándar (JIT intrinsics) en hot paths
- Cache local de variables antes del loop
- Branch prediction: condiciones común → raro
- PacketType enum switch (NO instanceof)
- Offload async: DB, Discord, FFT → AsyncProcessor

### Por check crítico
- **KillauraC** — Voxel DDA Amanatides-Woo, máx ~12 iter (NO step 0.01)
- **AimA** — GCD Euclidean longs cada 40t con buffer≥32, NO por packet
- **AutoClickerC / MacroClickerA** — FFT radix-2 n=32 async SOLO si σ 
  preliminar sospechosa
- **MacroSequenceA** — Rolling hash Rabin-Karp O(1). 
  HashMap<long,int> con Caffeine ttl 30s
- **VelocityA/B/C** — pending_kb al envío SET_ENTITY_VELOCITY. Ratio O(1)
- **ReachA** — DistanceSq (evita sqrt), threshold² precomputado
- **FastBreakA** — `HashMap<Material+Tool+Enchant,long>` precomputado al load
- **MountSpeedA** — switch EntityType (JIT), `static final Map<MountPhysics>`
- **ElytraPredictor** — RESET en transiciones, tolerancia acumulativa, 
  ring buffer 20 velocities

### Objetivo
- <0.1ms P99 por check individual
- <1ms P99 por jugador/tick con 200 jugadores
---

<a id="macro-strategy"></a>
# 🤖 Macro Detection Strategy

Macros = secuencias automatizadas de inputs LEGALES con timing sobrehumano. 
Categoría propia (no son movement cheats).

### Filosofía conservadora
| VL    | Acción                                                |
|-------|-------------------------------------------------------|
| 0-4   | NO_ACTION (log interno, acumula evidencia)            |
| 5-9   | SILENT_ALERT (staff only, no avisa jugador)           |
| 10-14 | CANCEL_PACKET input sospechoso + ALERT                |
| 15-19 | CANCEL_PACKET + RESYNC inventario + ALERT             |
| 20+   | KICK con mensaje genérico                             |
**Razón:** macro-user pierde la ventaja (ritmo roto, cooldown desincronizado) 
sin que el AntiCheat revele que fue detectado → harder to adapt.

### Anti false-flag
- Hardware gaming consistente → n≥20 inv, n≥30 clicks
- Whitelist UUID en `macro.yml` (streamers/torneos)
- HIGH_PING>300ms desactiva MacroTimingA
- LAG_SPIKE tps<18 pausa todos 5s post-recovery
- DEATH/RESPAWN resetea buffers
- Config strictness low|medium|high (low: n≥50, r²>0.98)
---

<a id="elytra"></a>
# 🪶 Elytra Physics Notes

La elytra NO tiene velocidad terminal. Motion acumula tick a tick. Dive 
2→40 b/s en ~8s → **NO es cheat, es vanilla**.

### Constantes
- `GRAVITY_ELYTRA = 0.08`
- `HORIZONTAL_DRAG = 0.99`
- `VERTICAL_DRAG = 0.98`

### Fórmula (Mojang simplificada)
1. `lookVec = dirVector(pitch, yaw)`
2. `motion.y -= GRAVITY * (0.5 + 0.5 * min(1, -lookVec.y * 10))`
3. fallBonus si `motion.y<0 && hLookLen>0`
4. diveBonus si `lookVec.y<0` → acelera progresivamente
5. Redirección hacia look
6. Drag final

### Consumidores
- **ElytraA** — flag solo por desviación persistente ≥12t
- **SpeedA/B** — delegan a ElytraPredictor si `elytra_active`
- **FlyA/B** — DESHABILITADOS durante elytra
- **NoFallA** — no dispara durante landing correcto

### Exempts
- `ELYTRA_ACTIVE` — mientras deployed
- `ELYTRA_BOOST 20t` — tras firework use
- `ELYTRA_WALL_BOUNCE 5t` — tras colisión lateral
- `ELYTRA_TAKEOFF 10t` — transición ground→air
- `ELYTRA_LANDING 15t` — transición air→ground

### RESET
- Takeoff, landing, firework use, wall collision
**Regla crítica:** ElytraA NO flaggea por "velocidad alta". Solo por 
desviación persistente vs `ElytraPredictor.predict()` acumulada ≥12 ticks.
---
---

<a id="checks"></a>
# 📖 Check Catalog

Especificación completa de cada check. Formato compacto:

> **Detecta** → qué identifica en lenguaje humano  
> **Cómo** → algoritmo técnico  
> **Severidad · Complejidad · Packets**  
> **False+** → falsos positivos conocidos  
> **Mitigación** → cascada VL→acción  
> **Alert · Config**
**Formato global de alerta:**  
`⚔ {player} flagged {check} [VL:{vl}] ({ping}ms)`
**Leyenda severidad:** 🟢 Baja · 🟡 Media · 🟠 Alta · 🔴 Crítica
---

<a id="cat-movement"></a>
## 🏃 Movement (14 checks)

#### `SpeedA` — Horizontal Speed
**Detecta:** velocidad horizontal (XZ) superior al máximo vanilla.  
**Cómo:** compara `deltaXZ` observado vs `base_speed` esperado según estado 
(sprint 0.2806, walk 0.2158, sneak 0.065) + efectos (Speed/Slowness) + 
bloque bajo pies (ice, slime, soul sand). Flag si desviación persistente 
>tolerancia+ping_comp durante ≥3t consecutivos.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** PLAYER_FLYING  
**False+:** ice/packed_ice encadenado → mitigado con detección de bloque 
previo. Knockback reciente → exempt 10t.  
**Mitigación:** VL3 SETBACK soft · VL8 ALERT · VL15 KICK  
**Alert:** `⚔ {player} flagged SpeedA [VL:{vl}]` · **Config:** `checks.movement.SpeedA`

#### `SpeedB` — Friction-based Prediction
**Detecta:** speed cheats que burlan SpeedA con friction manipulada.  
**Cómo:** usa `MovementPredictor` vanilla con friction = 0.91 × blockFriction. 
Flag si `|observed - predicted| > 0.003 + ping_comp` durante ≥5t.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** PLAYER_FLYING  
**False+:** friction anormal en bloques modded → respeta `blockFriction` override.  
**Mitigación:** VL5 SETBACK · VL10 ALERT · VL18 KICK  
**Alert:** `⚔ {player} flagged SpeedB [VL:{vl}]` · **Config:** `checks.movement.SpeedB`

#### `FlyA` — Gravity Violation
**Detecta:** vuelo simple ignorando gravedad.  
**Cómo:** `motion.y` esperado = `(prevMotionY - 0.08) × 0.98`. Flag si en 
aire (`onGround=false`) `observedY > expectedY + 0.001` durante ≥4t sin 
jump/boost/elytra/levitation.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** PLAYER_FLYING  
**False+:** levitation → exempt. Slime rebound → exempt 5t. Web/scaffold → exempt.  
**Mitigación:** VL2 SETBACK · VL5 ALERT · VL10 KICK  
**Alert:** `⚔ {player} flagged FlyA [VL:{vl}]` · **Config:** `checks.movement.FlyA`

#### `FlyB` — Hover
**Detecta:** hover cheats que mantienen Y constante sin caer.  
**Cómo:** flag si `|motionY| < 0.005` en aire durante ≥10t sin 
levitation/slow_falling/elytra.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** PLAYER_FLYING  
**False+:** cobweb, soul sand bubble, levitation → exempt correspondiente.  
**Mitigación:** VL3 SETBACK · VL6 ALERT · VL10 KICK  
**Alert:** `⚔ {player} flagged FlyB [VL:{vl}]` · **Config:** `checks.movement.FlyB`

#### `NoFallA` — Fall Damage Bypass
**Detecta:** bypass de fall damage vía packet `ON_GROUND` falso.  
**Cómo:** track `fallDistance` acumulado. Si cliente reporta `onGround=true` 
pero servidor calcula `onGround=false` con `fallDistance>3.0` → flag + 
CANCEL packet onGround.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** PLAYER_FLYING  
**False+:** water/cobweb/slime bounce → exempt. Elytra landing → exempt 15t.  
**Mitigación:** VL1 CANCEL_PACKET onGround forzado · VL5 ALERT · VL10 damage retroactivo  
**Alert:** `⚔ {player} flagged NoFallA [VL:{vl}]` · **Config:** `checks.movement.NoFallA`

#### `JesusA` — Water/Lava Walk
**Detecta:** caminar sobre líquido sin hundirse.  
**Cómo:** si bloque bajo pies es `WATER`/`LAVA` y `motionY >= 0` durante 
≥3t sin lily pad/frost walker/depth strider → flag.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** PLAYER_FLYING  
**False+:** frost walker enchant → exempt. Lily pad/ice → validar bloque 
real. Riptide jump → exempt.  
**Mitigación:** VL2 SETBACK · VL5 ALERT · VL8 KICK  
**Alert:** `⚔ {player} flagged JesusA [VL:{vl}]` · **Config:** `checks.movement.JesusA`

#### `StepA` — Auto-Step
**Detecta:** subir bloques sin jump (step > 0.6).  
**Cómo:** flag si `deltaY` en un tick supera `max_step = 0.6` sin jump 
input previo y sin slab/stair válido.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** PLAYER_FLYING  
**False+:** slab/stair = 0.5 step legal. Piston push → exempt. Boat exit 
→ exempt 3t.  
**Mitigación:** VL3 SETBACK · VL7 ALERT · VL12 KICK  
**Alert:** `⚔ {player} flagged StepA [VL:{vl}]` · **Config:** `checks.movement.StepA`

#### `TimerA` — Game Speed Manipulation
**Detecta:** cliente procesa más ticks por segundo (timer hack).  
**Cómo:** balance = `balance + 50 - deltaT_ms` cada PLAYER_FLYING. Si 
`balance > 150ms` sostenido → flag timer speedup.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** PLAYER_FLYING  
**False+:** lag spike recovery (burst) → autoregula. High ping jitter → 
threshold dinámico.  
**Mitigación:** VL5 CANCEL_PACKET exceso · VL10 ALERT · VL15 KICK  
**Alert:** `⚔ {player} flagged TimerA [VL:{vl}]` · **Config:** `checks.movement.TimerA`

#### `PhaseA` — Block Phase
**Detecta:** atravesar bloques sólidos.  
**Cómo:** `CollisionEngine` valida que nueva posición no intersecte AABB 
sólido al moverse. Si `from` y `to` separados por bloque sólido sin 
portal/pearl → flag + SETBACK hard.  
**Severidad:** 🔴 Crítica · **Complejidad:** O(n) AABB voxel · **Packets:** PLAYER_FLYING  
**False+:** pearl teleport → exempt 3t. End portal → exempt. Piston → exempt 2t.  
**Mitigación:** VL1 SETBACK hard · VL3 ALERT · VL5 KICK  
**Alert:** `⚔ {player} flagged PhaseA [VL:{vl}]` · **Config:** `checks.movement.PhaseA`

#### `StrafeA` — Air Strafe
**Detecta:** cambio de dirección horizontal en aire (imposible vanilla).  
**Cómo:** en aire, dot product entre `prevMotionXZ` y `currMotionXZ` debe 
mostrar cambios graduales. Flag si cambio angular >35° sin input/knockback.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** PLAYER_FLYING  
**False+:** knockback → exempt. Bow pushback → exempt. Elytra → exempt.  
**Mitigación:** VL4 SETBACK soft · VL8 ALERT · VL14 KICK  
**Alert:** `⚔ {player} flagged StrafeA [VL:{vl}]` · **Config:** `checks.movement.StrafeA`

#### `ElytraA` 🪶 — Elytra Motion
**Detecta:** elytra cheats (modificación de física de vuelo).  
**Cómo:** `ElytraPredictor.predict()` por tick. Flag solo si desviación 
persistente vs predicción ≥12 ticks consecutivos (evita false+ por 
dive/firework).  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** PLAYER_FLYING  
**False+:** firework boost → exempt 20t. Wall bounce → exempt 5t. 
Takeoff/landing → exempt.  
**Mitigación:** VL3 SETBACK · VL7 ALERT · VL12 KICK  
**Alert:** `⚔ {player} flagged ElytraA [VL:{vl}]` · **Config:** `checks.movement.ElytraA`

#### `JumpResetA` 🛡️ — Jump Reset Pattern
**Detecta:** bypass de velocity vía jump-reset artificial consistente.  
**Cómo:** secuencias `damage→jump (Y≈0.42)→success ratio`. Flag solo con 
σ<1.5ms de delay pre-jump, ≥95% success, n≥8 muestras.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** PLAYER_FLYING+DAMAGE  
**False+:** jump-reset legit con timing humano (σ>1.5ms) → NO flag. 
Spam de jumps sin damage → NO flag.  
**Mitigación:** VL5 ALERT · VL12 REDUCE_KB persistencia · VL18 KICK  
**Alert:** `⚔ {player} flagged JumpResetA [VL:{vl}]` · **Config:** `checks.movement.JumpResetA`

#### `JumpResetB` 🛡️ — Jump Reset Timing
**Detecta:** jump-reset con timing sub-tick imposible.  
**Cómo:** mide delay `damage_tick → jump_tick` ≤1 con ≥95% consistencia 
en ≥10 muestras.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** PLAYER_FLYING+DAMAGE  
**False+:** reacción humana excepcional → n≥10 + σ<1ms requerido.  
**Mitigación:** VL5 ALERT · VL15 KICK  
**Alert:** `⚔ {player} flagged JumpResetB [VL:{vl}]` · **Config:** `checks.movement.JumpResetB`

#### `MotionA` — Motion Mismatch
**Detecta:** `motion` reportado por cliente no coincide con delta posición.  
**Cómo:** compara `packet.motion` con `delta = currPos - prevPos`. Flag 
si desviación >0.01 durante ≥5t.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** PLAYER_FLYING  
**False+:** lag compensation vía transaction barrier. Packet reordering 
→ respetar secuencia.  
**Mitigación:** VL4 ALERT · VL10 SETBACK · VL18 KICK  
**Alert:** `⚔ {player} flagged MotionA [VL:{vl}]` · **Config:** `checks.movement.MotionA`
---

<a id="cat-mount"></a>
## 🐎 Mount (4 checks)

#### `MountSpeedA` — Mount Speed Violation
**Detecta:** monturas moviéndose más rápido que la velocidad vanilla del 
EntityType.  
**Cómo:** `static final Map<EntityType, MountPhysics>` precomputado. Switch 
JIT-friendly por tipo (HORSE, CAMEL, PIG, STRIDER, BOAT, MINECART). 
Compara deltaXZ con `mountPhysics.maxSpeed` ± tolerancia.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** PLAYER_FLYING+VEHICLE_MOVE  
**False+:** Camel dash → exempt 8t. Horse jump → exempt. Minecart powered 
rail → exempt. Strider en lava con warped fungus → velocidad especial.  
**Mitigación:** VL3 SETBACK · VL8 DISMOUNT · VL15 KICK  
**Alert:** `⚔ {player} flagged MountSpeedA [VL:{vl}]` · **Config:** `checks.mount.MountSpeedA`

#### `NautilusA` — Aquatic Mount Anomaly
**Detecta:** monturas acuáticas (boat, dolphin-ride) con velocidad o 
comportamiento anómalo fuera/dentro del agua.  
**Cómo:** detecta boat fuera del agua con motion acuática activa, o 
dolphin grace persistente en lava/void.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** VEHICLE_MOVE  
**False+:** boat saliendo del agua por inercia → exempt 5t. Dolphin grace 
tras contacto legítimo → exempt 15t.  
**Mitigación:** VL3 DISMOUNT · VL7 ALERT · VL12 KICK  
**Alert:** `⚔ {player} flagged NauthilusA [VL:{vl}]` · **Config:** `checks.mount.NautilusA`

#### `ZombieHorseA` 🆕 — Untamed Rideable Exploit (1.21.11)
**Detecta:** abuso de la feature 1.21.11 que permite montar zombie horses 
untamed sin saddle.  
**Cómo:** verifica que el EntityType=ZOMBIE_HORSE realmente sea untamed 
vanilla y no spawneado por exploit. Valida tag NBT `Tame` + spawn reason.  
**Severidad:** 🟢 Baja · **Complejidad:** O(1) · **Packets:** USE_ENTITY (mount)  
**False+:** zombie horses spawned by /summon legal (admin) → respeta permiso.  
**Mitigación:** VL2 DISMOUNT · VL5 ALERT  
**Alert:** `⚔ {player} flagged ZombieHorseA [VL:{vl}]` · **Config:** `checks.mount.ZombieHorseA`

#### `MountFlyA` — Mount Flying
**Detecta:** monturas volando (imposible vanilla salvo strider en 
nether-ceiling exploit).  
**Cómo:** si `mountPhysics.canFly=false` y `motion.y > 0` sostenido en 
aire durante ≥6t → flag.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** VEHICLE_MOVE  
**False+:** horse jump → exempt. Camel dash peak → exempt.  
**Mitigación:** VL2 SETBACK · VL5 DISMOUNT · VL10 KICK  
**Alert:** `⚔ {player} flagged MountFlyA [VL:{vl}]` · **Config:** `checks.mount.MountFlyA`
---

<a id="cat-combat"></a>
## ⚔ Combat (21 checks)

### Killaura (4)

#### `KillauraA` — Rotation Delta Pre-Hit
**Detecta:** killaura que snap-rotates al target justo antes del ataque.  
**Cómo:** mide Δyaw/Δpitch entre tick_pre_attack y tick_attack. Flag si 
rotación >45°/tick sin input gradual en los 3 ticks previos.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** PLAYER_ROTATION+ATTACK  
**False+:** combo-reset legit con rotación gradual → NO flag. Target swap 
humano con transición ≥2t → NO flag.  
**Mitigación:** VL3 CANCEL_ATTACK · VL8 ALERT · VL15 KICK  
**Alert:** `⚔ {player} flagged KillauraA [VL:{vl}]` · **Config:** `checks.combat.KillauraA`

#### `KillauraB` — Multi-Target
**Detecta:** atacar múltiples entidades en el mismo tick sin rotación 
entre targets.  
**Cómo:** si ≥2 ATTACK a entidades distintas dentro del mismo tick y 
Δyaw entre targets <10° → flag.  
**Severidad:** 🔴 Crítica · **Complejidad:** O(1) · **Packets:** ATTACK  
**False+:** sword sweep AOE (cooldown full) → NO es ATTACK múltiple, es 
mecánica vanilla. Mace smash AOE → exempt.  
**Mitigación:** VL2 CANCEL_ATTACK extras · VL5 ALERT · VL10 KICK  
**Alert:** `⚔ {player} flagged KillauraB [VL:{vl}]` · **Config:** `checks.combat.KillauraB`

#### `KillauraC` — Wall Attack (Voxel DDA)
**Detecta:** atacar a través de paredes sólidas.  
**Cómo:** Voxel traversal Amanatides-Woo desde eye position hasta target 
hitbox. Si raycast colisiona con bloque sólido opaco antes del target → 
flag. Máx ~12 iteraciones.  
**Severidad:** 🔴 Crítica · **Complejidad:** O(n) DDA ~12 iter · **Packets:** ATTACK  
**False+:** puerta/trapdoor/fence con gaps → respeta AABB real. Ping alto 
→ target rewind habilitado.  
**Mitigación:** VL2 CANCEL_ATTACK · VL5 ALERT · VL8 KICK  
**Alert:** `⚔ {player} flagged KillauraC [VL:{vl}]` · **Config:** `checks.combat.KillauraC`

#### `KillauraD` — Angle Difference
**Detecta:** diferencia angular entre rotation vector y vector-to-target 
imposible para aim humano.  
**Cómo:** calcula ángulo entre `lookVec` y `targetVec`. Flag si ángulo 
>90° en momento del ATTACK.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** PLAYER_ROTATION+ATTACK  
**False+:** hit en el límite del hitbox con lag → target rewind + 
ping_comp 5°.  
**Mitigación:** VL3 CANCEL_ATTACK · VL7 ALERT · VL12 KICK  
**Alert:** `⚔ {player} flagged KillauraD [VL:{vl}]` · **Config:** `checks.combat.KillauraD`

### Aim (3)

#### `AimA` — GCD (Euclidean)
**Detecta:** aim assist/aimbot por GCD constante en deltas de rotación.  
**Cómo:** GCD Euclidean de longs (Δyaw × 1e6) cada 40 ticks con buffer≥32. 
GCD constante → sensitivity artificial. Computación async.  
**Severidad:** 🟠 Alta · **Complejidad:** O(n log n) batch async · **Packets:** PLAYER_ROTATION  
**False+:** sensi muy baja + mouse mecánico (σ bajísima legítima) → 
requiere n≥32 + persistencia ≥3 batches.  
**Mitigación:** VL5 ALERT silent · VL12 ALERT · VL20 KICK  
**Alert:** `⚔ {player} flagged AimA [VL:{vl}]` · **Config:** `checks.combat.AimA`

#### `AimB` — Sensitivity Constant
**Detecta:** sensitivity (magnitud del delta) demasiado constante.  
**Cómo:** Welford online σ de |Δyaw| + |Δpitch|. Flag si σ<0.5° con 
media rotacional alta durante ≥40 samples.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** PLAYER_ROTATION  
**False+:** joystick/controller input → whitelist brand. Aim assist 
consola → respeta `client.brand=bedrock`.  
**Mitigación:** VL5 ALERT · VL15 KICK  
**Alert:** `⚔ {player} flagged AimB [VL:{vl}]` · **Config:** `checks.combat.AimB`

#### `AimC` — Pitch Variance
**Detecta:** pitch perfecto constante (aimbot vertical).  
**Cómo:** σ pitch <0.1° con n≥50 + hits activos durante detección.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** PLAYER_ROTATION  
**False+:** standing still mirando fijo (NO hits) → exempt (solo aplica 
en combate activo).  
**Mitigación:** VL5 ALERT · VL12 CANCEL_ATTACK · VL18 KICK  
**Alert:** `⚔ {player} flagged AimC [VL:{vl}]` · **Config:** `checks.combat.AimC`

### Reach / Hitbox (3)

#### `ReachA` — Ping-Compensated Distance
**Detecta:** atacar entidades más allá del reach vanilla (3.0 survival).  
**Cómo:** `distanceSq` (evita sqrt) eye→target center con threshold² 
precomputado. Ping compensation: `max_reach_sq = (3.0 + ping/50 × 0.1)²`.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** ATTACK  
**False+:** ping alto → compensación generosa. Lag spike → pause 5s.  
**Mitigación:** VL3 CANCEL_ATTACK · VL7 ALERT · VL15 KICK  
**Alert:** `⚔ {player} flagged ReachA [VL:{vl}]` · **Config:** `checks.combat.ReachA`

#### `ReachB` — Target Rewind
**Detecta:** reach validado contra posición real del target en el tick 
del packet (no la actual).  
**Cómo:** `LagCompensatedWorld` rebobina target a su posición 
`tick - ping/50`. Flag si incluso así `reach > 3.0`.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** ATTACK  
**False+:** teleport del target en el mismo tick → exempt.  
**Mitigación:** VL3 CANCEL_ATTACK · VL7 ALERT · VL15 KICK  
**Alert:** `⚔ {player} flagged ReachB [VL:{vl}]` · **Config:** `checks.combat.ReachB`

#### `HitboxA` — AABB Expansion
**Detecta:** hits que requieren hitbox expandida (>0.1 bloques fuera del AABB).  
**Cómo:** raycast eye→attack_vector debe intersectar AABB target 
(expandida +0.1 ping-comp). Flag si distancia al AABB >0.1.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** ATTACK  
**False+:** entity interpolation rendering → rewind compensa.  
**Mitigación:** VL3 CANCEL_ATTACK · VL7 ALERT · VL15 KICK  
**Alert:** `⚔ {player} flagged HitboxA [VL:{vl}]` · **Config:** `checks.combat.HitboxA`

### AutoClicker (3)

#### `AutoClickerA` — CPS Variance
**Detecta:** clicks con varianza anormalmente baja (sin jitter humano).  
**Cómo:** Welford online σ de intervalos entre clicks. Flag si 
σ<1.5ms con n≥30 en ventana de 5s.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** ATTACK+ARM_ANIMATION  
**False+:** butterfly/jitter/drag legítimos tienen σ alta → NO flag. 
Mouse con alta polling rate puede reducir σ → threshold dinámico.  
**Mitigación:** VL5 ALERT · VL12 CANCEL_ATTACK 50% · VL20 KICK  
**Alert:** `⚔ {player} flagged AutoClickerA [VL:{vl}]` · **Config:** `checks.combat.AutoClickerA`

#### `AutoClickerB` — Double-Click Pattern
**Detecta:** dobles clicks con delta artificial (<5ms entre clicks).  
**Cómo:** detecta patrón repetido `click(t), click(t+Δ<5ms)` con Δ 
consistente ≥10 ocurrencias.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** ATTACK  
**False+:** mouse con doble-click hardware defectuoso → whitelist UUID.  
**Mitigación:** VL5 ALERT · VL12 CANCEL extras · VL20 KICK  
**Alert:** `⚔ {player} flagged AutoClickerB [VL:{vl}]` · **Config:** `checks.combat.AutoClickerB`

#### `AutoClickerC` — FFT Spectral
**Detecta:** patrón de clicks con pico espectral único (bot) vs bimodal 
(humano).  
**Cómo:** FFT radix-2 n=32 sobre intervalos. Computación async SOLO si σ 
preliminar sospechosa. Kurtosis baja + un solo peak dominante → flag.  
**Severidad:** 🟠 Alta · **Complejidad:** O(n log n) async · **Packets:** ATTACK  
**False+:** macro humano entrenado (muy raro) → requiere n≥32 + confirmación 
cross-check con AutoClickerA.  
**Mitigación:** VL5 ALERT silent · VL12 ALERT · VL20 KICK  
**Alert:** `⚔ {player} flagged AutoClickerC [VL:{vl}]` · **Config:** `checks.combat.AutoClickerC`

### Velocity (3)

#### `VelocityA` — Horizontal KB Ratio
**Detecta:** reducción anti-knockback horizontal (anti-kb, velocity 
modifier).  
**Cómo:** al enviar SET_ENTITY_VELOCITY, guarda `pending_kb` con tick. 
Al llegar PLAYER_FLYING, calcula ratio `observedXZ / expectedXZ`. Flag 
si ratio <0.9 sostenido.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** SET_ENTITY_VELOCITY+PLAYER_FLYING  
**False+:** block-hit (shield activo) → expected×0.5. W-tap → exempt 5t. 
Jump-reset legit → validado por JumpResetA. Attribute-swap → 
AttributeSwapA notifica.  
**Mitigación:** VL3 ALERT · VL8 FORCE_KB · VL15 KICK  
**Alert:** `⚔ {player} flagged VelocityA [VL:{vl}]` · **Config:** `checks.combat.VelocityA`

#### `VelocityB` — Vertical KB Ratio
**Detecta:** reducción anti-knockback vertical.  
**Cómo:** igual a VelocityA pero eje Y. Flag si `observedY/expectedY < 0.9`.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** SET_ENTITY_VELOCITY+PLAYER_FLYING  
**False+:** jump pre-damage (Y≈0.42) → JumpResetA valida. Block-hit → ×0.5.  
**Mitigación:** VL3 ALERT · VL8 FORCE_KB · VL15 KICK  
**Alert:** `⚔ {player} flagged VelocityB [VL:{vl}]` · **Config:** `checks.combat.VelocityB`

#### `VelocityC` — KB Timing
**Detecta:** knockback aplicado con retraso artificial.  
**Cómo:** mide delay `SET_ENTITY_VELOCITY_sent → motion_applied`. Flag si 
delay >ping+100ms.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** SET_ENTITY_VELOCITY+PLAYER_FLYING  
**False+:** lag spike → pause 5s. Pre-existing motion buffer → respeta.  
**Mitigación:** VL5 ALERT · VL12 KICK  
**Alert:** `⚔ {player} flagged VelocityC [VL:{vl}]` · **Config:** `checks.combat.VelocityC`

### Damage-based (4)

#### `CriticalsA` — Fake Crits
**Detecta:** crits aplicados sin condiciones físicas (onGround=false + 
motionY<0).  
**Cómo:** al recibir ATTACK con crit particle flag, valida que en el tick 
previo `onGround=false && motionY<0 && !inWater && !onLadder && !hasBlindness`.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** ATTACK+PLAYER_FLYING  
**False+:** crit-tapping legit (jump rítmico con cooldown sync) → 
condiciones vanilla cumplidas → NO flag.  
**Mitigación:** VL3 CANCEL_CRIT_DMG · VL8 ALERT · VL15 KICK  
**Alert:** `⚔ {player} flagged CriticalsA [VL:{vl}]` · **Config:** `checks.combat.CriticalsA`

#### `SpearA` 🆕 — Trident Anomaly
**Detecta:** trident throw/riptide con daño o trayectoria imposibles.  
**Cómo:** valida damage esperado según `Impaling` enchant + condición 
(water/rain). Riptide requiere agua/lluvia real.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** ATTACK+USE_ITEM  
**False+:** rain parcial por bioma → valida `isRainingAt(pos)`.  
**Mitigación:** VL3 CANCEL_DMG · VL7 ALERT · VL12 KICK  
**Alert:** `⚔ {player} flagged SpearA [VL:{vl}]` · **Config:** `checks.combat.SpearA`

#### `MaceDmgA` — Mace Scaling
**Detecta:** daño de mace superior al esperado por altura de caída.  
**Cómo:** fórmula vanilla mace: `damage = base + (fallDistance × 
density_per_block)` con caps. Flag si damage observado >expected × 1.1.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** ATTACK+PLAYER_FLYING  
**False+:** density enchant correcto → tabla precomputada. Attribute-swap 
pre-hit → AttributeSwapA notifica.  
**Mitigación:** VL3 CANCEL_DMG_EXCESS · VL7 ALERT · VL12 KICK  
**Alert:** `⚔ {player} flagged MaceDmgA [VL:{vl}]` · **Config:** `checks.combat.MaceDmgA`

#### `MaceDmgB` — Mace Smash without Airtime
**Detecta:** smash attack sin fallDistance suficiente.  
**Cómo:** si attack con mace + crit smash particles pero `fallDistance<1.5` → flag.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** ATTACK+PLAYER_FLYING  
**False+:** wind_burst charged mace legal → valida charged state.  
**Mitigación:** VL3 CANCEL_DMG_EXCESS · VL7 ALERT · VL12 KICK  
**Alert:** `⚔ {player} flagged MaceDmgB [VL:{vl}]` · **Config:** `checks.combat.MaceDmgB`

#### `MaceDmgC` — Mace Cooldown Bypass + Wind Charge
**Detecta:** ataques con mace sin respetar cooldown, o combo con 
wind_charge sub-tick.  
**Cómo:** cooldown mace = 1.0s/attack_speed. Flag si 
`deltaSinceLast < cooldown_ms × 0.9`.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** ATTACK+USE_ITEM  
**False+:** swap a otra arma resetea cooldown legal → AttributeSwapA notifica.  
**Mitigación:** VL3 CANCEL_ATTACK · VL7 ALERT · VL12 KICK  
**Alert:** `⚔ {player} flagged MaceDmgC [VL:{vl}]` · **Config:** `checks.combat.MaceDmgC`

### Validador (1)

#### `AttributeSwapA` 🛡️ — Held Item Swap Tracker
**Detecta:** NO es un check flag-emitter. Es un **validador central** 
que detecta HELD_ITEM_CHANGE entre ATTACK events e informa a 
VelocityA, MaceDmgA, AutoClickerA para evitar false-positives.  
**Cómo:** monitoriza HELD_ITEM_CHANGE + swap offhand. Publica evento 
interno `AttributeSwapEvent` al bus del ModuleRegistry.  
**Severidad:** — (no flag) · **Complejidad:** O(1) · **Packets:** HELD_ITEM_CHANGE+SWAP_HANDS  
**False+:** N/A (es validador).  
**Mitigación:** N/A — solo notifica a otros checks.  
**Alert:** — · **Config:** `checks.combat.AttributeSwapA` (enable/disable tracking)
---

<a id="cat-world"></a>
## 🌍 World (9 checks)

#### `ScaffoldA` — Rotation Consistency
**Detecta:** scaffold cheats que placean bloques sin rotación coherente.  
**Cómo:** al recibir BLOCK_PLACE, valida que `lookVec` en el tick del 
place realmente apunte al face del bloque de soporte. Usa raytrace real 
(NO rotación aislada). Flag si face target no alcanzable con pitch/yaw 
actuales.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) raytrace · **Packets:** BLOCK_PLACE+PLAYER_ROTATION  
**False+:** speed/ninja/jitter bridge legítimos → raytrace válido → NO flag. 
Godbridge humano con σ>0.1° jitter → NO flag.  
**Mitigación:** VL3 CANCEL_PLACE · VL7 ALERT · VL12 KICK  
**Alert:** `⚔ {player} flagged ScaffoldA [VL:{vl}]` · **Config:** `checks.world.ScaffoldA`

#### `ScaffoldB` — Invalid Angles / Godbridge Jitter
**Detecta:** godbridge automatizado (pitch ~80° fijo sin jitter humano).  
**Cómo:** mide σ pitch durante bridging backward (sneak + backward). 
Flag si σ<0.1° con n≥20 placements consecutivos. Godbridge humano 
tiene micro-jitter natural >0.1°.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** BLOCK_PLACE+PLAYER_ROTATION  
**False+:** mouse con DPI altísimo + lockpad → whitelist UUID. Short 
sessions n<20 → NO flag.  
**Mitigación:** VL5 ALERT · VL12 CANCEL_PLACE · VL18 KICK  
**Alert:** `⚔ {player} flagged ScaffoldB [VL:{vl}]` · **Config:** `checks.world.ScaffoldB`

#### `ScaffoldC` — Backward Placement
**Detecta:** placement de bloques hacia atrás sin rotación correspondiente 
(scaffold instant).  
**Cómo:** si delta movement es backward (dot(motionXZ, lookVec) < 0) y 
bloque se placea en dirección opuesta al look sin pitch >45° → flag.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** BLOCK_PLACE  
**False+:** telly/moon bridge legit con pitch correcto → NO flag.  
**Mitigación:** VL3 CANCEL_PLACE · VL7 ALERT · VL12 KICK  
**Alert:** `⚔ {player} flagged ScaffoldC [VL:{vl}]` · **Config:** `checks.world.ScaffoldC`

#### `TowerA` — Jump+Place Timing
**Detecta:** tower cheats (subir placeando bloques con timing sobrehumano).  
**Cómo:** mide σ del delta `jump_tick → place_tick` durante tower 
vertical. Flag si σ<2ms con n≥10 placements y altura ≥10 bloques.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** BLOCK_PLACE+PLAYER_FLYING  
**False+:** tower legit humano tiene σ>2ms natural. Short towers n<10 → NO flag.  
**Mitigación:** VL5 ALERT · VL12 CANCEL_PLACE · VL18 KICK  
**Alert:** `⚔ {player} flagged TowerA [VL:{vl}]` · **Config:** `checks.world.TowerA`

#### `NukerA` — Multi-Block Break
**Detecta:** romper múltiples bloques no adyacentes en el mismo tick.  
**Cómo:** si ≥2 BLOCK_BREAK events en mismo tick con distancia entre 
bloques >1.73 (diagonal) → flag.  
**Severidad:** 🔴 Crítica · **Complejidad:** O(1) · **Packets:** BLOCK_BREAK  
**False+:** TNT/explosion breaks → evento servidor, no BLOCK_BREAK packet. 
Efficiency V + haste → respeta break_time tabla.  
**Mitigación:** VL1 CANCEL_BREAK extras · VL3 ALERT · VL5 KICK  
**Alert:** `⚔ {player} flagged NukerA [VL:{vl}]` · **Config:** `checks.world.NukerA`

#### `FastBreakA` — Break Time Bypass
**Detecta:** romper bloques más rápido que el break_time vanilla.  
**Cómo:** `HashMap<Material+Tool+Enchant, long_ticks>` precomputado al 
load. Al BLOCK_BREAK, valida `delta_dig_start_to_break ≥ expected × 0.9`. 
Incluye Haste/Mining Fatigue/Aqua Affinity.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) lookup · **Packets:** DIGGING+BLOCK_BREAK  
**False+:** creative/adventure modes → exempt por gamemode. Efficiency V 
+ Haste II en material correcto → cálculo exacto.  
**Mitigación:** VL2 CANCEL_BREAK · VL5 ALERT · VL10 KICK  
**Alert:** `⚔ {player} flagged FastBreakA [VL:{vl}]` · **Config:** `checks.world.FastBreakA`

#### `FastPlaceA` — Placement Rate
**Detecta:** placement rate superior al humanamente posible.  
**Cómo:** flag si ≥6 BLOCK_PLACE/tick o ≥20 places/segundo sostenido.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** BLOCK_PLACE  
**False+:** speed bridge legítimo → 5-6 places/s normal. Short bursts → NO flag.  
**Mitigación:** VL3 CANCEL_PLACE excess · VL7 ALERT · VL15 KICK  
**Alert:** `⚔ {player} flagged FastPlaceA [VL:{vl}]` · **Config:** `checks.world.FastPlaceA`

#### `LiquidWalkA` — Placement on Liquid
**Detecta:** placement de bloques directamente sobre agua/lava sin 
soporte sólido.  
**Cómo:** si block clicked face es WATER/LAVA directo (sin waterlogged 
soportante ni lily pad) → flag.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** BLOCK_PLACE  
**False+:** lily pad, frozen water, waterlogged stairs → respeta estado 
real del bloque.  
**Mitigación:** VL3 CANCEL_PLACE · VL7 ALERT · VL12 KICK  
**Alert:** `⚔ {player} flagged LiquidWalkA [VL:{vl}]` · **Config:** `checks.world.LiquidWalkA`

#### `AirPlaceA` — Placement without Support
**Detecta:** placement de bloques en el aire sin bloque de soporte válido.  
**Cómo:** raytrace desde eye hacia face del bloque target. Debe haber 
bloque sólido adyacente. Flag si no lo hay.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** BLOCK_PLACE  
**False+:** scaffolding/chorus flower/plant growth → no son BLOCK_PLACE 
del jugador.  
**Mitigación:** VL2 CANCEL_PLACE · VL5 ALERT · VL10 KICK  
**Alert:** `⚔ {player} flagged AirPlaceA [VL:{vl}]` · **Config:** `checks.world.AirPlaceA`
---

<a id="cat-player"></a>
## 👤 Player (16 checks)

### Inventory (2)

#### `InventoryA` — Move with Inventory Open
**Detecta:** movimiento del jugador con inventario abierto (imposible vanilla).  
**Cómo:** flag si hay delta movement >0.05 mientras `inventoryOpen=true` 
durante ≥3t.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** PLAYER_FLYING+WINDOW events  
**False+:** piston push con inv abierto → exempt. KB residual → exempt 5t.  
**Mitigación:** VL3 CANCEL_PACKET move · VL7 ALERT · VL12 KICK  
**Alert:** `⚔ {player} flagged InventoryA [VL:{vl}]` · **Config:** `checks.player.InventoryA`

#### `InventoryB` — Click without OPEN_WINDOW
**Detecta:** click en inventario sin haber recibido OPEN_WINDOW previo.  
**Cómo:** flag si WINDOW_CLICK sin `inventoryOpen=true` en PlayerData.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** WINDOW_CLICK+OPEN_WINDOW  
**False+:** hotbar swap (F/offhand/number keys) → exempt (es SWAP_HANDS 
o HELD_ITEM_CHANGE, no WINDOW_CLICK).  
**Mitigación:** VL2 CANCEL_CLICK · VL5 ALERT · VL10 KICK  
**Alert:** `⚔ {player} flagged InventoryB [VL:{vl}]` · **Config:** `checks.player.InventoryB`

### BadPackets (6)

#### `BadPacketsA` — NaN / Infinity
**Detecta:** packets con valores NaN o Infinity en posición/rotación.  
**Cómo:** valida `Double.isFinite(x) && isFinite(y) && isFinite(z) && 
isFinite(yaw) && isFinite(pitch)`.  
**Severidad:** 🔴 Crítica · **Complejidad:** O(1) · **Packets:** PLAYER_FLYING+any  
**False+:** ninguno.  
**Mitigación:** VL1 KICK inmediato (packet crasher)  
**Alert:** `⚔ {player} flagged BadPacketsA [VL:{vl}]` · **Config:** `checks.player.BadPacketsA`

#### `BadPacketsB` — Out of Range
**Detecta:** valores fuera del rango válido (yaw > ±1e5, pitch > ±90).  
**Cómo:** valida `|yaw|<1e5 && pitch in [-90,90]`.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** PLAYER_ROTATION  
**False+:** ninguno.  
**Mitigación:** VL1 KICK  
**Alert:** `⚔ {player} flagged BadPacketsB [VL:{vl}]` · **Config:** `checks.player.BadPacketsB`

#### `BadPacketsC` — Position Y Excess
**Detecta:** posición Y extrema (>1e7 o <-1e7).  
**Cómo:** valida `|y| < 1e7`.  
**Severidad:** 🔴 Crítica · **Complejidad:** O(1) · **Packets:** PLAYER_FLYING  
**False+:** ninguno.  
**Mitigación:** VL1 SETBACK + KICK  
**Alert:** `⚔ {player} flagged BadPacketsC [VL:{vl}]` · **Config:** `checks.player.BadPacketsC`

#### `BadPacketsD` — Invalid Rotation
**Detecta:** rotación inválida (yaw/pitch no normalizados o NaN).  
**Cómo:** valida que yaw esté en rango esperado tras normalización mod 360.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** PLAYER_ROTATION  
**False+:** ninguno.  
**Mitigación:** VL1 KICK  
**Alert:** `⚔ {player} flagged BadPacketsD [VL:{vl}]` · **Config:** `checks.player.BadPacketsD`

#### `BadPacketsE` — Invalid Slot
**Detecta:** HELD_ITEM_CHANGE con slot fuera de rango [0,8].  
**Cómo:** valida `slot >= 0 && slot <= 8`.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** HELD_ITEM_CHANGE  
**False+:** ninguno.  
**Mitigación:** VL1 KICK  
**Alert:** `⚔ {player} flagged BadPacketsE [VL:{vl}]` · **Config:** `checks.player.BadPacketsE`

#### `BadPacketsF` — Impossible Duplicates
**Detecta:** packets idénticos imposibles (ej: ATTACK duplicado mismo tick 
con mismo target ID).  
**Cómo:** hash (packetType + targetId + tick). Flag si duplicado.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** ATTACK+USE_ENTITY  
**False+:** retransmission por packet loss → respeta `packet.sequence`.  
**Mitigación:** VL2 CANCEL_PACKET duplicado · VL5 ALERT · VL10 KICK  
**Alert:** `⚔ {player} flagged BadPacketsF [VL:{vl}]` · **Config:** `checks.player.BadPacketsF`

### Misc (4)

#### `PostA` — Action Pre-Teleport Confirm
**Detecta:** acciones (attack/place/break) antes de confirmar teleport.  
**Cómo:** tras SETBACK o TELEPORT, requiere TELEPORT_CONFIRM del cliente 
antes de aceptar otras acciones. Flag si ATTACK/PLACE/BREAK antes del 
confirm.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** TELEPORT_CONFIRM+ATTACK/PLACE/BREAK  
**False+:** lag spike → respeta tolerancia 2t.  
**Mitigación:** VL2 CANCEL action · VL5 ALERT · VL10 KICK  
**Alert:** `⚔ {player} flagged PostA [VL:{vl}]` · **Config:** `checks.player.PostA`

#### `CrashA` — Crasher Packets
**Detecta:** packets conocidos por crashear el servidor (payloads malformados).  
**Cómo:** whitelist de tamaños máximos por packet type. Detecta patrones 
crasher conocidos (custom payload >32KB, book >8KB, etc).  
**Severidad:** 🔴 Crítica · **Complejidad:** O(1) · **Packets:** any  
**False+:** ninguno.  
**Mitigación:** VL1 CANCEL + KICK inmediato  
**Alert:** `⚔ {player} flagged CrashA [VL:{vl}]` · **Config:** `checks.player.CrashA`

#### `BookA` — Book Edit Payload
**Detecta:** BOOK_EDIT payload >8KB (crash exploit).  
**Cómo:** valida tamaño del payload del packet BOOK_EDIT.  
**Severidad:** 🔴 Crítica · **Complejidad:** O(1) · **Packets:** BOOK_EDIT  
**False+:** ninguno.  
**Mitigación:** VL1 CANCEL + KICK  
**Alert:** `⚔ {player} flagged BookA [VL:{vl}]` · **Config:** `checks.player.BookA`

#### `TimerPacketA` — Packet Rate
**Detecta:** rate de packets/s anormalmente alto (DDoS o timer extremo).  
**Cómo:** contador de packets/s por jugador. Flag si >200 packets/s.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** any  
**False+:** lag recovery burst → tolera pico 2s.  
**Mitigación:** VL3 THROTTLE · VL7 ALERT · VL12 KICK  
**Alert:** `⚔ {player} flagged TimerPacketA [VL:{vl}]` · **Config:** `checks.player.TimerPacketA`

### AutoTotem (4)

#### `AutoTotemA` — Reswap Time
**Detecta:** reequipar tótem en offhand con timing sobrehumano tras proc.  
**Cómo:** mide delay `totem_consumed_event → offhand_swap_totem`. Flag si 
<5 ticks (250ms) con n≥3.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** WINDOW_CLICK+entity_status  
**False+:** reswap legit humano ≥5t → NO flag. Latency alto → ping-comp.  
**Mitigación:** VL3 ALERT silent · VL8 CANCEL_CLICK · VL15 KICK  
**Alert:** `⚔ {player} flagged AutoTotemA [VL:{vl}]` · **Config:** `checks.player.AutoTotemA`

#### `AutoTotemB` — σ Variance
**Detecta:** reswap con varianza anormalmente baja (script consistente).  
**Cómo:** Welford σ de reswap_delays. Flag si σ<1.5ms con n≥5.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** WINDOW_CLICK  
**False+:** reswap humano σ>1.5ms natural.  
**Mitigación:** VL3 ALERT silent · VL8 ALERT · VL15 KICK  
**Alert:** `⚔ {player} flagged AutoTotemB [VL:{vl}]` · **Config:** `checks.player.AutoTotemB`

#### `AutoTotemC` — Packet Pattern without OPEN_WINDOW
**Detecta:** WINDOW_CLICK para swap tótem sin haber enviado inventory_open.  
**Cómo:** valida que WINDOW_CLICK (slot 45 / offhand) tenga 
`inventoryOpen=true` previo. Script que usa clicks directos sin abrir 
inventario → flag.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** WINDOW_CLICK+OPEN_WINDOW  
**False+:** hotbar swap legal (F key / SWAP_HANDS) → NO es WINDOW_CLICK.  
**Mitigación:** VL2 CANCEL_CLICK · VL5 ALERT · VL10 KICK  
**Alert:** `⚔ {player} flagged AutoTotemC [VL:{vl}]` · **Config:** `checks.player.AutoTotemC`

#### `AutoTotemD` — Multitasking Same Tick
**Detecta:** swap de tótem + ATTACK/PLACE en el mismo tick (imposible 
humanamente).  
**Cómo:** flag si WINDOW_CLICK inventario + ATTACK/BLOCK_PLACE ocurren en 
mismo tick.  
**Severidad:** 🔴 Crítica · **Complejidad:** O(1) · **Packets:** WINDOW_CLICK+ATTACK/BLOCK_PLACE  
**False+:** ninguno (físicamente imposible en 50ms).  
**Mitigación:** VL1 CANCEL acción secundaria · VL3 ALERT · VL7 KICK  
**Alert:** `⚔ {player} flagged AutoTotemD [VL:{vl}]` · **Config:** `checks.player.AutoTotemD`

### Combate pasivo (3)

#### `ChestStealerA` — Auto Loot
**Detecta:** vaciar cofres con timing sobrehumano (<50ms entre clicks de 
slots distintos).  
**Cómo:** σ de delays entre WINDOW_CLICK de slots distintos en chest 
inventory. Flag si σ<2ms con n≥5 slots.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** WINDOW_CLICK  
**False+:** shift-click masivo es 1 acción. Click arrastre (drag drop) → 
exempt (paquet especial).  
**Mitigación:** VL5 ALERT · VL12 CANCEL extras · VL18 KICK  
**Alert:** `⚔ {player} flagged ChestStealerA [VL:{vl}]` · **Config:** `checks.player.ChestStealerA`

#### `AutoArmorA` — Auto Equip
**Detecta:** reequipar armadura rota automáticamente al instante del break.  
**Cómo:** mide delay `armor_break_event → armor_slot_refill`. Flag si <5t con σ baja.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** WINDOW_CLICK+entity_equipment  
**False+:** hotbar prep manual → respeta interaction antes del break.  
**Mitigación:** VL5 ALERT · VL12 CANCEL_CLICK · VL18 KICK  
**Alert:** `⚔ {player} flagged AutoArmorA [VL:{vl}]` · **Config:** `checks.player.AutoArmorA`

#### `FastEatA` — Fast Eat
**Detecta:** comer ítems más rápido que el tiempo vanilla (1.6s).  
**Cómo:** mide delay `USE_ITEM_START → item_consumed_effect`. Flag si 
<1500ms (95% vanilla).  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** USE_ITEM  
**False+:** golden apple + saturation → respeta mecánica. Chorus fruit → 
instant consumption vanilla.  
**Mitigación:** VL3 CANCEL_EFFECT · VL7 ALERT · VL12 KICK  
**Alert:** `⚔ {player} flagged FastEatA [VL:{vl}]` · **Config:** `checks.player.FastEatA`
---

<a id="cat-macro"></a>
## 🤖 Macro (7 checks)

> **Filosofía:** Mitigación conservadora. VL 0-9 log/silent_alert solamente. 
> VL 10+ CANCEL sin revelar detección.

#### `MacroSequenceA` — N-gram Rabin-Karp
**Detecta:** secuencias de acciones repetidas idénticamente (patrón macro).  
**Cómo:** rolling hash Rabin-Karp O(1) sobre RingBuffer<MacroAction>[64]. 
N-grams de tamaño 3,4,5. `HashMap<long, int>` con Caffeine ttl 30s. 
Flag si mismo hash ≥8 ocurrencias en 30s.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) rolling · **Packets:** ActionTracker central  
**False+:** patrones humanos comunes (WASD repetitivo) → n-grams ≥3 acciones 
distintas. Whitelist UUID.  
**Mitigación:** VL5 silent_alert · VL12 CANCEL sospechoso · VL20 KICK  
**Alert:** `🤖 {player} flagged MacroSequenceA [VL:{vl}]` · **Config:** `checks.macro.MacroSequenceA`

#### `MacroTimingA` — Inhuman Reaction Time
**Detecta:** reacciones <150ms a eventos del juego (damage, arrow, spawn 
enemigo).  
**Cómo:** mide delay `game_event → player_action_response`. Flag si <150ms 
con n≥10 ocurrencias.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** ActionTracker  
**False+:** HIGH_PING>300ms desactiva este check. Reflejos pro 150-200ms 
→ threshold conservador.  
**Mitigación:** VL5 silent_alert · VL12 ALERT · VL20 KICK  
**Alert:** `🤖 {player} flagged MacroTimingA [VL:{vl}]` · **Config:** `checks.macro.MacroTimingA`

#### `MacroVarianceA` — σ Timing
**Detecta:** acciones repetidas con varianza <1.5ms (imposible humano).  
**Cómo:** Welford σ online sobre intervalos de acciones repetidas. Flag si 
σ<1.5ms con n≥20.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** ActionTracker  
**False+:** hardware gaming muy consistente → n≥20 + cross-check 
MacroSequenceA. Whitelist.  
**Mitigación:** VL5 silent_alert · VL12 ALERT · VL20 KICK  
**Alert:** `🤖 {player} flagged MacroVarianceA [VL:{vl}]` · **Config:** `checks.macro.MacroVarianceA`

#### `MacroInputA` — Same-Tick Actions
**Detecta:** 4+ acciones distintas (click + swap + crouch + jump) en el 
mismo tick.  
**Cómo:** cuenta MacroActions en tick actual. Flag si ≥4 tipos distintos 
en un solo tick.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** ActionTracker  
**False+:** hotkey combos (crouch+attack+jump legales) → límite 4 tipos 
distintos es conservador.  
**Mitigación:** VL3 silent_alert · VL8 CANCEL · VL15 KICK  
**Alert:** `🤖 {player} flagged MacroInputA [VL:{vl}]` · **Config:** `checks.macro.MacroInputA`

#### `MacroInventoryA` — Auto Gapple/Pot/Soup/Armor FSM
**Detecta:** auto-gapple, auto-pot, auto-soup, auto-armor vía FSM de 
inventario.  
**Cómo:** FSM por jugador: `IDLE → DETECTING → CONFIRMED`. Transiciones 
basadas en patrones (ej: `damage → slot_swap_to_gapple → USE_ITEM` con 
σ<2ms n≥5). Comparte InventoryTracker con InventoryA.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) FSM · **Packets:** WINDOW_CLICK+USE_ITEM+entity_damage  
**False+:** keybind macros humanos (F para totem) → respeta σ humana. 
Whitelist UUID.  
**Mitigación:** VL5 silent_alert · VL12 RESYNC inventory + ALERT · VL20 KICK  
**Alert:** `🤖 {player} flagged MacroInventoryA [VL:{vl}]` · **Config:** `checks.macro.MacroInventoryA`

#### `MacroClickerA` — FFT + Kurtosis
**Detecta:** clicker macros con pattern espectral artificial.  
**Cómo:** FFT radix-2 n=32 async sobre intervalos de clicks. Flag si 
kurtosis baja + pico espectral único dominante + σ<2ms. Async SOLO si 
preliminar sospechoso.  
**Severidad:** 🟡 Media · **Complejidad:** O(n log n) async · **Packets:** ATTACK+USE_ITEM  
**False+:** humano entrenado rarísimo → cross-check con AutoClickerA/C.  
**Mitigación:** VL5 silent_alert · VL12 ALERT · VL20 KICK  
**Alert:** `🤖 {player} flagged MacroClickerA [VL:{vl}]` · **Config:** `checks.macro.MacroClickerA`

#### `MacroCorrelationA` — Pearson r²
**Detecta:** correlación estadística r²>0.95 entre secuencias de acciones 
en diferentes sesiones/contextos.  
**Cómo:** Pearson correlation coefficient sobre intervalos de acciones 
en contextos distintos (pvp, build, gather). r²>0.95 → macro consistente 
cross-context.  
**Severidad:** 🟠 Alta · **Complejidad:** O(n) batch async · **Packets:** ActionTracker  
**False+:** muestras pequeñas n<50 → NO flag. Strictness low requiere r²>0.98.  
**Mitigación:** VL5 silent_alert · VL12 ALERT · VL20 KICK  
**Alert:** `🤖 {player} flagged MacroCorrelationA [VL:{vl}]` · **Config:** `checks.macro.MacroCorrelationA`
---

<a id="cat-misc"></a>
## 🔧 Misc (3 checks)

#### `NameSpoofA` — Invalid Characters
**Detecta:** nombres con caracteres inválidos (unicode exploit, color 
codes, null bytes).  
**Cómo:** regex `^[a-zA-Z0-9_]{3,16}$` al join. Flag si no coincide.  
**Severidad:** 🟠 Alta · **Complejidad:** O(1) · **Packets:** LOGIN_START  
**False+:** bedrock players con `.` prefix → respeta Floodgate/Geyser integration.  
**Mitigación:** VL1 KICK login  
**Alert:** `⚔ {player} flagged NameSpoofA [VL:{vl}]` · **Config:** `checks.misc.NameSpoofA`

#### `ClientBrandA` — Spoofed/Empty Brand
**Detecta:** client brand vacío, null o spoofed (ej: "vanilla" pero usa 
Forge mods detectados).  
**Cómo:** valida `client.brand` recibido vía CUSTOM_PAYLOAD 
`minecraft:brand`. Flag si vacío/null/blacklist pattern.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** CUSTOM_PAYLOAD  
**False+:** mods legítimos (Optifine, Sodium) → whitelist brands en config.  
**Mitigación:** VL2 ALERT · VL5 KICK  
**Alert:** `⚔ {player} flagged ClientBrandA [VL:{vl}]` · **Config:** `checks.misc.ClientBrandA`

#### `GhostHandA` — Interact without Arm Swing
**Detecta:** USE_ITEM / ATTACK sin ARM_ANIMATION previo.  
**Cómo:** flag si ATTACK/USE_ITEM sin ARM_ANIMATION en mismo tick o tick previo.  
**Severidad:** 🟡 Media · **Complejidad:** O(1) · **Packets:** ATTACK+USE_ITEM+ARM_ANIMATION  
**False+:** packet reordering (raro) → tolera 1t.  
**Mitigación:** VL3 CANCEL action · VL7 ALERT · VL12 KICK  
**Alert:** `⚔ {player} flagged GhostHandA [VL:{vl}]` · **Config:** `checks.misc.GhostHandA`
---
---

<a id="logging"></a>
# 📜 Logging Specification

## Niveles
| Nivel    | Uso                                                            |
|----------|----------------------------------------------------------------|
| `TRACE`  | Packet-by-packet, cada check evaluado (debug profundo)         |
| `DEBUG`  | Cálculos intermedios, predictions, buffers (debug flag on)     |
| `INFO`   | Lifecycle (enable/disable), reloads, staff commands            |
| `WARN`   | Violations flagged (VL aumenta), mitigations aplicadas         |
| `ERROR`  | Excepciones, DB connection issues, config validation fails     |
| `FATAL`  | Plugin disable forzado (DB down crítica, corrupción)           |

## Backends
- **Consola:** Paper console con prefijo `[ASSASIN]` colorizado rojo sangre
- **Archivos rotativos:** `plugins/ASSASIN/logs/`
  - `assasin-YYYY-MM-DD.log` (general, INFO+)
  - `violations-YYYY-MM-DD.log` (solo WARN violations)
  - `mitigations-YYYY-MM-DD.log` (acciones aplicadas)
  - `macro-evidence-YYYY-MM-DD.log` (MacroCheck evidence accumulation)
  - `errors-YYYY-MM-DD.log` (ERROR+FATAL)
  - `staff-actions-YYYY-MM-DD.log` (commands, GUI toggles)
- **DB:** tabla `assasin_violations` + `assasin_mitigations` + `assasin_alerts` + `assasin_macro_evidence`
- **Discord webhook:** violations VL≥threshold configurable

## Formato
### Plain (archivos + consola)