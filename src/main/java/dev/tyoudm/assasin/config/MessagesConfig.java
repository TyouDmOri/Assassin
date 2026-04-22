/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Typed accessor for {@code messages.yml}.
 *
 * <p>All messages support MiniMessage format and placeholder substitution.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class MessagesConfig {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final FileConfiguration cfg;
    private final String            prefix;

    public MessagesConfig(final FileConfiguration cfg) {
        this.cfg    = cfg;
        this.prefix = cfg.getString("prefix", "<color:#8B0000><bold>[ASSASIN]</bold></color> ");
    }

    // ─── Raw string accessors ─────────────────────────────────────────────────

    public String getRaw(final String key, final String def) {
        return cfg.getString(key, def);
    }

    // ─── Component builders ───────────────────────────────────────────────────

    /**
     * Returns the message at {@code key} as an Adventure {@link Component},
     * with the standard prefix prepended.
     *
     * @param key          the message key (e.g., "general.no-permission")
     * @param def          default MiniMessage string
     * @param resolvers    optional placeholder resolvers
     * @return the formatted component
     */
    public Component get(final String key, final String def,
                         final TagResolver... resolvers) {
        final String raw = prefix + cfg.getString(key, def);
        return MM.deserialize(raw, resolvers);
    }

    /**
     * Returns a message with a single {@code {player}} placeholder.
     *
     * @param key        the message key
     * @param def        default string
     * @param playerName the player name
     * @return formatted component
     */
    public Component getWithPlayer(final String key, final String def,
                                   final String playerName) {
        return get(key, def, Placeholder.unparsed("player", playerName));
    }

    // ─── Common messages ──────────────────────────────────────────────────────

    public Component noPermission() {
        return get("general.no-permission", "<color:#FF3333>✖ No permission.</color>");
    }

    public Component playerNotFound(final String name) {
        return getWithPlayer("general.player-not-found",
            "<color:#AAAAAA>Player '<white>{player}</white>' not found.</color>", name);
    }

    public Component reloaded(final String target) {
        return get("general.reloaded", "<color:#228B22>Reloaded: {target}.</color>",
            Placeholder.unparsed("target", target));
    }

    public String getKickMessage() {
        return cfg.getString("mitigation.kick",
            "You have been disconnected from the server.");
    }
}
