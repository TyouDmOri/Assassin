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

/**
 * ClientBrandA — Client brand spoofing / empty brand detection.
 *
 * <p>Detects clients that send a spoofed or empty brand string in the
 * {@code MC|Brand} plugin channel. Legitimate vanilla clients send
 * {@code "vanilla"}; modded clients send their mod loader name.
 *
 * <p>Flags when:
 * <ul>
 *   <li>The brand is {@code null} or empty (client didn't send it).</li>
 *   <li>The brand contains non-printable or control characters.</li>
 *   <li>The brand is suspiciously long (&gt; {@link #MAX_BRAND_LENGTH}).</li>
 * </ul>
 *
 * <h2>Note</h2>
 * This check is informational — it does not kick players for using
 * modded clients. It only flags anomalous brand values that indicate
 * packet manipulation.
 *
 * @author TyouDm
 * @version 1.0.0
 */
@CheckInfo(
    name              = "ClientBrandA",
    type              = CheckType.MISC_B,
    category          = CheckCategory.MISC,
    description       = "Detects spoofed or empty client brand string.",
    maxVl             = 3.0,
    severity          = CheckInfo.Severity.MEDIUM,
    mitigationProfile = "soft"
)
public final class ClientBrandA extends Check {

    /** Maximum allowed brand string length. */
    private static final int MAX_BRAND_LENGTH = 64;

    public ClientBrandA(final MitigationEngine engine) { super(engine); }

    @Override
    protected void process(final Player player, final PlayerData data, final long tick) {
        // Per-tick check is not needed — run on brand receive via checkBrand()
    }

    /**
     * Validates the client brand string. Call when the MC|Brand channel
     * message is received.
     *
     * @param player the player
     * @param data   the player's data
     * @param brand  the brand string received (may be null)
     * @param tick   current server tick
     */
    public void checkBrand(final Player player, final PlayerData data,
                           final String brand, final long tick) {
        if (brand == null || brand.isEmpty()) {
            flag(player, data, 1.0,
                "empty/null client brand",
                tick);
            return;
        }

        if (brand.length() > MAX_BRAND_LENGTH) {
            flag(player, data, 2.0,
                String.format("brand too long: %d chars (max %d)", brand.length(), MAX_BRAND_LENGTH),
                tick);
            return;
        }

        // Check for non-printable / control characters
        for (int i = 0; i < brand.length(); i++) {
            final char c = brand.charAt(i);
            if (c < 0x20 || c == 0x7F) {
                flag(player, data, 2.0,
                    String.format("brand contains control char 0x%02X at index %d", (int) c, i),
                    tick);
                return;
            }
        }
    }
}
