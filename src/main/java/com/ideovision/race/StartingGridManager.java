package com.ideovision.race;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.ideovision.managers.CircuitManager;

/**
 * Gestisce la griglia di partenza con 12 posizioni individuali.
 * Come in Mario Kart, i giocatori vengono teletrasportati nelle loro
 * posizioni di partenza e congelati fino al via.
 */
public class StartingGridManager {
    private final JavaPlugin plugin;

    private final Map<UUID, Location> frozenPlayers = new HashMap<>();
    private final Map<Integer, Location> gridPositions = new HashMap<>();
    private final List<UUID> lastAssignedOrder = new ArrayList<>();

    public static final int MAX_GRID_SLOTS = 12;

    public StartingGridManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Carica le posizioni della griglia dal file del circuito
     */
    public void loadGrid(String raceName) {
        gridPositions.clear();
        lastAssignedOrder.clear();
        unfreezeAll();

        for (int i = 1; i <= MAX_GRID_SLOTS; i++) {
            Location loc = CircuitManager.getGridPosition(raceName, i);
            if (loc != null) {
                gridPositions.put(i, loc);
            }
        }
    }

    /**
     * Teletrasporta tutti i giocatori online nelle posizioni della griglia
     */
    public void teleportPlayersToGrid() {
        List<Player> players = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            players.add(p);
        }

        Collections.shuffle(players);
        lastAssignedOrder.clear();

        List<Integer> slots = new ArrayList<>(gridPositions.keySet());
        Collections.sort(slots);

        for (int i = 0; i < players.size() && i < slots.size() && i < MAX_GRID_SLOTS; i++) {
            Player player = players.get(i);
            int slot = slots.get(i);
            Location gridLoc = gridPositions.get(slot);

            if (gridLoc != null) {
                player.teleport(gridLoc);
                lastAssignedOrder.add(player.getUniqueId());
            }
        }
    }

    /**
     * Congela un giocatore (velocità a 0)
     */
    public void freezePlayer(Player player) {
        frozenPlayers.put(player.getUniqueId(), player.getLocation().clone());
        player.setWalkSpeed(0);
        player.setFlySpeed(0);
    }

    /**
     * Congela tutti i giocatori online
     */
    public void freezeAll() {
        if (!lastAssignedOrder.isEmpty()) {
            for (UUID uuid : lastAssignedOrder) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    freezePlayer(player);
                }
            }
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            freezePlayer(player);
        }
    }

    /**
     * Scongela un giocatore
     */
    public void unfreezePlayer(Player player) {
        frozenPlayers.remove(player.getUniqueId());
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
    }

    /**
     * Scongela tutti i giocatori congelati
     */
    public void unfreezeAll() {
        for (UUID uuid : new ArrayList<>(frozenPlayers.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                unfreezePlayer(player);
            }
        }
        frozenPlayers.clear();
    }

    /**
     * Controlla se un giocatore è congelato
     */
    public boolean isFrozen(Player player) {
        return frozenPlayers.containsKey(player.getUniqueId());
    }

    /**
     * Ottiene la posizione in cui il giocatore è congelato
     */
    public Location getFrozenLocation(Player player) {
        return frozenPlayers.get(player.getUniqueId());
    }

    /**
     * Salva una posizione della griglia nel circuito
     */
    public void setGridPosition(String raceName, int slot, Location location) {
        if (slot < 1 || slot > MAX_GRID_SLOTS) return;
        CircuitManager.saveGridPosition(raceName, slot, location);
        gridPositions.put(slot, location.clone());
    }

    /**
     * Ottiene una posizione della griglia
     */
    public Location getGridPosition(int slot) {
        return gridPositions.get(slot);
    }

    /**
     * Controlla se ci sono posizioni griglia configurate
     */
    public boolean hasGridPositions() {
        return !gridPositions.isEmpty();
    }

    /**
     * Ottiene tutte le posizioni griglia
     */
    public Map<Integer, Location> getGridPositions() {
        return new HashMap<>(gridPositions);
    }

    public List<UUID> getLastAssignedOrder() {
        return new ArrayList<>(lastAssignedOrder);
    }
}