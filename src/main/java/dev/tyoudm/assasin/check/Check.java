package dev.tyoudm.assasin.check;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;
public abstract class Check {
    protected final MitigationEngine engine;
    public Check(MitigationEngine engine) { this.engine = engine; }
    protected abstract void process(Player player, PlayerData data, long tick);
    protected boolean isExemptAny(PlayerData data, long tick, ExemptType... types) {
        for (ExemptType t : types) if (data.getExemptManager().isExempt(t, tick)) return true;
        return false;
    }
    protected void flag(Player player, PlayerData data, double vl, String info, long tick) {
        engine.handleFlag(player, this, vl, info, tick);
    }
    public CheckCategory getCategory() { return this.getClass().getAnnotation(CheckInfo.class).category(); }
    public String getName() { return this.getClass().getAnnotation(CheckInfo.class).name(); }
    public double getMaxVl() { return this.getClass().getAnnotation(CheckInfo.class).maxVl(); }
}
