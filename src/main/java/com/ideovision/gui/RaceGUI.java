package com.ideovision.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.ideovision.MTKart;
import com.ideovision.config.PluginConfig;
import com.ideovision.managers.LapsManager;

public class RaceGUI {
    private final MTKart plugin;
    private final PluginConfig config;
    private final Map<UUID, Integer> guiTasks = new HashMap<>();

    public RaceGUI(MTKart plugin) {
        this.plugin = plugin;
        this.config = PluginConfig.getInstance(plugin);
    }

    /**
     * Apre la GUI della mappa del circuito
     */
    public void openMapGUI(Player player) {
        if (!config.isMapGuiEnabled()) {
            player.sendMessage(config.getColoredMessage("map-disabled"));
            return;
        }

        int rows = config.getMapGuiRows();
        String title = colorize(config.getMapGuiTitle());
        Inventory gui = Bukkit.createInventory(new MapHolder(), rows * 9, title);

        fillBackground(gui, rows);

        ItemStack mapItem = createMapItem();
        gui.setItem(rows * 9 / 2, mapItem);

        updatePlayerIndicators(gui, rows);

        player.openInventory(gui);

        startMapUpdateTask(player);
    }

    /**
     * Apre la GUI della classifica
     */
    public void openLeaderboardGUI(Player player) {
        if (!config.isLeaderboardEnabled()) {
            player.sendMessage(config.getColoredMessage("leaderboard-disabled"));
            return;
        }

        int rows = config.getLeaderboardRows();
        String title = colorize(config.getLeaderboardTitle());
        Inventory gui = Bukkit.createInventory(new LeaderboardHolder(), rows * 9, title);

        updateLeaderboard(gui, rows);

        player.openInventory(gui);

        startLeaderboardUpdateTask(player);
    }

    /**
     * Apre l'inventario dei power-up del giocatore
     */
    public void openPowerUpInventory(Player player) {
        int rows = 3;
        String title = colorize("&6I tuoi Power-Up");
        Inventory gui = Bukkit.createInventory(new PowerUpHolder(player), rows * 9, title);

        List<com.ideovision.powerups.PowerUp> powerUps =
            plugin.getPowerUpManager().getPlayerInventory(player);

        int slot = 0;
        for (com.ideovision.powerups.PowerUp powerUp : powerUps) {
            gui.setItem(slot++, powerUp.toItemStack());
        }

        player.openInventory(gui);
    }

    private void fillBackground(Inventory gui, int rows) {
        ItemStack glass = createGlassItem();
        for (int i = 0; i < rows * 9; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, glass);
            }
        }
    }

    private ItemStack createGlassItem() {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);
        }
        return glass;
    }

    private ItemStack createMapItem() {
        ItemStack map = new ItemStack(Material.FILLED_MAP);
        ItemMeta meta = map.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(colorize("&6&lMappa del Circuito"));
            List<String> lore = new ArrayList<>();
            lore.add(colorize("&7Visualizza la posizione dei giocatori"));
            lore.add(colorize(""));
            lore.add(colorize("&eIcone:"));
            lore.add(colorize("  &a• Tu"));
            lore.add(colorize("  &c• Avversari"));
            map.setItemMeta(meta);
        }

        return map;
    }

    private void updatePlayerIndicators(Inventory gui, int rows) {
        List<Player> racers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (LapsManager.isRacing(p)) {
                racers.add(p);
            }
        }

        int baseSlot = (rows - 1) * 9 + 1;
        int slot = baseSlot;

        for (Player racer : racers) {
            if (slot >= rows * 9) break;

            ItemStack head = createPlayerHead(racer);
            gui.setItem(slot++, head);
        }
    }

    private ItemStack createPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(colorize("&f" + player.getName()));

            int lap = LapsManager.getCurrentLap(player);
            int totalLaps = LapsManager.getActiveRaceTotalLaps();

            List<String> lore = new ArrayList<>();
            lore.add(colorize("&7Giro: &e" + lap + "/" + totalLaps));

            if (LapsManager.hasFinished(player)) {
                lore.add(colorize("&a&lFINITO"));
            }

            meta.setLore(lore);
            meta.setOwningPlayer(player);
            head.setItemMeta(meta);
        }

        return head;
    }

    private void updateLeaderboard(Inventory gui, int rows) {
        List<Player> racers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (LapsManager.isRacing(p)) {
                racers.add(p);
            }
        }

        racers.sort((a, b) -> {
            int lapA = LapsManager.getCurrentLap(a);
            int lapB = LapsManager.getCurrentLap(b);
            return Integer.compare(lapB, lapA);
        });

        int slot = 9;
        int position = 1;

        for (Player racer : racers) {
            if (slot >= rows * 9 - 9) break;

            ItemStack head = createLeaderboardHead(racer, position);
            gui.setItem(slot, head);
            slot += 2;
            position++;
        }

        ItemStack decoration = createDecorationItem(position);
        for (int i = rows * 9 - 9; i < rows * 9; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, decoration);
            }
        }
    }

    private ItemStack createLeaderboardHead(Player player, int position) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            String positionColor = getPositionColor(position);
            meta.setDisplayName(positionColor + "#" + position + " &f" + player.getName());

            int lap = LapsManager.getCurrentLap(player);
            int totalLaps = LapsManager.getActiveRaceTotalLaps();

            List<String> lore = new ArrayList<>();
            lore.add(colorize("&7Giro: &e" + lap + "/" + totalLaps));

            if (LapsManager.hasFinished(player)) {
                lore.add(colorize(""));
                lore.add(colorize("&a&lCLASSIFICATO"));
            }

            meta.setLore(lore);
            meta.setOwningPlayer(player);
            head.setItemMeta(meta);
        }

        return head;
    }

    private String getPositionColor(int position) {
        switch (position) {
            case 1: return "&6";
            case 2: return "&f";
            case 3: return "&e";
            default: return "&7";
        }
    }

    private ItemStack createDecorationItem(int totalPlayers) {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(colorize("&7Informazioni Gara"));
            List<String> lore = new ArrayList<>();
            lore.add(colorize("&7Partecipanti: &e" + totalPlayers));
            lore.add(colorize("&7Stato: &aIn corso"));
            lore.add(colorize(""));
            lore.add(colorize("&7Gira per aggiornare"));
            meta.setLore(lore);
            book.setItemMeta(meta);
        }

        return book;
    }

    private void startMapUpdateTask(Player player) {
        final int[] taskId = new int[1];
        taskId[0] = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
            plugin,
            () -> {
                if (!player.isOnline()) {
                    stopMapUpdateTask(player);
                    return;
                }

                Inventory top = player.getOpenInventory().getTopInventory();
                if (top.getHolder() instanceof MapHolder) {
                    updatePlayerIndicators(top, config.getMapGuiRows());
                } else {
                    stopMapUpdateTask(player);
                }
            },
            config.getMapUpdateInterval(),
            config.getMapUpdateInterval()
        );

        guiTasks.put(player.getUniqueId(), taskId[0]);
    }

    private void startLeaderboardUpdateTask(Player player) {
        final int[] taskId = new int[1];
        taskId[0] = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
            plugin,
            () -> {
                if (!player.isOnline()) {
                    stopLeaderboardUpdateTask(player);
                    return;
                }

                Inventory top = player.getOpenInventory().getTopInventory();
                if (top.getHolder() instanceof LeaderboardHolder) {
                    updateLeaderboard(top, config.getLeaderboardRows());
                } else {
                    stopLeaderboardUpdateTask(player);
                }
            },
            config.getLeaderboardUpdateInterval(),
            config.getLeaderboardUpdateInterval()
        );

        guiTasks.put(player.getUniqueId(), taskId[0]);
    }

    private void stopMapUpdateTask(Player player) {
        Integer taskId = guiTasks.remove(player.getUniqueId());
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
    }

    private void stopLeaderboardUpdateTask(Player player) {
        Integer taskId = guiTasks.remove(player.getUniqueId());
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
    }

    public void closeAllGUIs() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof RaceGUIHolder) {
                player.closeInventory();
            }
        }
    }

    private String colorize(String msg) {
        return msg.replace("&", "§");
    }

    public interface RaceGUIHolder extends InventoryHolder {}

    public static class MapHolder implements RaceGUIHolder {
        private final Inventory inventory;

        public MapHolder() {
            this.inventory = Bukkit.createInventory(this, 54, "Map");
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    public static class LeaderboardHolder implements RaceGUIHolder {
        private final Inventory inventory;

        public LeaderboardHolder() {
            this.inventory = Bukkit.createInventory(this, 54, "Leaderboard");
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    public static class PowerUpHolder implements RaceGUIHolder {
        private final Inventory inventory;
        private final Player owner;

        public PowerUpHolder(Player owner) {
            this.owner = owner;
            this.inventory = Bukkit.createInventory(this, 27, "Power-Ups");
        }

        public Player getOwner() {
            return owner;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
