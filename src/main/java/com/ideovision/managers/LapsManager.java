package com.ideovision.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class LapsManager {

    private static final Map<UUID, Integer> PLAYER_LAPS = new HashMap<>();
    private static final Map<UUID, Boolean> PLAYER_IN_FINISH_ZONE = new HashMap<>();
    private static Integer taskId = null;

    public static void startTracking(String race) {
        stopTracking();

        int laps = CircuitManager.getLapsFromCircuit(race);
        Location endLocation = CircuitManager.getEndLocation(race);
        World endWorld = endLocation != null ? endLocation.getWorld() : null;
        if (laps <= 0 || endLocation == null || endWorld == null) {
            return;
        }
        Location finishLine = Objects.requireNonNull(endLocation);

        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(StartRaceManager.class);
        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!player.getWorld().equals(endWorld)) {
                    continue;
                }

                Location playerLocation = player.getLocation();
                if (playerLocation == null) {
                    continue;
                }
                boolean inFinishZone = playerLocation.distance(finishLine) <= 3.0;
                boolean wasInFinishZone = PLAYER_IN_FINISH_ZONE.getOrDefault(player.getUniqueId(), false);

                if (inFinishZone && !wasInFinishZone) {
                    int currentLap = PLAYER_LAPS.getOrDefault(player.getUniqueId(), 0) + 1;
                    PLAYER_LAPS.put(player.getUniqueId(), currentLap);
                    player.sendMessage("Current Lap: " + currentLap + "/" + laps);

                    if (currentLap >= laps) {
                        StopRaceManager.stopRace(race);
                        player.sendMessage("Race ended");
                        stopTracking();
                        return;
                    }
                }

                PLAYER_IN_FINISH_ZONE.put(player.getUniqueId(), inFinishZone);
            }
        }, 0L, 5L);
    }

    public static void stopTracking() {
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(StartRaceManager.class);
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = null;
        }
        PLAYER_LAPS.clear();
        PLAYER_IN_FINISH_ZONE.clear();
    }

    public static void lapsCounter(String race){
        startTracking(race);
    }
}
