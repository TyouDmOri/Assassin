package dev.tyoudm.assasin.check;

import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

public abstract class Check {
    protected final MitigationEngine engine;

    public Check(MitigationEngine engine) {
        this.engine = engine;
    }

    // Método que cada check debe implementar con su lógica
    protected abstract void process(Player player, PlayerData data, long tick);

    // Verifica si el jugador está exento de este check en este momento
    protected boolean isExemptAny(PlayerData data, long tick, ExemptType... types) {
        for (ExemptType type : types) {
            if (data.getExemptManager().isExempt(type, tick)) return true;
        }
        return false;
    }

    // Envía una alerta si se detecta algo sospechoso
    protected void flag(Player player, PlayerData data, double vl, String info, long tick) {
        engine.handleFlag(player, this, vl, info, tick);
    }

    // Métodos de utilidad para obtener info de la anotación @CheckInfo
    public String getName() { return this.getClass().getAnnotation(CheckInfo.class).name(); }
    public CheckCategory getCategory() { return this.getClass().getAnnotation(CheckInfo.class).category(); }
    public double getMaxVl() { return this.getClass().getAnnotation(CheckInfo.class).maxVl(); }
}