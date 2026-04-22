package dev.tyoudm.assasin.check.impl.combat;

import dev.tyoudm.assasin.check.*;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;

@CheckInfo(name = "ReachA", type = CheckType.AIM_A, category = CheckCategory.COMBAT)
public final class ReachA extends Check {
    public ReachA(MitigationEngine engine) { super(engine); }
    @Override
    protected void process(Player player, PlayerData data, long tick) {
        double dist = data.getCombatTracker().getLastAttackedDistance();
        double max = data.getAttributeTracker().getAttackRange();
        if (dist > max + 0.45) {
            double b = data.getCheckData().getReachBuffer();
            if (++b > 2) flag(player, data, 1.0, "D=" + dist + " M=" + max, tick);
            data.getCheckData().setReachBuffer(b);
        } else data.getCheckData().setReachBuffer(0);
    }
}
