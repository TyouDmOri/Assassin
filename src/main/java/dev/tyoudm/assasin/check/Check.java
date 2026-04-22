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

    protected abstract void process(Player player, PlayerData data, long tick);

    protected boolean isExempt(PlayerData data, ExemptType type, long tick) {
        return data.getExemptManager().isExempt(type, tick);
    }

    protected boolean isExemptAny(PlayerData data, long tick, ExemptType... types) {
        for (ExemptType type : types) {
            if (isExempt(data, type, tick)) return true;
        }
        return false;
    }

    protected void flag(Player player, PlayerData data, double vl, String info, long tick) {
        // Tu lógica de flag aquí
    }
}