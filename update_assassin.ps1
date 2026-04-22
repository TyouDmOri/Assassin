# ASSASIN AntiCheat - 1.21.11 Production Ready Updater
$basePath = "src/main/java/dev/tyoudm/assasin"

function Create-File($path, $content) {
    $fullPath = Join-Path $basePath $path
    $dir = Split-Path $fullPath
    if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force }
    Set-Content -Path $fullPath -Value $content -Encoding UTF8
    Write-Host "✅ Creado/Actualizado: $path" -ForegroundColor Green
}

Write-Host "🚀 Iniciando actualización de ASSASIN v1.0.0 (1.21.11)..." -ForegroundColor Cyan

# 1. ENUMS Y ANOTACIONES
Create-File "exempt/ExemptType.java" @"
package dev.tyoudm.assasin.exempt;
public enum ExemptType {
    TELEPORT_PENDING, SETBACK, LAG_SPIKE, RESPAWN, WORLD_CHANGE,
    ELYTRA_ACTIVE, ELYTRA_BOOST, RIPTIDE, VEHICLE, LIQUID,
    CLIMBABLE, SLIME_BLOCK, ICE, JOINED, WIND_CHARGE,
    SPEAR_LUNGE, NAUTILUS_DASH, CINEMATIC_CAMERA;
}
"@

Create-File "check/CheckInfo.java" @"
package dev.tyoudm.assasin.check;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CheckInfo {
    String name();
    CheckType type();
    CheckCategory category();
    String description() default "";
    double maxVl() default 10.0;
    Severity severity() default Severity.MEDIUM;
    enum Severity { LOW, MEDIUM, HIGH, CRITICAL }
}
"@

# 2. DATA Y TRACKERS
Create-File "data/CheckData.java" @"
package dev.tyoudm.assasin.data;
public class CheckData {
    private double aimABuffer, speedBBuffer, motionABuffer, postABuffer, reachBuffer;
    public double getAimABuffer() { return aimABuffer; }
    public void setAimABuffer(double v) { this.aimABuffer = v; }
    public double getSpeedBBuffer() { return speedBBuffer; }
    public void setSpeedBBuffer(double v) { this.speedBBuffer = v; }
    public double getMotionABuffer() { return motionABuffer; }
    public void setMotionABuffer(double v) { this.motionABuffer = v; }
    public double getPostABuffer() { return postABuffer; }
    public void setPostABuffer(double v) { this.postABuffer = v; }
    public double getReachBuffer() { return reachBuffer; }
    public void setReachBuffer(double v) { this.reachBuffer = v; }
}
"@

Create-File "data/tracker/AttributeTracker.java" @"
package dev.tyoudm.assasin.data.tracker;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
public class AttributeTracker {
    private double walkSpeed = 0.1, attackRange = 3.0;
    public void handleSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.UPDATE_ATTRIBUTES) {
            WrapperPlayServerUpdateAttributes wrapper = new WrapperPlayServerUpdateAttributes(event);
            for (WrapperPlayServerUpdateAttributes.Property p : wrapper.getProperties()) {
                if (p.getKey().contains("movement_speed")) this.walkSpeed = p.getValue();
                if (p.getKey().contains("attack_range")) this.attackRange = p.getValue();
            }
        }
    }
    public double getWalkSpeed() { return walkSpeed; }
    public double getAttackRange() { return attackRange; }
}
"@

Create-File "data/tracker/VelocityTracker.java" @"
package dev.tyoudm.assasin.data.tracker;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
public class VelocityTracker {
    private int ticks;
    public void handleSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) this.ticks = 40;
    }
    public void tick() { if (ticks > 0) ticks--; }
    public boolean isExempt() { return ticks > 0; }
}
"@

# 3. CHECKS CORE
Create-File "check/Check.java" @"
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
"@

# 4. IMPLEMENTACIÓN DE CHECKS (1.21.11)
Create-File "check/impl/combat/ReachA.java" @"
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
"@

Create-File "check/impl/movement/SpeedB.java" @"
package dev.tyoudm.assasin.check.impl.movement;
import dev.tyoudm.assasin.check.*;
import dev.tyoudm.assasin.data.PlayerData;
import dev.tyoudm.assasin.exempt.ExemptType;
import dev.tyoudm.assasin.mitigation.MitigationEngine;
import org.bukkit.entity.Player;
@CheckInfo(name = "SpeedB", type = CheckType.SPEED_B, category = CheckCategory.MOVEMENT)
public final class SpeedB extends Check {
    public SpeedB(MitigationEngine engine) { super(engine); }
    @Override
    protected void process(Player player, PlayerData data, long tick) {
        if (isExemptAny(data, tick, ExemptType.TELEPORT_PENDING, ExemptType.SPEAR_LUNGE)) return;
        double speedH = data.getVelocityH();
        double lastH = data.getMovementTracker().getLastSpeedH();
        double friction = data.isOnGround() ? 0.546 : 0.91;
        double pred = (lastH * friction) + (data.getAttributeTracker().getWalkSpeed() * (data.isSprinting() ? 0.13 : 0.1));
        if (speedH > pred + 0.01) {
            double b = data.getCheckData().getSpeedBBuffer();
            if (++b > 3) flag(player, data, 1.0, "H=" + speedH + " P=" + pred, tick);
            data.getCheckData().setSpeedBBuffer(b);
        } else data.getCheckData().setSpeedBBuffer(0);
    }
}
"@

# 5. MOTOR DE MITIGACIÓN Y PROCESADOR
Create-File "mitigation/MitigationEngine.java" @"
package dev.tyoudm.assasin.mitigation;
import dev.tyoudm.assasin.Assassin;
import dev.tyoudm.assasin.check.Check;
import dev.tyoudm.assasin.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
public class MitigationEngine {
    public void handleFlag(Player player, Check check, double vl, String info, long tick) {
        Bukkit.broadcastMessage("§8[§cAssassin§8] §7" + player.getName() + " flagged §c" + check.getName() + " §8[§f" + info + "§8]");
        if (check.getCategory().name().equals("MOVEMENT") && vl > 3) {
            PlayerData data = Assassin.getInstance().getDataManager().getData(player);
            Bukkit.getScheduler().runTask(Assassin.getInstance(), () -> player.teleport(data.getMovementTracker().getLastValidLocation()));
        }
    }
}
"@

Write-Host "`n✨ ¡Arquitectura ASSASIN v1.0.0 (1.21.11) lista para compilar!" -ForegroundColor Yellow
Write-Host "Recuerda registrar el PacketProcessor en tu clase Main." -ForegroundColor White