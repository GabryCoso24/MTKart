package com.ideovision.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.ideovision.MTKart;
import com.ideovision.managers.LapsManager;
import com.ideovision.race.StartingGridManager;

public class RaceListener implements Listener {

    private final MTKart plugin;

    public RaceListener(MTKart plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Se il giocatore è congelato (prima del VIA), non può muoversi
        if (plugin.getStartingGridManager().isFrozen(player)) {
            Location frozen = plugin.getStartingGridManager().getFrozenLocation(player);
            if (frozen != null) {
                // Permetti solo rotazione della testa, non movimento
                Location to = event.getTo();
                if (to != null && (to.getX() != frozen.getX() || to.getY() != frozen.getY() || to.getZ() != frozen.getZ())) {
                    Location corrected = new Location(
                        to.getWorld(),
                        frozen.getX(),
                        frozen.getY(),
                        frozen.getZ(),
                        to.getYaw(),
                        to.getPitch()
                    );
                    event.setTo(corrected);
                }
            }
            return;
        }

        if (!LapsManager.isRacing(player)) {
            return;
        }

        // Aggiorna posizione del kart quando il giocatore si muove
        if (plugin.getKartManager().hasKart(player)) {
            plugin.getKartManager().teleportKart(player, event.getTo());
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // Riduci danno da caduta durante la gara (i kart non dovrebbero subire fall damage)
            if (LapsManager.isRacing(player) && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
            }
        }
    }
}