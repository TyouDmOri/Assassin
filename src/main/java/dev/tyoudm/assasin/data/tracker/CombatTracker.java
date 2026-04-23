package dev.tyoudm.assasin.data.tracker;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class CombatTracker {
    private int targetId;
    private double lastAttackedDistance;
    private long lastAttackTime;
    private boolean attacking;

    public void handleInteract(PacketReceiveEvent event) {
        WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
        
        if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
            this.targetId = wrapper.getEntityId();
            this.lastAttackTime = System.currentTimeMillis();
            this.attacking = true;

            // Calcular distancia (Simplificado para el ejemplo)
            Player player = (Player) event.getPlayer();
            Entity target = null;
            
            // Buscar la entidad por ID (puedes optimizar esto con una caché)
            for (Entity entity : player.getNearbyEntities(6, 6, 6)) {
                if (entity.getEntityId() == targetId) {
                    target = entity;
                    break;
                }
            }

            if (target != null) {
                this.lastAttackedDistance = player.getLocation().distance(target.getLocation());
            }
        } else {
            this.attacking = false;
        }
    }

    public double getLastAttackedDistance() { return lastAttackedDistance; }
    public boolean isAttacking() { return attacking; }
    public long getLastAttackTime() { return lastAttackTime; }
}