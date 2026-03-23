package com.ideovision.managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class StopRaceManager {

    public static void stopRace(String raceName) {
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(StopRaceManager.class);

        String ostName = CircuitManager.getOstFromCircuit(raceName);
        String ostSoundId = null;
        if (ostName != null && !ostName.trim().isEmpty()) {
            String cleaned = ostName.trim();
            ostSoundId = cleaned.contains(":") ? cleaned : "mtkart:" + cleaned;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            // Stop common race-related sounds.
            player.stopSound("mtkart:3");
            player.stopSound("mtkart:2");
            player.stopSound("mtkart:1");
            player.stopSound("mtkart:go");
            if (ostSoundId != null) {
                player.stopSound(ostSoundId);
            }

            player.sendTitle(ChatColor.translateAlternateColorCodes('&', "&c&lRACE STOPPED"), "", 5, 30, 10);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cLa gara '&e" + raceName + "&c' e' stata fermata."));
        }

        plugin.getLogger().info(String.format("[StopRaceManager] Gara '%s' fermata", raceName));
        LapsManager.stopTracking();
    }
}