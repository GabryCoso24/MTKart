package com.ideovision.karts;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Horse;
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
    private static final double KART_VERTICAL_OFFSET = 4.0;
    private static final double KART_MODEL_OFFSET = 0.65;
    private static final double NAME_MODEL_OFFSET = 2.1;
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

        Location playerLocation = player.getLocation();
        if (playerLocation == null) {
            return;
        }

        World world = playerLocation.getWorld();
        if (world == null) {
            return;
        }

        Location spawnLoc = playerLocation.clone().add(0, -0.15 + KART_VERTICAL_OFFSET, 0);

        Horse seat = world.spawn(spawnLoc, Horse.class, horse -> {
            horse.setAdult();
            horse.setTamed(true);
            horse.setSilent(true);
            horse.setInvulnerable(true);
            horse.setInvisible(true);
            horse.setCollidable(false);
            horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            horse.addPassenger(player);
        });

        // Affidabilita': in alcune situazioni il mount nello stesso tick non viene applicato.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (seat.isValid() && !seat.getPassengers().contains(player)) {
                seat.addPassenger(player);
            }
            if (seat.isValid()) {
                // Nasconde la sella visivamente mantenendo il controllo del mount gia' attivo.
                seat.getInventory().setSaddle(null);
            }
        }, 1L);

        String modelEngineModelId = getModelEngineModelId(kartType);
        boolean modelEngineActive = tryEnableModelEngineModel(seat, player, modelEngineModelId);

        ItemDisplay kartDisplay = null;
        if (!modelEngineActive) {
            ItemStack kartItem = createKartItem(kartType);
            kartDisplay = world.spawn(spawnLoc, ItemDisplay.class, display -> {
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
        }

        TextDisplay nameDisplay = null;
        if (config.getConfig().getBoolean("kart.show-name", true)) {
            Location nameLoc = spawnLoc.clone().add(0, 1.5, 0);
            nameDisplay = world.spawn(nameLoc, TextDisplay.class, display -> {
                display.setText(player.getName());
                display.setBackgroundColor(null);
                display.setBillboard(Display.Billboard.CENTER);
                display.setDefaultBackground(false);
            });
        }

        KartData kartData = new KartData(seat, kartDisplay, nameDisplay, kartType, modelEngineModelId, modelEngineActive);
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
            disableModelEngineModel(kartData, player);

            if (player.isInsideVehicle()) {
                player.leaveVehicle();
            }
            if (kartData.seat != null && kartData.seat.isValid()) {
                kartData.seat.eject();
                kartData.seat.remove();
            }
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

            if (!kartData.modelEngineActive && (kartData.kartDisplay == null || !kartData.kartDisplay.isValid())) {
                removeKart(player);
                return;
            }

            Location baseLocation = kartData.seat != null && kartData.seat.isValid()
                ? kartData.seat.getLocation()
                : player.getLocation();
            if (baseLocation == null) {
                removeKart(player);
                return;
            }
            Location kartLoc = baseLocation.clone().add(0, KART_MODEL_OFFSET, 0);

            if (kartData.kartDisplay != null && kartData.kartDisplay.isValid()) {
                kartData.kartDisplay.teleport(kartLoc);
                kartData.kartDisplay.setRotation(baseLocation.getYaw(), 0);
            }

            if (kartData.nameDisplay != null && kartData.nameDisplay.isValid()) {
                Location nameLoc = baseLocation.clone().add(0, NAME_MODEL_OFFSET, 0);
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
        if (kartData != null && location != null) {
            if (kartData.kartDisplay != null && kartData.kartDisplay.isValid()) {
                kartData.kartDisplay.teleport(location.clone().add(0, KART_MODEL_OFFSET, 0));
            }
            if (kartData.seat != null && kartData.seat.isValid()) {
                kartData.seat.teleport(location.clone().add(0, -0.15 + KART_VERTICAL_OFFSET, 0));
            }
        }
    }

    private boolean tryEnableModelEngineModel(Horse seat, Player player, String modelId) {
        if (!isModelEngineEnabled() || seat == null || !seat.isValid() || modelId == null || modelId.isBlank()) {
            return false;
        }

        String commandTemplate = config.getConfig().getString("kart.modelengine.commands.add", "meg model add {entity} {model}");
        if (commandTemplate == null || commandTemplate.isBlank()) {
            return false;
        }

        String command = commandTemplate
            .replace("{entity}", seat.getUniqueId().toString())
            .replace("{model}", modelId)
            .replace("{player}", player.getName());

        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    private void disableModelEngineModel(KartData kartData, Player player) {
        if (kartData == null || !kartData.modelEngineActive || kartData.seat == null) {
            return;
        }

        String commandTemplate = config.getConfig().getString("kart.modelengine.commands.remove", "meg model remove {entity} {model}");
        if (commandTemplate == null || commandTemplate.isBlank()) {
            return;
        }

        String modelId = kartData.modelEngineModelId != null ? kartData.modelEngineModelId : "";
        String command = commandTemplate
            .replace("{entity}", kartData.seat.getUniqueId().toString())
            .replace("{model}", modelId)
            .replace("{player}", player.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    private boolean isModelEngineEnabled() {
        return config.getConfig().getBoolean("kart.modelengine.enabled", false)
            && Bukkit.getPluginManager().getPlugin("ModelEngine") != null;
    }

    private String getModelEngineModelId(KartType kartType) {
        return switch (kartType) {
            case SPEED_KART -> config.getConfig().getString("kart.modelengine.models.speed_kart", "mtkart_speed");
            case BALANCE_KART -> config.getConfig().getString("kart.modelengine.models.balance_kart", "mtkart_balance");
            case ACCELERATION_KART -> config.getConfig().getString("kart.modelengine.models.acceleration_kart", "mtkart_acceleration");
            case DEFAULT -> config.getConfig().getString("kart.modelengine.models.default", "mtkart_default");
        };
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
        Horse seat;
        ItemDisplay kartDisplay;
        TextDisplay nameDisplay;
        KartType kartType;
        String modelEngineModelId;
        boolean modelEngineActive;

        KartData(Horse seat, ItemDisplay kartDisplay, TextDisplay nameDisplay, KartType kartType,
                 String modelEngineModelId, boolean modelEngineActive) {
            this.seat = seat;
            this.kartDisplay = kartDisplay;
            this.nameDisplay = nameDisplay;
            this.kartType = kartType;
            this.modelEngineModelId = modelEngineModelId;
            this.modelEngineActive = modelEngineActive;
        }
    }
}
