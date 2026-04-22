/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.check.impl.misc;

import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.check.CheckCategory;
import dev.tyoudm.assasin.check.CheckInfo;
import dev.tyoudm.assasin.check.CheckType;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

/**
 * NameSpoofA — Invalid username character detection.
 *
 * <p>Detects players with usernames containing characters outside the
 * valid Minecraft username set. Valid usernames contain only:
 * {@code [a-zA-Z0-9_]} and must be 3–16 characters long.
 *
 * <p>Usernames with invalid characters can cause issues with:
 * <ul>
 *   <li>Chat formatting and component parsing</li>
 *   <li>Database queries (SQL injection via username)</li>
 *   <li>Log file parsing</li>
 *   <li>Plugin APIs that assume valid usernames</li>
 * </ul>
 *
 * <h2>Trigger</h2>
 * This check runs once on join (called by {@code PlayerEventHandler})
 * rather than every tick. The {@link #process} method is a no-op;
 * use {@link #checkName(Player, PlayerData, long)} directly.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "NameSpoofA",
    type              = CheckType.MISC_A,
    category          = CheckCategory.MISC,
    description       = "Detects invalid username characters (name spoof).",
    maxVl             = 3.0,
    severity          = CheckInfo.Severity.CRITICAL,
    mitigationProfile = "badpackets"
)
public final class NameSpoofA extends Check {

    /** Valid Minecraft username pattern: 3–16 alphanumeric + underscore. */
    private static final Pattern VALID_NAME = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

    public NameSpoofA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        // Per-tick check is not needed — run on join via checkName()
    }

    /**
     * Validates the player's username. Call once on join.
     *
     * @param player the player
     * @param data   the player's data
     * @param tick   current server tick
     */
    public void checkName(final Player player, final PlayerData data, final long tick) {
        final String name = player.getName();
        if (!VALID_NAME.matcher(name).matches()) {
            flag(player, data, 3.0,
                String.format("invalid username: '%s' (pattern: [a-zA-Z0-9_]{3,16})", name),
                tick);
        }
    }
}
