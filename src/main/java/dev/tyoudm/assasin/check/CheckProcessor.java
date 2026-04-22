/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check;

import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Dispatches checks to the correct players on each event type.
 *
 * <p>Handlers call the appropriate {@code process*} method after updating
 * {@link PlayerData}. The processor iterates the checks registered for
 * that event's category and calls {@link Check#handle(Player, PlayerData, long)}
 * on each enabled check.
 *
 * <h2>Event → Category mapping</h2>
 * <ul>
 *   <li>Movement packets  → {@link CheckCategory#MOVEMENT}</li>
 *   <li>Rotation packets  → {@link CheckCategory#COMBAT} (aim checks)</li>
 *   <li>Combat packets    → {@link CheckCategory#COMBAT}</li>
 *   <li>Combat events     → {@link CheckCategory#COMBAT}</li>
 *   <li>Block packets     → {@link CheckCategory#WORLD}</li>
 *   <li>Mount packets     → {@link CheckCategory#MOUNT}</li>
 *   <li>Inventory packets → {@link CheckCategory#PLAYER}</li>
 *   <li>Movement packets  → {@link CheckCategory#PLAYER} (bad-packets)</li>
 *   <li>Macro events      → {@link CheckCategory#MACRO}</li>
 *   <li>Misc events       → {@link CheckCategory#MISC}</li>
 * </ul>
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class CheckProcessor {

    private final CheckRegistry registry;

    // ─── Cached category lists (avoid repeated map lookups) ──────────────────

    private final List<Check> movementChecks;
    private final List<Check> combatChecks;
    private final List<Check> mountChecks;
    private final List<Check> worldChecks;
    private final List<Check> playerChecks;
    private final List<Check> macroChecks;
    private final List<Check> miscChecks;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates the processor backed by the given registry.
     *
     * @param registry the check registry
     */
    public CheckProcessor(final CheckRegistry registry) {
        this.registry       = registry;
        this.movementChecks = registry.getChecks(CheckCategory.MOVEMENT);
        this.combatChecks   = registry.getChecks(CheckCategory.COMBAT);
        this.mountChecks    = registry.getChecks(CheckCategory.MOUNT);
        this.worldChecks    = registry.getChecks(CheckCategory.WORLD);
        this.playerChecks   = registry.getChecks(CheckCategory.PLAYER);
        this.macroChecks    = registry.getChecks(CheckCategory.MACRO);
        this.miscChecks     = registry.getChecks(CheckCategory.MISC);
    }

    // ─── Dispatch methods ─────────────────────────────────────────────────────

    /**
     * Called on every movement packet (PLAYER_POSITION / PLAYER_POSITION_AND_ROTATION).
     * Runs movement checks and player bad-packet checks.
     *
     * @param player the player
     * @param data   the player's data
     * @param tick   the current server tick
     */
    public void processMovement(final Player player, final PlayerData data, final long tick) {
        run(movementChecks, player, data, tick);
        run(playerChecks,   player, data, tick);
    }

    /**
     * Called on every rotation packet (PLAYER_ROTATION / PLAYER_POSITION_AND_ROTATION).
     * Runs combat checks that rely on rotation data (aim, killaura).
     *
     * @param player the player
     * @param data   the player's data
     * @param tick   the current server tick
     */
    public void processRotation(final Player player, final PlayerData data, final long tick) {
        run(combatChecks, player, data, tick);
    }

    /**
     * Called on every INTERACT_ENTITY (attack) packet.
     * Runs combat checks.
     *
     * @param player the player
     * @param data   the player's data
     * @param tick   the current server tick
     */
    public void processCombatPacket(final Player player, final PlayerData data, final long tick) {
        run(combatChecks, player, data, tick);
    }

    /**
     * Called on EntityDamageByEntityEvent (server-side hit confirmation).
     * Runs combat checks and macro checks.
     *
     * @param player the player
     * @param data   the player's data
     * @param tick   the current server tick
     */
    public void processCombatEvent(final Player player, final PlayerData data, final long tick) {
        run(combatChecks, player, data, tick);
        run(macroChecks,  player, data, tick);
    }

    /**
     * Called on block interaction packets (place / break).
     * Runs world checks.
     *
     * @param player the player
     * @param data   the player's data
     * @param tick   the current server tick
     */
    public void processBlock(final Player player, final PlayerData data, final long tick) {
        run(worldChecks, player, data, tick);
    }

    /**
     * Called on mount-related packets (steer vehicle, etc.).
     * Runs mount checks.
     *
     * @param player the player
     * @param data   the player's data
     * @param tick   the current server tick
     */
    public void processMount(final Player player, final PlayerData data, final long tick) {
        run(mountChecks, player, data, tick);
    }

    /**
     * Called on inventory-related packets (click, close, etc.).
     * Runs player checks.
     *
     * @param player the player
     * @param data   the player's data
     * @param tick   the current server tick
     */
    public void processInventory(final Player player, final PlayerData data, final long tick) {
        run(playerChecks, player, data, tick);
    }

    /**
     * Called on macro-relevant events (action sequences, input patterns).
     * Runs macro checks.
     *
     * @param player the player
     * @param data   the player's data
     * @param tick   the current server tick
     */
    public void processMacro(final Player player, final PlayerData data, final long tick) {
        run(macroChecks, player, data, tick);
    }

    /**
     * Called on misc events (brand packet, name change, etc.).
     * Runs misc checks.
     *
     * @param player the player
     * @param data   the player's data
     * @param tick   the current server tick
     */
    public void processMisc(final Player player, final PlayerData data, final long tick) {
        run(miscChecks, player, data, tick);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private static void run(final List<Check> checks,
                            final Player player,
                            final PlayerData data,
                            final long tick) {
        for (int i = 0, n = checks.size(); i < n; i++) {
            checks.get(i).handle(player, data, tick);
        }
    }

    // ─── Accessor ─────────────────────────────────────────────────────────────

    /**
     * Returns the underlying {@link CheckRegistry}.
     *
     * @return the registry
     */
    public CheckRegistry getRegistry() {
        return registry;
    }
}
