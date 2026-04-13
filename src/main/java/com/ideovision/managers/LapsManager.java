package com.ideovision.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class LapsManager {

    private static final Map<UUID, Integer> PLAYER_LAPS = new HashMap<>();
    private static final Map<UUID, Boolean> PLAYER_IN_FINISH_ZONE = new HashMap<>();
    private static final Map<UUID, Location> PLAYER_LAST_LOCATION = new HashMap<>();
    private static final Set<UUID> TRACKED_PLAYERS = new HashSet<>();
    private static final Set<UUID> FINISHED_PLAYERS = new HashSet<>();

    private static final double FINISH_RADIUS = 3.0;
    private static final double MIN_DIRECTION_DOT = 0.35;
    private static final double MIN_MOVEMENT_SQUARED = 0.04;
    private static String activeRaceName = null;
    private static int activeRaceTotalLaps = 0;
    private static Integer taskId = null;

    public static void startTracking(String race) {
        stopTracking();

        int laps = CircuitManager.getLapsFromCircuit(race);
        Location startLocation = CircuitManager.getStartLocation(race);
        Location endLocation = CircuitManager.getEndLocation(race);
        World endWorld = endLocation != null ? endLocation.getWorld() : null;
        if (laps <= 0 || startLocation == null || endLocation == null || endWorld == null) {
            return;
        }

        activeRaceName = race;
        activeRaceTotalLaps = laps;

        Location finishLine = Objects.requireNonNull(endLocation);
        Vector finishDirection = finishLine.toVector().subtract(startLocation.toVector());
        finishDirection.setY(0);
        if (finishDirection.lengthSquared() == 0) {
            return;
        }
        finishDirection.normalize();

        String ostName = CircuitManager.getOstFromCircuit(race);
        final String ostSoundId;
        if (ostName != null && !ostName.trim().isEmpty()) {
            String cleaned = ostName.trim();
            ostSoundId = cleaned.contains(":") ? cleaned : "mtkart:" + cleaned;
        } else {
            ostSoundId = null;
        }

        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(StartRaceManager.class);

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            Location initialLocation = p.getLocation();
            World initialWorld = initialLocation != null ? initialLocation.getWorld() : null;
            if (initialLocation != null && initialWorld != null && initialWorld.equals(endWorld)) {
                TRACKED_PLAYERS.add(p.getUniqueId());
                PLAYER_LAST_LOCATION.put(p.getUniqueId(), initialLocation.clone());
            }
        }

        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (TRACKED_PLAYERS.isEmpty()) {
                stopTracking();
                return;
            }

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                UUID id = player.getUniqueId();

                if (!TRACKED_PLAYERS.contains(id) || FINISHED_PLAYERS.contains(id)) {
                    continue;
                }
                if (!player.getWorld().equals(endWorld)) {
                    continue;
                }

                Location playerLocation = player.getLocation();
                if (playerLocation == null || playerLocation.getWorld() == null) {
                    continue;
                }
                boolean inFinishZone = playerLocation.distance(finishLine) <= FINISH_RADIUS;
                boolean wasInFinishZone = PLAYER_IN_FINISH_ZONE.getOrDefault(id, false);

                if (inFinishZone && !wasInFinishZone) {
                    Location previousLocation = PLAYER_LAST_LOCATION.get(id);
                    World previousWorld = previousLocation != null ? previousLocation.getWorld() : null;
                    World playerWorld = playerLocation.getWorld();
                    if (previousLocation == null || previousWorld == null || !previousWorld.equals(playerWorld)) {
                        PLAYER_LAST_LOCATION.put(id, playerLocation.clone());
                        PLAYER_IN_FINISH_ZONE.put(id, inFinishZone);
                        continue;
                    }

                    Vector movement = playerLocation.toVector().subtract(previousLocation.toVector());
                    movement.setY(0);
                    if (movement.lengthSquared() < MIN_MOVEMENT_SQUARED) {
                        PLAYER_LAST_LOCATION.put(id, playerLocation.clone());
                        PLAYER_IN_FINISH_ZONE.put(id, inFinishZone);
                        continue;
                    }

                    movement.normalize();
                    double directionDot = movement.dot(finishDirection);
                    if (directionDot < MIN_DIRECTION_DOT) {
                        PLAYER_LAST_LOCATION.put(id, playerLocation.clone());
                        PLAYER_IN_FINISH_ZONE.put(id, inFinishZone);
                        continue;
                    }

                    int currentLap = PLAYER_LAPS.getOrDefault(id, 0) + 1;
                    PLAYER_LAPS.put(id, currentLap);

                    try {
                        com.ideovision.MTKart mtkart = com.ideovision.MTKart.getInstance();
                        if (mtkart != null) {
                            mtkart.getRaceManager().recordLap(player);
                        }
                    } catch (Exception e) {
                        // Plugin non ancora pronto
                    }

                    player.sendMessage("§6Giro completato: §e" + currentLap + "/" + laps);

                    if (currentLap < laps) {
                        player.playSound(player, "mtkart:lap", SoundCategory.PLAYERS, 1.0f, 1.0f);
                    } else {
                        FINISHED_PLAYERS.add(id);

                        try {
                            com.ideovision.MTKart mtkart = com.ideovision.MTKart.getInstance();
                            if (mtkart != null) {
                                int position = mtkart.getRaceManager().getPosition(player);
                                mtkart.getScoreboardManager().showFinishTitle(player, position, laps);
                                player.sendMessage("§a§lGARA COMPLETATA! §7Posizione: §e#" + position);
                            }
                        } catch (Exception e) {
                            player.sendMessage("§a§lGARA COMPLETATA!");
                        }

                        if (ostSoundId != null) {
                            player.stopSound(ostSoundId);
                        }
                        player.playSound(player, "mtkart:end_race", SoundCategory.PLAYERS, 1.0f, 1.0f);
                    }
                }

                PLAYER_IN_FINISH_ZONE.put(id, inFinishZone);
                PLAYER_LAST_LOCATION.put(id, playerLocation.clone());
            }

            boolean allFinished = true;
            for (UUID id : TRACKED_PLAYERS) {
                if (!FINISHED_PLAYERS.contains(id)) {
                    allFinished = false;
                    break;
                }
            }

            if (allFinished) {
                stopTracking();
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
        PLAYER_LAST_LOCATION.clear();
        TRACKED_PLAYERS.clear();
        FINISHED_PLAYERS.clear();
        activeRaceName = null;
        activeRaceTotalLaps = 0;
    }

    public static void lapsCounter(String race) {
        startTracking(race);
    }

    public static void handlePlayerQuit(Player player) {
        UUID id = player.getUniqueId();
        PLAYER_LAPS.remove(id);
        PLAYER_IN_FINISH_ZONE.remove(id);
        PLAYER_LAST_LOCATION.remove(id);
        TRACKED_PLAYERS.remove(id);
        FINISHED_PLAYERS.remove(id);
    }

    public static String getActiveRaceName() {
        return activeRaceName;
    }

    public static int getActiveRaceTotalLaps() {
        return activeRaceTotalLaps;
    }

    public static int getCurrentLap(Player player) {
        if (player == null) {
            return 0;
        }
        return PLAYER_LAPS.getOrDefault(player.getUniqueId(), 0);
    }

    public static boolean isRacing(Player player) {
        if (player == null) {
            return false;
        }
        return TRACKED_PLAYERS.contains(player.getUniqueId());
    }

    public static boolean hasFinished(Player player) {
        if (player == null) {
            return false;
        }
        return FINISHED_PLAYERS.contains(player.getUniqueId());
    }
}
