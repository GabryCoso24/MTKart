package com.ideovision.managers;

import java.util.Objects;

import org.bukkit.ChatColor;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class StartRaceManager {
   

    public void startTimer(String raceName){
        // Leggi l'OST dal file yml della race
        String ostName = CircuitManager.getOstFromCircuit(raceName);
        String ostSoundId = null;
        if (ostName != null && !ostName.trim().isEmpty()) {
            String cleaned = ostName.trim();
            ostSoundId = cleaned.contains(":") ? cleaned : "mtkart:" + cleaned;
        }
        
        String[] titles = {
            ChatColor.translateAlternateColorCodes('&', "&c&l3"),
            ChatColor.translateAlternateColorCodes('&', "&6&l2"),
            ChatColor.translateAlternateColorCodes('&', "&2&l1"),
            ChatColor.translateAlternateColorCodes('&', "&a&lGO!"),
        };
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(StartRaceManager.class);
        final String finalOstSoundId = ostSoundId;

        // Step 1: Countdown 3 con fade out della musica idle
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.stopSound("mtkart:idle");
            player.sendTitle(titles[0], "", 5, 15, 5);
            player.playSound(player, "mtkart:3", SoundCategory.PLAYERS, 1.0f, 1.0f);
            
        }
        
        // Step 2: Countdown 2
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendTitle(titles[1], "", 5, 15, 5);
                player.playSound(player, "mtkart:2", SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        }, 20L);
        
        // Step 3: Countdown 1
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendTitle(titles[2], "", 5, 15, 5);
                player.playSound(player, "mtkart:1", SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        }, 40L);

        // Step 3: Countdown Go e OST
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendTitle(titles[3], "", 5, 15, 5);
                player.playSound(player, "mtkart:go", SoundCategory.PLAYERS, 1.0f, 1.0f);

                if (finalOstSoundId != null) {
                    String soundId = Objects.requireNonNull(finalOstSoundId);
                    player.playSound(player, soundId, 1.0f, 1.0f);
                }
            }
        }, 60L);

        if (finalOstSoundId == null) {
            plugin.getLogger().warning(String.format("[StartRaceManager] Nessuna OST valida trovata per race '%s'", raceName));
        }
    }

    public void startRace(String raceName){
        startTimer(raceName);
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(StartRaceManager.class);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> LapsManager.startTracking(raceName), 60L);
    }
    
        
}
