package com.ideovision.powerups;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ideovision.MTKart;
import com.ideovision.config.PluginConfig;

public class PowerUp {
    private final PowerUpType type;
    private final MTKart plugin;
    private final PluginConfig config;

    public PowerUp(PowerUpType type, MTKart plugin) {
        this.type = type;
        this.plugin = plugin;
        this.config = PluginConfig.getInstance(plugin);
    }

    public PowerUpType getType() {
        return type;
    }

    /**
     * Crea l'ItemStack per il power-up con CustomModelData
     */
    public ItemStack toItemStack() {
        Material material = getBaseMaterial();
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setCustomModelData(type.getModelId());
            meta.setDisplayName(colorize(config.getPowerUpName(type.getName())));
            meta.setLore(getColoredLore());
            item.setItemMeta(meta);
        }

        return item;
    }

    private Material getBaseMaterial() {
        switch (type) {
            case GREEN_SHELL:
            case RED_SHELL:
                return Material.SLIME_BALL;
            case BANANA:
                return Material.GOLDEN_HOE;
            case MUSHROOM:
                return Material.RED_MUSHROOM;
            case STAR:
                return Material.NETHER_STAR;
            case LIGHTNING:
                return Material.FEATHER;
            case BLOOPER:
                return Material.INK_SAC;
            case BOB_OMB:
                return Material.TNT;
            default:
                return Material.STICK;
        }
    }

    private List<String> getColoredLore() {
        List<String> coloredLore = new ArrayList<>();
        for (String line : config.getPowerUpLore(type.getName())) {
            coloredLore.add(colorize(line));
        }
        return coloredLore;
    }

    /**
     * Usa il power-up sul giocatore
     */
    public void use(Player player) {
        PowerUpUseEvent event = new PowerUpUseEvent(player, this);
        plugin.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        switch (type) {
            case GREEN_SHELL:
                plugin.getPowerUpManager().launchProjectile(player, this, false);
                break;
            case RED_SHELL:
                plugin.getPowerUpManager().launchProjectile(player, this, true);
                break;
            case BANANA:
                plugin.getPowerUpManager().placeBanana(player.getLocation(), player);
                break;
            case MUSHROOM:
                plugin.getPowerUpManager().applyBoost(player, 60, 2.0);
                break;
            case STAR:
                plugin.getPowerUpManager().applyStarEffect(player, 100);
                break;
            case LIGHTNING:
                plugin.getPowerUpManager().applyLightning(player);
                break;
            case BLOOPER:
                plugin.getPowerUpManager().applyBlooper(player);
                break;
            case BOB_OMB:
                plugin.getPowerUpManager().launchBobOmb(player, this);
                break;
        }
    }

    private String colorize(String msg) {
        return msg.replace("&", "§");
    }
}
