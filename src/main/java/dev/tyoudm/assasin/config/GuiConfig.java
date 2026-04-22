/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Typed accessor for {@code gui.yml}.
 *
 * <p>Provides slot assignments and material names for all GUI screens.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class GuiConfig {

    private final FileConfiguration cfg;

    public GuiConfig(final FileConfiguration cfg) {
        this.cfg = cfg;
    }

    // ─── Border ───────────────────────────────────────────────────────────────

    public Material getBorderMaterial() {
        return parseMaterial(cfg.getString("border.material", "RED_STAINED_GLASS_PANE"),
            Material.RED_STAINED_GLASS_PANE);
    }

    // ─── Main GUI ─────────────────────────────────────────────────────────────

    public String getMainTitle()          { return cfg.getString("main-gui.title", "⚔ ASSASIN — Panel Principal"); }
    public int    getMainRows()           { return cfg.getInt("main-gui.rows", 6); }
    public int    getMainSlot(String key) { return cfg.getInt("main-gui.slots." + key, -1); }

    // ─── Category GUI ─────────────────────────────────────────────────────────

    public String getCategoryTitlePrefix()  { return cfg.getString("category-gui.title-prefix", "⚔ ASSASIN — "); }
    public int    getCategoryRows()         { return cfg.getInt("category-gui.rows", 4); }
    public int    getCategoryBackSlot()     { return cfg.getInt("category-gui.back-slot", 28); }
    public int    getCategoryResetSlot()    { return cfg.getInt("category-gui.reset-vl-slot", 34); }

    public Material getCategoryEnabledMaterial() {
        return parseMaterial(cfg.getString("category-gui.material-enabled", "LIME_DYE"), Material.LIME_DYE);
    }

    public Material getCategoryDisabledMaterial() {
        return parseMaterial(cfg.getString("category-gui.material-disabled", "GRAY_DYE"), Material.GRAY_DYE);
    }

    // ─── Check Manager GUI ────────────────────────────────────────────────────

    public String getCheckManagerTitlePrefix() { return cfg.getString("check-manager-gui.title-prefix", "⚙ ASSASIN — Check Manager"); }
    public int    getCheckManagerRows()        { return cfg.getInt("check-manager-gui.rows", 6); }
    public int    getChecksPerPage()           { return cfg.getInt("check-manager-gui.checks-per-page", 28); }
    public int    getPrevSlot()                { return cfg.getInt("check-manager-gui.prev-slot", 46); }
    public int    getNextSlot()                { return cfg.getInt("check-manager-gui.next-slot", 52); }
    public int    getResetAllSlot()            { return cfg.getInt("check-manager-gui.reset-all-slot", 53); }

    // ─── About GUI ────────────────────────────────────────────────────────────

    public int getAboutStarSlot() { return cfg.getInt("about-gui.star-slot", 13); }
    public int getAboutBackSlot() { return cfg.getInt("about-gui.back-slot", 19); }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private static Material parseMaterial(final String name, final Material fallback) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (final IllegalArgumentException ex) {
            return fallback;
        }
    }
}
