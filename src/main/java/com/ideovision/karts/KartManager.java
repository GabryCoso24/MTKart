package com.ideovision.karts;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import com.ideovision.MTKart;
import com.ideovision.config.PluginConfig;

public class KartManager {
    private final MTKart plugin;
    private final PluginConfig config;
    private final Map<UUID, KartData> playerKarts = new HashMap<>();

    public KartManager(MTKart plugin) {
        this.plugin = plugin;
        this.config = PluginConfig.getInstance(plugin);
    }

    /**
     * Equipaggia un kart al giocatore
     */
    public void equipKart(Player player, KartType kartType) {
        removeKart(player);

        Location spawnLoc = player.getLocation().clone().add(0, -0.5, 0);

        ItemStack kartItem = createKartItem(kartType);
        ItemDisplay kartDisplay = spawnLoc.getWorld().spawn(spawnLoc, ItemDisplay.class, display -> {
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            display.setItemStack(kartItem);
            display.setGlowing(true);
            display.setBillboard(Display.Billboard.FIXED);

            Transformation transformation = new Transformation(
                new Vector3f(0, 0, 0),
                new AxisAngle4f((float) Math.toRadians(-90), 1, 0, 0),
                new Vector3f(1.2f, 1.2f, 1.2f),
                new AxisAngle4f(0, 0, 0, 0)
            );
            display.setTransformation(transformation);
        });

        TextDisplay nameDisplay = null;
        if (config.getConfig().getBoolean("kart.show-name", true)) {
            Location nameLoc = spawnLoc.clone().add(0, 1.5, 0);
            nameDisplay = nameLoc.getWorld().spawn(nameLoc, TextDisplay.class, display -> {
                display.setText(player.getName());
                display.setBackgroundColor(null);
                display.setBillboard(Display.Billboard.CENTER);
                display.setDefaultBackground(false);
            });
        }

        KartData kartData = new KartData(kartDisplay, nameDisplay, kartType);
        playerKarts.put(player.getUniqueId(), kartData);

        followTask(player, kartData);
    }

    private ItemStack createKartItem(KartType kartType) {
        Material baseMaterial = Material.MINECART;

        ItemStack item = new ItemStack(baseMaterial);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            int modelId = config.getKartModelId(kartType.getConfigName());
            meta.setCustomModelData(modelId);
            meta.setDisplayName(kartType.getDisplayName());
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Rimuove il kart dal giocatore
     */
    public void removeKart(Player player) {
        KartData kartData = playerKarts.remove(player.getUniqueId());
        if (kartData != null) {
            if (kartData.kartDisplay != null && kartData.kartDisplay.isValid()) {
                kartData.kartDisplay.remove();
            }
            if (kartData.nameDisplay != null && kartData.nameDisplay.isValid()) {
                kartData.nameDisplay.remove();
            }
        }
    }

    /**
     * Fa seguire il kart al giocatore
     */
    private void followTask(Player player, KartData kartData) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || !player.isValid()) {
                removeKart(player);
                return;
            }

            if (kartData.kartDisplay == null || !kartData.kartDisplay.isValid()) {
                removeKart(player);
                return;
            }

            Location playerLoc = player.getLocation();
            Location kartLoc = playerLoc.clone().add(0, -0.5, 0);

            kartData.kartDisplay.teleport(kartLoc);
            kartData.kartDisplay.setRotation(playerLoc.getYaw(), 0);

            if (kartData.nameDisplay != null && kartData.nameDisplay.isValid()) {
                Location nameLoc = playerLoc.clone().add(0, 1.8, 0);
                kartData.nameDisplay.teleport(nameLoc);
                kartData.nameDisplay.setText(player.getName());
            }

        }, 1L, 1L);
    }

    /**
     * Teletrasporta il kart a una posizione
     */
    public void teleportKart(Player player, Location location) {
        KartData kartData = playerKarts.get(player.getUniqueId());
        if (kartData != null && kartData.kartDisplay != null && kartData.kartDisplay.isValid()) {
            kartData.kartDisplay.teleport(location.clone().add(0, -0.5, 0));
        }
    }

    public boolean hasKart(Player player) {
        return playerKarts.containsKey(player.getUniqueId());
    }

    public KartType getKartType(Player player) {
        KartData kartData = playerKarts.get(player.getUniqueId());
        return kartData != null ? kartData.kartType : KartType.DEFAULT;
    }

    public void clearAllKarts() {
        for (UUID uuid : playerKarts.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                removeKart(player);
            }
        }
        playerKarts.clear();
    }

    private static class KartData {
        ItemDisplay kartDisplay;
        TextDisplay nameDisplay;
        KartType kartType;

        KartData(ItemDisplay kartDisplay, TextDisplay nameDisplay, KartType kartType) {
            this.kartDisplay = kartDisplay;
            this.nameDisplay = nameDisplay;
            this.kartType = kartType;
        }
    }
}
