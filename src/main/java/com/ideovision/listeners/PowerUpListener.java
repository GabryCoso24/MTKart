package com.ideovision.listeners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.ideovision.MTKart;
import com.ideovision.powerups.PowerUp;
import com.ideovision.powerups.PowerUpType;
import com.ideovision.powerups.PowerUpUseEvent;

public class PowerUpListener implements Listener {

    private final MTKart plugin;

    // Mappa per tracciare quale power-up è associato a ogni item
    private final Map<Integer, PowerUpType> itemToPowerUp = new HashMap<>();

    public PowerUpListener(MTKart plugin) {
        this.plugin = plugin;

        // Inizializza la mappatura CustomModelData -> PowerUpType
        for (PowerUpType type : PowerUpType.values()) {
            itemToPowerUp.put(type.getModelId(), type);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR ||
            event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();

            if (item == null || item.getType() == Material.AIR) {
                return;
            }

            if (!item.hasItemMeta()) {
                return;
            }

            int modelData = item.getItemMeta().getCustomModelData();
            if (modelData == 0) {
                return;
            }

            PowerUpType powerUpType = itemToPowerUp.get(modelData);
            if (powerUpType == null) {
                return;
            }

            // Previeni il comportamento default
            event.setCancelled(true);

            // Crea e usa il power-up
            PowerUp powerUp = new PowerUp(powerUpType, plugin);
            PowerUpUseEvent useEvent = new PowerUpUseEvent(player, powerUp);
            plugin.getServer().getPluginManager().callEvent(useEvent);

            if (!useEvent.isCancelled()) {
                powerUp.use(player);

                // Rimuovi l'item dall'inventario
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(null);
                }

                plugin.getPowerUpManager().removePowerUp(player, powerUpType);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();

            if (item == null || !item.hasItemMeta()) {
                return;
            }

            int modelData = item.getItemMeta().getCustomModelData();
            if (modelData == 0) {
                return;
            }

            // Se è un power-up, previeni lo spostamento nell'inventario craft
            if (itemToPowerUp.containsKey(modelData)) {
                if (event.getSlot() == 0 && event.getInventory().getType() == org.bukkit.event.inventory.InventoryType.CRAFTING) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
