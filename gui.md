# ASSASSIN — GUI Reference

> Referencia completa de layouts, slots y componentes para todas las pantallas del anticheat.
> Clase base: `gui/AssasinGui.java` · Gestor: `gui/GuiManager.java`

---

## Convenciones

| Símbolo | Significado |
|---------|-------------|
| `[B]` | Border — `GuiBorder.java` → Red Stained Glass Pane |
| `[ ]` | Slot vacío (no item) |
| `[X]` | Item funcional (ver detalle de tabla) |
| `[A]` | Item solo visible con permiso `assassin.admin` |

**Numeración de slots:** Empieza en `0` (arriba-izquierda), va de izquierda a derecha, fila por fila.  
Ejemplo en 6 filas × 9 columnas: slot `0`–`8` = fila 1, slot `9`–`17` = fila 2, ..., slot `45`–`53` = fila 6.

**NamespacedKeys registrados:**
- `assasin:gui_action` → identifica la acción del slot
- `assasin:check_name` → identifica el check asociado (CheckManagerGui / CategoryGui)

**Sonidos:**
- Click normal → `UI_BUTTON_CLICK` (vol 0.5, pitch 1.0)
- Toggle ON → `BLOCK_NOTE_BLOCK_PLING` (vol 0.7, pitch 1.2)
- Toggle OFF → `BLOCK_NOTE_BLOCK_BASS` (vol 0.7, pitch 0.8)

---

## 1. MainGui

**Archivo:** `gui/screen/MainGui.java`  
**Título:** `⚔ ASSASSIN — Panel Principal`  
**Tamaño:** 6 filas × 9 cols = 54 slots  
**InventoryHolder:** `AssasinGui`

### Layout visual

```
 0   1   2   3   4   5   6   7   8
[B] [B] [B] [B] [B] [B] [B] [B] [B]   ← fila 1 (border)

 9  10  11  12  13  14  15  16  17
[B] [X] [X] [X] [X] [X] [B] [B] [B]   ← fila 2 (categorías checks)

18  19  20  21  22  23  24  25  26
[B] [B] [X] [X] [X] [X] [B] [B] [B]   ← fila 3 (utilidades)

27  28  29  30  31  32  33  34  35
[B] [B] [A] [A] [A] [B] [B] [B] [B]   ← fila 4 (admin)

36  37  38  39  40  41  42  43  44
[B] [B] [B] [B] [B] [B] [B] [B] [B]   ← fila 5 (border)

45  46  47  48  49  50  51  52  53
[B] [B] [B] [B] [B] [B] [B] [B] [B]   ← fila 6 (border)
```

### Tabla de slots

| Slot | Item | Material | Nombre | Acción (`gui_action`) | Abre |
|------|------|----------|--------|-----------------------|------|
| 0–8, 9, 15–17, 27–28, 32–44, 45–53 | Border | RED_STAINED_GLASS_PANE | `§7 ` | `NONE` | — |
| 10 | Kill Aura | IRON_SWORD (encantado) | `§cKill Aura` | `OPEN_CATEGORY` | CategoryGui(`KILL_AURA`) |
| 11 | Movement | FEATHER | `§6Movement` | `OPEN_CATEGORY` | CategoryGui(`MOVEMENT`) |
| 12 | Aim Checks | BOW (encantado) | `§bAim Checks` | `OPEN_CATEGORY` | CategoryGui(`AIM`) |
| 13 | Scaffold / Place | SCAFFOLDING | `§aScaffold / Place` | `OPEN_CATEGORY` | CategoryGui(`SCAFFOLD`) |
| 14 | Combat+ | TRIDENT | `§5Combat+` | `OPEN_CATEGORY` | CategoryGui(`COMBAT`) |
| 20 | Alertas | BELL | `§eAlertas` | `OPEN_ALERTS` | AlertsToggleGui |
| 21 | Server Stats | COMPARATOR | `§bServer Stats` | `OPEN_STATS` | ServerStatsGui |
| 22 | Últimas Flags | PAPER | `§fÚltimas Flags` | `OPEN_FLAGS` | RecentFlagsGui |
| 23 | About | NETHER_STAR | `§6About` | `OPEN_ABOUT` | AboutGui |
| 29 | Check Manager | COMMAND_BLOCK | `§cCheck Manager` | `OPEN_CHECK_MANAGER` | CheckManagerGui |
| 30 | Bulk Actions | REDSTONE | `§cBulk Actions` | `OPEN_BULK` | BulkActionsGui |
| 31 | Formato Alertas | WRITABLE_BOOK | `§cFormato Alertas` | `OPEN_ALERT_FORMAT` | AlertFormatGui |

> **Slots admin (29, 30, 31):** Si el jugador NO tiene `assassin.admin`, se reemplazan con `GuiBorder` (glass pane sin acción).

---

## 2. CategoryGui

**Archivo:** `gui/screen/CategoryGui.java`  
**Título:** `⚔ ASSASSIN — {Categoría}`  
**Tamaño:** 4 filas × 9 cols = 36 slots  
**Parámetro:** `CheckCategory category`

### Layout visual

```
 0   1   2   3   4   5   6   7   8
[B] [B] [B] [B] [B] [B] [B] [B] [B]   ← fila 1 (border)

 9  10  11  12  13  14  15  16  17
[B] [X] [X] [X] [X] [X] [X] [X] [B]   ← fila 2 (checks, max 7)

18  19  20  21  22  23  24  25  26
[B] [X] [X] [X] [X] [X] [X] [X] [B]   ← fila 3 (checks continúa)

27  28  29  30  31  32  33  34  35
[B] [◀] [B] [B] [B] [B] [B] [↺] [B]   ← fila 4 (navegación)
```

### Tabla de slots

| Slot | Item | Material | Nombre | Click izquierdo | Click derecho |
|------|------|----------|--------|-----------------|---------------|
| 0–8, 9, 17, 18, 26, 27, 29–33, 35 | Border | RED_STAINED_GLASS_PANE | `§7 ` | — | — |
| 10–16, 19–25 | Check dinámico | LIME_DYE (activo) / GRAY_DYE (inactivo) | `§f{check.displayName}` | Toggle ON/OFF | Abrir config del check |
| 28 | Volver | ARROW | `§7Volver` | `OPEN_MAIN` | — |
| 34 | Reset VLs | CLOCK | `§aReset VLs` | Reset VLs de categoría | — |

**Lore de check dinámico:**
```
§7{check.description}

§7VL: §e{vl}§7/§c{maxVl}
§7Estado: §aACTIVO  ← o →  §cDESACTIVADO

§eClick: Toggle  ·  §bDerecho: Configurar
```

**PersistentDataContainer en cada check-slot:**
- `assasin:check_name` → `String` (nombre interno del check, e.g. `"KA_ANGLE"`)
- `assasin:gui_action` → `String` (`"TOGGLE_CHECK"`)

> Los checks con `enabled=true` llevan enchant glow (`ItemFlag.HIDE_ENCHANTS` + encantamiento dummy).

---

## 3. AlertsToggleGui

**Archivo:** `gui/screen/AlertsToggleGui.java`  
**Título:** `🔔 ASSASSIN — Configurar Alertas`  
**Tamaño:** 3 filas × 9 cols = 27 slots

### Layout visual

```
 0   1   2   3   4   5   6   7   8
[B] [B] [B] [B] [B] [B] [B] [B] [B]   ← fila 1 (border)

 9  10  11  12  13  14  15  16  17
[B] [X] [X] [X] [X] [X] [B] [B] [B]   ← fila 2 (canales)

18  19  20  21  22  23  24  25  26
[B] [◀] [B] [B] [B] [B] [B] [B] [B]   ← fila 3 (navegación)
```

### Tabla de slots

| Slot | Canal | Material (ON) | Material (OFF) | Nombre | `gui_action` |
|------|-------|---------------|----------------|--------|--------------|
| 0–8, 9, 16–17, 18, 20–26 | Border | RED_STAINED_GLASS_PANE | — | `§7 ` | `NONE` |
| 10 | Chat | GREEN_DYE | RED_DYE | `§fChat` | `TOGGLE_ALERT_CHAT` |
| 11 | Action Bar | GREEN_DYE | RED_DYE | `§fAction Bar` | `TOGGLE_ALERT_ACTIONBAR` |
| 12 | Title | GREEN_DYE | RED_DYE | `§fTitle` | `TOGGLE_ALERT_TITLE` |
| 13 | Sound | NOTE_BLOCK | RED_DYE | `§fSound` | `TOGGLE_ALERT_SOUND` |
| 14 | Discord | PAPER | RED_DYE | `§fDiscord` | `TOGGLE_ALERT_DISCORD` |
| 19 | Volver | ARROW | — | `§7Volver` | `OPEN_MAIN` |

**Lore por canal:**
```
§7{descripción del canal}

§aACTIVO   ← si enabled
§cDESACTIVADO   ← si disabled

§eClick para toggle
```

**Persistencia:** Al hacer toggle → actualiza `assasin_alert_preferences` (DB) + Caffeine cache (expireAfterAccess 10 min).  
Sonido: ON → `PLING`, OFF → `BASS`.

---

## 4. CheckManagerGui

**Archivo:** `gui/screen/CheckManagerGui.java`  
**Título:** `⚙ ASSASSIN — Check Manager [Pág. {n}/{total}]`  
**Tamaño:** 6 filas × 9 cols = 54 slots  
**Requiere:** `assassin.admin`  
**Paginado:** `PaginationBar.java`

### Layout visual

```
 0   1   2   3   4   5   6   7   8
[B] [B] [B] [B] [B] [B] [B] [B] [B]   ← fila 1 (border)

 9  10  11  12  13  14  15  16  17
[B] [X] [X] [X] [X] [X] [X] [X] [B]   ← fila 2 (checks)

18  19  20  21  22  23  24  25  26
[B] [X] [X] [X] [X] [X] [X] [X] [B]   ← fila 3 (checks)

27  28  29  30  31  32  33  34  35
[B] [X] [X] [X] [X] [X] [X] [X] [B]   ← fila 4 (checks)

36  37  38  39  40  41  42  43  44
[B] [X] [X] [X] [X] [X] [X] [X] [B]   ← fila 5 (checks)

45  46  47  48  49  50  51  52  53
[B] [◀] [B] [B] [B] [B] [B] [▶] [↺]   ← fila 6 (paginación + acciones)
```

### Slots de contenido (checks)

Slots disponibles para checks: `10–16, 19–25, 28–34, 37–43` = **28 slots por página**

| Slot | Contenido | Material (ON/OFF) | `gui_action` |
|------|-----------|-------------------|--------------|
| 10–16, 19–25, 28–34, 37–43 | Check dinámico | LIME_STAINED_GLASS_PANE / RED_STAINED_GLASS_PANE | `TOGGLE_CHECK` |
| 0–8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 47–51 | Border | RED_STAINED_GLASS_PANE | `NONE` |
| 46 | Página anterior | ARROW | `PAGE_PREV` (gris si pág. 1) |
| 52 | Página siguiente | ARROW | `PAGE_NEXT` (gris si última pág.) |
| 53 | Reset ALL VLs | BARRIER | `RESET_ALL_VL` |

**Lore del check en CheckManager:**
```
§7Categoría: §e{category.displayName}
§7Descripción: §7{check.description}

§7VL Actual: §c{vl}  ·  §7Máx: §c{maxVl}
§7Estado: §aACTIVO  /  §cDESACTIVADO

§eClick: Toggle
```

**PaginationBar:** Calcula `totalPages = ceil(totalChecks / 28)`. Slots 46 y 52 se deshabilitan (glass pane) si no hay página previa/siguiente.

---

## 5. ServerStatsGui

**Archivo:** `gui/screen/ServerStatsGui.java`  
**Título:** `📊 ASSASSIN — Server Stats`  
**Tamaño:** 4 filas × 9 cols = 36 slots

### Layout visual

```
 0   1   2   3   4   5   6   7   8
[B] [B] [B] [B] [B] [B] [B] [B] [B]

 9  10  11  12  13  14  15  16  17
[B] [X] [X] [X] [X] [X] [B] [B] [B]

18  19  20  21  22  23  24  25  26
[B] [B] [B] [B] [B] [B] [B] [B] [B]

27  28  29  30  31  32  33  34  35
[B] [◀] [B] [B] [B] [B] [B] [↺] [B]
```

### Tabla de slots

| Slot | Stat | Material | Nombre | Lore (resumen) |
|------|------|----------|--------|----------------|
| 10 | TPS | COMPARATOR | `§aTPS` | 1m / 5m / 15m tick rates |
| 11 | Memoria | ENDER_CHEST | `§bMemoria` | Usada / Máx / Libre / GC Runs |
| 12 | CPU | REDSTONE | `§eCPU` | Uso % / Threads / Uptime |
| 13 | Flags 24h | CLOCK | `§cFlags 24h` | Total / Jugadores únicos / Pico hora |
| 14 | Top Checks | BOOK | `§6Top Checks` | Top 5 checks por violaciones |
| 28 | Volver | ARROW | `§7Volver` | `OPEN_MAIN` |
| 34 | Refrescar | RECOVERY_COMPASS | `§aRefrescar` | Re-renders la GUI con datos frescos |

> `Refrescar` llama a `gui.refresh()` → diff de slots modificados (no reconstruye toda la GUI).

---

## 6. AboutGui

**Archivo:** `gui/screen/AboutGui.java`  
**Título:** `⭐ ASSASSIN — About`  
**Tamaño:** 3 filas × 9 cols = 27 slots

### Layout visual

```
 0   1   2   3   4   5   6   7   8
[B] [B] [B] [B] [B] [B] [B] [B] [B]

 9  10  11  12  13  14  15  16  17
[B] [B] [B] [B] [★] [B] [B] [B] [B]

18  19  20  21  22  23  24  25  26
[B] [◀] [B] [B] [B] [B] [B] [B] [B]
```

### Tabla de slots

| Slot | Item | Material | Nombre |
|------|------|----------|--------|
| 0–8, 9–12, 14–17, 18, 20–26 | Border | RED_STAINED_GLASS_PANE | `§7 ` |
| 13 | Nether Star (encantado) | NETHER_STAR | `§6§lASSASSIN` |
| 19 | Volver | ARROW | `§7Volver` |

**Lore del Nether Star (slot 13):**
```
§fAnticheat for Minecraft 1.21.1

§7Version: §a{plugin.version}
§7Author: §bTyouDm
§7Build: §f#{buildNumber}

§7Checks registrados: §e{totalChecks}
§7Flags hoy: §c{todayFlags}

§aThanks for using ASSASSIN!
```

> Nether Star lleva enchant glow con `ItemFlag.HIDE_ENCHANTS`. No tiene acción (`gui_action = NONE`).

---

## 7. RecentFlagsGui

**Archivo:** `gui/screen/RecentFlagsGui.java`  
**Título:** `📋 ASSASSIN — Flags Recientes`  
**Tamaño:** 5 filas × 9 cols = 45 slots

### Layout visual

```
 0   1   2   3   4   5   6   7   8
[B] [B] [B] [B] [B] [B] [B] [B] [B]

 9  10  11  12  13  14  15  16  17
[B] [X] [X] [X] [X] [X] [X] [X] [B]

18  19  20  21  22  23  24  25  26
[B] [X] [X] [X] [X] [X] [X] [X] [B]

27  28  29  30  31  32  33  34  35
[B] [X] [X] [X] [X] [X] [X] [X] [B]

36  37  38  39  40  41  42  43  44
[B] [◀] [B] [B] [B] [B] [B] [🗑] [B]
```

### Tabla de slots

| Slot | Contenido | Material (por VL) | `gui_action` |
|------|-----------|-------------------|--------------|
| 0–8, 9, 17, 18, 26, 27, 35, 36, 38–42, 44 | Border | RED_STAINED_GLASS_PANE | `NONE` |
| 10–16, 19–25, 28–34 | Flag dinámica | ver tabla color | `FLAG_TELEPORT` (click) / `FLAG_BAN` (derecho) |
| 37 | Volver | ARROW | `OPEN_MAIN` |
| 43 | Limpiar historial | BARRIER | `CLEAR_FLAGS` |

**Color del item de flag según VL:**

| VL | Material | Color nombre |
|----|----------|-------------|
| ≥ 40 | RED_WOOL | `§c{player}` |
| 20–39 | ORANGE_WOOL | `§6{player}` |
| < 20 | YELLOW_WOOL | `§e{player}` |

**Lore de flag:**
```
§7Check: §e{check.displayName}
§7VL: §c{vl}
§7Ping: §f{ping}ms
§7Hora: §7{timestamp}

§eClick: Teletransportar al jugador
§bDerecho: Banear jugador
```

---

## 8. AlertFormatGui

**Archivo:** `gui/screen/AlertFormatGui.java`  
**Título:** `✏ ASSASSIN — Formato de Alertas`  
**Tamaño:** Written Book (no inventario)  
**Requiere:** `assassin.admin`

> Esta pantalla abre un **Written Book** directamente al jugador (no es un inventario Bukkit).  
> Se usa `player.openBook(ItemStack book)`.

**Placeholders disponibles (escritos en el libro):**

| Placeholder | Descripción |
|-------------|-------------|
| `{player}` | Nombre del jugador |
| `{check}` | Nombre del check |
| `{vl}` | Violation level actual |
| `{maxvl}` | VL máximo del check |
| `{ping}` | Ping del jugador |
| `{world}` | Mundo donde ocurrió |
| `{x}`, `{y}`, `{z}` | Coordenadas |
| `{time}` | Hora del flag |
| `{server}` | Nombre del servidor |

> El formato editado se guarda en `config.yml` bajo `alerts.format` y se recarga en caliente.

---

## 9. BulkActionsGui

**Archivo:** `gui/screen/BulkActionsGui.java`  
**Título:** `⚙ ASSASSIN — Bulk Actions`  
**Tamaño:** 3 filas × 9 cols = 27 slots  
**Requiere:** `assassin.admin`

### Layout visual

```
 0   1   2   3   4   5   6   7   8
[B] [B] [B] [B] [B] [B] [B] [B] [B]

 9  10  11  12  13  14  15  16  17
[B] [✓] [✗] [B] [↺] [B] [💾] [📂] [B]

18  19  20  21  22  23  24  25  26
[B] [◀] [B] [B] [B] [B] [B] [B] [B]
```

### Tabla de slots

| Slot | Acción | Material | Nombre | `gui_action` |
|------|--------|----------|--------|--------------|
| 0–8, 9, 12, 14, 17–18, 20–26 | Border | RED_STAINED_GLASS_PANE | `§7 ` | `NONE` |
| 10 | Enable ALL | LIME_BLOCK | `§aEnable ALL` | `BULK_ENABLE_ALL` |
| 11 | Disable ALL | RED_BLOCK | `§cDisable ALL` | `BULK_DISABLE_ALL` |
| 13 | Reset ALL VLs | CLOCK | `§eReset ALL VLs` | `RESET_ALL_VL` |
| 15 | Guardar Config | ENDER_CHEST | `§bGuardar Config` | `CONFIG_SAVE` |
| 16 | Recargar Config | RECOVERY_COMPASS | `§bRecargar Config` | `CONFIG_RELOAD` |
| 19 | Volver | ARROW | `§7Volver` | `OPEN_MAIN` |

---

## Componentes compartidos

### GuiBorder — `gui/component/GuiBorder.java`

Rellena el perímetro de cualquier GUI con Red Stained Glass Pane.

```java
// Uso:
GuiBorder.apply(inventory); // rellena slots de borde automáticamente según filas
```

Lógica: slot en borde si `slot < cols`, `slot >= (rows-1)*cols`, `slot % cols == 0`, o `slot % cols == cols-1`.

---

### GuiItem — `gui/component/GuiItem.java`

Wrapper de `ItemStack` con acción y lore builder fluente.

```java
GuiItem item = new GuiItem(Material.IRON_SWORD)
    .name("§cKill Aura")
    .lore("§7Detecta ataques ilegales", "", "§eClick para abrir")
    .enchantGlow(true)
    .action(event -> openCategory(event.getWhoClicked(), CheckCategory.KILL_AURA))
    .pdc("assasin:gui_action", "OPEN_CATEGORY");
```

---

### GuiAction — `gui/component/GuiAction.java`

```java
@FunctionalInterface
public interface GuiAction extends Consumer<InventoryClickEvent> {}
```

---

### PaginationBar — `gui/component/PaginationBar.java`

| Parámetro | Descripción |
|-----------|-------------|
| `currentPage` | Página actual (0-indexed) |
| `totalPages` | Total de páginas |
| `prevSlot` | Slot del botón anterior |
| `nextSlot` | Slot del botón siguiente |
| `onPrev` | `GuiAction` al ir atrás |
| `onNext` | `GuiAction` al ir adelante |

Deshabilita botones (reemplaza con glass pane) cuando no hay página previa/siguiente.

---

### ItemBuilder — `gui/util/ItemBuilder.java`

Fluent builder con soporte para `PersistentDataContainer`:

```java
ItemStack item = new ItemBuilder(Material.BELL)
    .name("§eAlertas")
    .lore("§7Configura canales", "", "§eClick para configurar")
    .pdc(plugin, "assasin:gui_action", PersistentDataType.STRING, "OPEN_ALERTS")
    .build();
```

---

## Persistencia y caché

| Elemento | Mecanismo |
|----------|-----------|
| Preferencias de alerta por jugador | Tabla `assasin_alert_preferences` (DB) |
| Caché en memoria | Caffeine `expireAfterAccess(10, MINUTES)` |
| Sincronización al toggle | `alertPrefsCache.put(uuid, prefs)` + async DB write |
| Cleanup al cerrar GUI | `InventoryCloseEvent` → `GuiManager.remove(uuid)` |

---

## Flujo de eventos

```
InventoryClickEvent
    └─ GuiManager.getGui(uuid)
        └─ AssasinGui.handleClick(slot, event)
            ├─ cancel event (siempre)
            ├─ play UI_BUTTON_CLICK
            ├─ GuiItem.getAction(slot).accept(event)
            └─ GuiManager.refresh(uuid) si necesario (diff parcial)

InventoryCloseEvent
    └─ GuiManager.remove(uuid)
```

---

## Tests requeridos

| Test | Descripción |
|------|-------------|
| `ClickDispatchTest` | Click en slot X dispara la `GuiAction` correcta |
| `AdminVisibilityTest` | Sin `assassin.admin` → slots 29/30/31 son glass pane |
| `DiffRefreshTest` | `refresh()` solo actualiza slots modificados (no reconstruye todo) |
| `CloseCleanupTest` | `InventoryCloseEvent` elimina la entrada del `GuiManager` |
| `PaginationTest` | Botones prev/next deshabilitados correctamente en extremos |
| `CacheExpiryTest` | Preferencias expiran en 10 min de inactividad |