package com.ideovision.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.ideovision.managers.LapsManager;

public class PlayerListener implements Listener {
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.playSound(player.getEyeLocation(), "mtkart:idle", 1.0f, 1.0f);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        LapsManager.handlePlayerQuit(event.getPlayer());
    }
}