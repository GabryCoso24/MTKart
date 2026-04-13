package com.ideovision.powerups;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ideovision.MTKart;
import com.ideovision.config.PluginConfig;
import com.ideovision.managers.CircuitManager;
import com.ideovision.managers.LapsManager;

/**
 * Gestisce gli spawner di power-up sul circuito.
 * Crea caselle item visibili (come Mario Kart) che i giocatori
 * raccolgono guidandoci attraverso.
 */
public class PowerUpSpawnerManager {
    private final MTKart plugin;
    private final PluginConfig config;

    private final List<PowerUpSpawnerData> activeSpawners = new ArrayList<>();
    private int checkTaskId = -1;

    public PowerUpSpawnerManager(MTKart plugin) {
        this.plugin = plugin;
        this.config = PluginConfig.getInstance(plugin);
    }

    /**
     * Carica le posizioni degli spawner dal file del circuito
     */
    public void loadSpawners(String raceName) {
        stopSpawners();
        List<Location> locations = CircuitManager.getPowerUpSpawners(raceName);
        if (locations == null || locations.isEmpty()) return;

        for (Location loc : locations) {
            activeSpawners.add(new PowerUpSpawnerData(loc));
        }
    }

    /**
     * Avvia tutti gli spawner: fa apparire le caselle item visibili
     * e inizia il task di controllo per la raccolta
     */
    public void startSpawners() {
        for (PowerUpSpawnerData data : activeSpawners) {
            spawnBox(data);
        }

        double collectionRadius = config.getConfig().getDouble("powerups.distribution.collection-radius", 1.8);
        double radiusSq = collectionRadius * collectionRadius;

        checkTaskId = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!LapsManager.isRacing(player)) continue;
                for (PowerUpSpawnerData data : activeSpawners) {
                    if (!data.active || data.display == null) continue;
                    if (!data.display.isValid() || data.display.isDead()) continue;

                    if (player.getWorld().equals(data.location.getWorld()) &&
                        player.getLocation().distanceSquared(data.location) <= radiusSq) {
                        collectPowerUp(player, data);
                        break;
                    }
                }
            }
        }, 5L, 5L).getTaskId();
    }

    /**
     * Genera una casella item visibile (ItemDisplay ruotante)
     */
    private void spawnBox(PowerUpSpawnerData data) {
        Location loc = data.location.clone().add(0, 0.5, 0);

        int modelId = config.getConfig().getInt("powerups.distribution.box-model-id", 3000);
        ItemStack boxItem = new ItemStack(Material.BEACON);
        ItemMeta meta = boxItem.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(modelId);
            meta.setDisplayName("§bPower-Up Box");
            boxItem.setItemMeta(meta);
        }

        ItemDisplay display = loc.getWorld().spawn(loc, ItemDisplay.class, d -> {
            d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            d.setItemStack(boxItem);
            d.setGlowing(true);
        });

        data.display = display;
        data.active = true;
        data.rotationTaskId = startRotationTask(display, loc);
    }

    /**
     * Anima la rotazione della casella item
     */
    private int startRotationTask(ItemDisplay display, Location loc) {
        final float[] angle = {0};
        return plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!display.isValid() || display.isDead()) return;
            angle[0] = (angle[0] + 6) % 360;
            Location newLoc = loc.clone();
            newLoc.setYaw(angle[0]);
            display.teleport(newLoc);
        }, 1L, 2L).getTaskId();
    }

    /**
     * Un giocatore raccoglie il power-up dalla casella
     */
    private void collectPowerUp(Player player, PowerUpSpawnerData data) {
        plugin.getPowerUpManager().giveRandomPowerUp(player);

        if (data.display != null && data.display.isValid()) {
            data.display.remove();
        }
        if (data.rotationTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(data.rotationTaskId);
        }
        data.active = false;
        data.display = null;

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.5f);
        player.getWorld().spawnParticle(Particle.END_ROD, data.location.clone().add(0, 0.5, 0), 10, 0.3, 0.3, 0.3, 0.05);

        int respawnTime = config.getPowerUpRespawnTime();
        data.respawnTaskId = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (activeSpawners.contains(data)) {
                spawnBox(data);
            }
        }, respawnTime).getTaskId();
    }

    /**
     * Ferma tutti gli spawner e pulisce le entità
     */
    public void stopSpawners() {
        if (checkTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(checkTaskId);
            checkTaskId = -1;
        }
        for (PowerUpSpawnerData data : activeSpawners) {
            if (data.display != null && data.display.isValid()) {
                data.display.remove();
            }
            if (data.rotationTaskId != -1) {
                plugin.getServer().getScheduler().cancelTask(data.rotationTaskId);
            }
            if (data.respawnTaskId != -1) {
                plugin.getServer().getScheduler().cancelTask(data.respawnTaskId);
            }
        }
        activeSpawners.clear();
    }

    /**
     * Aggiunge una posizione spawner al circuito
     */
    public void addSpawnerLocation(String raceName, Location location) {
        CircuitManager.savePowerUpSpawner(raceName, location);
    }

    /**
     * Rimuove lo spawner più vicino alla posizione data
     */
    public boolean removeNearestSpawner(String raceName, Location location) {
        return CircuitManager.removePowerUpSpawner(raceName, location);
    }

    private static class PowerUpSpawnerData {
        final Location location;
        ItemDisplay display;
        boolean active;
        int rotationTaskId = -1;
        int respawnTaskId = -1;

        PowerUpSpawnerData(Location location) {
            this.location = location;
            this.active = false;
        }
    }
}