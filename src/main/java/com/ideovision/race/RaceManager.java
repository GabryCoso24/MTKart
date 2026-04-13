package com.ideovision.race;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.ideovision.MTKart;
import com.ideovision.config.PluginConfig;
import com.ideovision.managers.LapsManager;

public class RaceManager {
    private final MTKart plugin;
    private final PluginConfig config;

    // Dati di gara per giocatore
    private final Map<UUID, RaceData> raceData = new ConcurrentHashMap<>();

    // Classifica attuale
    private final List<UUID> leaderboard = new ArrayList<>();

    // Tempi migliori per giro
    private final Map<UUID, Long> bestLapTimes = new HashMap<>();

    // Tempo di inizio gara
    private long raceStartTime = 0;

    public RaceManager(MTKart plugin) {
        this.plugin = plugin;
        this.config = PluginConfig.getInstance(plugin);
    }

    /**
     * Inizia a tracciare i dati di un giocatore
     */
    public void startTracking(Player player) {
        raceData.put(player.getUniqueId(), new RaceData(
            System.currentTimeMillis(),
            0,
            0,
            System.currentTimeMillis()
        ));
    }

    public void setLeaderboardOrder(List<UUID> order) {
        leaderboard.clear();

        for (UUID uuid : order) {
            if (!leaderboard.contains(uuid)) {
                leaderboard.add(uuid);
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (LapsManager.isRacing(player) && !leaderboard.contains(uuid)) {
                leaderboard.add(uuid);
            }
        }
    }

    /**
     * Ferma il tracciamento di un giocatore
     */
    public void stopTracking(Player player) {
        raceData.remove(player.getUniqueId());
        bestLapTimes.remove(player.getUniqueId());
    }

    /**
     * Registra il passaggio di un giro
     */
    public void recordLap(Player player) {
        RaceData data = raceData.get(player.getUniqueId());
        if (data == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long lapTime = currentTime - data.lastLapTime;

        // Aggiorna ultimo tempo giro
        data.lastLapTime = currentTime;
        data.totalLaps++;

        // Controlla se è il miglior tempo
        Long bestTime = bestLapTimes.get(player.getUniqueId());
        if (bestTime == null || lapTime < bestTime) {
            bestLapTimes.put(player.getUniqueId(), lapTime);
            player.sendMessage(config.getColoredMessage("new-best-lap", Map.of(
                "time", formatTime(lapTime)
            )));
        }

        // Annuncia tempo giro
        player.sendMessage(config.getColoredMessage("lap-time", Map.of(
            "time", formatTime(lapTime),
            "best", bestTime != null ? formatTime(bestTime) : "--:--"
        )));

        // Aggiorna classifica
        updateLeaderboard();
    }

    /**
     * Aggiorna la classifica
     */
    public void updateLeaderboard() {
        List<Player> racers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (LapsManager.isRacing(p)) {
                racers.add(p);
            }
        }

        // Ordina per giri completati, poi per tempo ultimo giro
        racers.sort((a, b) -> {
            int lapA = LapsManager.getCurrentLap(a);
            int lapB = LapsManager.getCurrentLap(b);

            if (lapA != lapB) {
                return Integer.compare(lapB, lapA);
            }

            // Stesso giro, confronta tempo ultimo giro
            RaceData dataA = raceData.get(a.getUniqueId());
            RaceData dataB = raceData.get(b.getUniqueId());

            if (dataA != null && dataB != null) {
                return Long.compare(dataA.lastLapTime, dataB.lastLapTime);
            }

            return 0;
        });

        leaderboard.clear();
        for (Player p : racers) {
            leaderboard.add(p.getUniqueId());
        }
    }

    /**
     * Ottiene la posizione attuale di un giocatore
     */
    public int getPosition(Player player) {
        int pos = leaderboard.indexOf(player.getUniqueId()) + 1;
        return pos > 0 ? pos : getTotalRacers();
    }

    /**
     * Ottiene il numero totale di partecipanti
     */
    public int getTotalRacers() {
        int count = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (LapsManager.isRacing(p) || raceData.containsKey(p.getUniqueId())) {
                count++;
            }
        }
        return Math.max(count, 1);
    }

    /**
     * Ottiene il tempo migliore di un giocatore
     */
    public Long getBestLapTime(Player player) {
        return bestLapTimes.get(player.getUniqueId());
    }

    /**
     * Formatta un tempo in millisecondi in formato leggibile
     */
    public String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        millis = millis % 1000;

        return String.format("%d:%02d.%03d", minutes, seconds, millis);
    }

    /**
     * Ottiene i dati di gara di un giocatore
     */
    public RaceData getRaceData(Player player) {
        return raceData.get(player.getUniqueId());
    }

    /**
     * Ottiene la classifica completa
     */
    public List<UUID> getLeaderboard() {
        return new ArrayList<>(leaderboard);
    }

    /**
     * Ottiene il giocatore in una specifica posizione
     */
    public Player getPlayerAtPosition(int position) {
        if (position < 1 || position > leaderboard.size()) {
            return null;
        }
        UUID uuid = leaderboard.get(position - 1);
        return Bukkit.getPlayer(uuid);
    }

    /**
     * Inizia una nuova gara
     */
    public void startRace(String raceName) {
        raceStartTime = System.currentTimeMillis();
        raceData.clear();
        bestLapTimes.clear();
        leaderboard.clear();

        // Inizia a tracciare tutti i giocatori online
        for (Player p : Bukkit.getOnlinePlayers()) {
            startTracking(p);
        }
    }

    /**
     * Ferma la gara corrente
     */
    public void stopRace() {
        raceData.clear();
        bestLapTimes.clear();
        leaderboard.clear();
        raceStartTime = 0;
    }

    /**
     * Ottiene il tempo totale di gara
     */
    public long getRaceTime() {
        if (raceStartTime == 0) {
            return 0;
        }
        return System.currentTimeMillis() - raceStartTime;
    }

    /**
     * Ottiene il tempo totale di gara formattato
     */
    public String getFormattedRaceTime() {
        return formatTime(getRaceTime());
    }

    /**
     * Calcola il distacco tra due giocatori
     */
    public String getGap(Player player, Player other) {
        RaceData dataPlayer = raceData.get(player.getUniqueId());
        RaceData dataOther = raceData.get(other.getUniqueId());

        if (dataPlayer == null || dataOther == null) {
            return "";
        }

        long gap = Math.abs(dataPlayer.lastLapTime - dataOther.lastLapTime);
        return "+" + formatTime(gap);
    }

    /**
     * Classe per memorizzare i dati di gara di un giocatore
     */
    public static class RaceData {
        public final long joinTime;
        public int totalLaps;
        public int finishedPosition;
        public long lastLapTime;
        public long finishTime;

        public RaceData(long joinTime, int totalLaps, int finishedPosition, long lastLapTime) {
            this.joinTime = joinTime;
            this.totalLaps = totalLaps;
            this.finishedPosition = finishedPosition;
            this.lastLapTime = lastLapTime;
        }
    }
}
