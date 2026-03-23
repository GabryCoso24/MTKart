package com.ideovision.managers;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class StartRaceManager {
   

    public void StartTimer(String raceName){
        // Leggi l'OST dal file yml della race
        String ostName = CircuitManager.getOstFromCircuit(raceName);
        
        String[] titles = {
            ChatColor.translateAlternateColorCodes('&', "&c&l3"),
            ChatColor.translateAlternateColorCodes('&', "&6&l2"),
            ChatColor.translateAlternateColorCodes('&', "&2&l1")
        };
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(StartRaceManager.class);

        // Step 1: Countdown 3 con fade out della musica idle
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.stopSound("mtkart:idle");
            player.sendTitle(titles[0], "", 5, 15, 5);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP, SoundCategory.PLAYERS, 1.0f, 1.0f);
            
        }
        
        // Step 2: Countdown 2
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendTitle(titles[1], "", 5, 15, 5);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP, SoundCategory.PLAYERS, 1.0f, 1.5f);
            }
        }, 20L);
        
        // Step 3: Countdown 1 e fade in OST
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendTitle(titles[2], "", 5, 15, 5);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP, SoundCategory.PLAYERS, 1.0f, 2.0f);
                
                player.playSound(player, "mtkart:"+ostName, 1.0f, 1.0f);
            }
        }, 40L);
    }
    
        
}
