/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Paper plugin bootstrapper for ASSASIN.
 *
 * <p>Runs before the server is fully initialized — used to print the startup
 * banner and perform any pre-server registration (e.g., Brigadier command
 * registration via {@code LifecycleEventManager} in later phases).
 *
 * @author TyouDm
 * @version 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class AssasinBootstrap implements PluginBootstrap {

    // ─── ANSI color constants (terminal output only) ──────────────────────────

    /** ANSI deep red — used for the banner. */
    private static final String ANSI_RED    = "\u001B[38;2;139;0;0m";
    /** ANSI bright red — used for accent lines. */
    private static final String ANSI_BRED   = "\u001B[38;2;220;20;20m";
    /** ANSI bold modifier. */
    private static final String ANSI_BOLD   = "\u001B[1m";
    /** ANSI reset. */
    private static final String ANSI_RESET  = "\u001B[0m";

    // ─── ASCII banner ─────────────────────────────────────────────────────────

    private static final String[] BANNER = {
        " ",
        ANSI_RED + ANSI_BOLD +
        " ░█████╗░░██████╗░██████╗░█████╗░░██████╗██╗███╗░░██╗" + ANSI_RESET,
        ANSI_RED + ANSI_BOLD +
        " ██╔══██╗██╔════╝██╔════╝██╔══██╗██╔════╝██║████╗░██║" + ANSI_RESET,
        ANSI_RED + ANSI_BOLD +
        " ███████║╚█████╗░╚█████╗░███████║╚█████╗░██║██╔██╗██║" + ANSI_RESET,
        ANSI_RED + ANSI_BOLD +
        " ██╔══██║░╚═══██╗░╚═══██╗██╔══██║░╚═══██╗██║██║╚████║" + ANSI_RESET,
        ANSI_RED + ANSI_BOLD +
        " ██║░░██║██████╔╝██████╔╝██║░░██║██████╔╝██║██║░╚███║" + ANSI_RESET,
        ANSI_RED + ANSI_BOLD +
        " ╚═╝░░╚═╝╚═════╝░╚═════╝░╚═╝░░╚═╝╚═════╝╚═╝╚═╝░░╚══╝" + ANSI_RESET,
        " ",
        ANSI_BRED + "  Mitigation-First Server-Side AntiCheat" + ANSI_RESET,
        ANSI_BRED + "  Version : " + ANSI_BOLD + "1.0.0" + ANSI_RESET,
        ANSI_BRED + "  Target  : " + ANSI_BOLD + "Paper 1.21.11 \"Mounts of Mayhem\"" + ANSI_RESET,
        ANSI_BRED + "  Author  : " + ANSI_BOLD + "TyouDm" + ANSI_RESET,
        " "
    };

    // ─── PluginBootstrap ──────────────────────────────────────────────────────

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        printBanner(context);
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        return new AssasinPlugin();
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    /**
     * Prints the ANSI ASCII banner to the server console.
     *
     * @param context the bootstrap context providing the logger
     */
    private void printBanner(@NotNull BootstrapContext context) {
        for (final String line : BANNER) {
            context.getLogger().info(line);
        }
    }
}
