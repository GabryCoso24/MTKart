package com.ideovision.managers;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class StartRaceManager {
   

    public void StartTimer(){
        String[] titles = {
            ChatColor.translateAlternateColorCodes('&', "&c&l3"),
            ChatColor.translateAlternateColorCodes('&', "&6&l2"),
            ChatColor.translateAlternateColorCodes('&', "&2&l1")
        };
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(StartRaceManager.class);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendTitle(titles[0], "", 5, 15, 5);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendTitle(titles[1], "", 5, 15, 5);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP, SoundCategory.PLAYERS, 1.0f, 1.5f);

            }
        }, 20L);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.sendTitle(titles[2], "", 5, 15, 5);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP, SoundCategory.PLAYERS, 1.0f, 2.0f);
            }
        }, 40L);
    }
    
        
}
