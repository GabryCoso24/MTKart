package com.ideovision.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ideovision.MTKart;
import com.ideovision.managers.CircuitManager;
import com.ideovision.managers.StartRaceManager;
import com.ideovision.managers.StopRaceManager;

public class RaceCommand implements CommandExecutor {
    private static final Map<String, RaceSetup> raceSetup = new HashMap<>();
    private final MTKart plugin;

    public RaceCommand(MTKart plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§b/race create <nome>");
            sender.sendMessage("§b/race edit <nome>");
            sender.sendMessage("§b/race set inizio|fine|laps|ost|grid|powerup");
            sender.sendMessage("§b/race save [nome]");
            sender.sendMessage("§b/race delete <nome>");
            sender.sendMessage("§b/race start <nome>");
            sender.sendMessage("§b/race stop <nome>");
            return true;
        }

        String subcommand = args[0].toLowerCase();

        // CREATE
        if (subcommand.equals("create")) {
            if (args.length < 2) {
                sender.sendMessage("§c/race create <nome>");
                return true;
            }
            String raceName = args[1];
            raceSetup.put(player.getName(), new RaceSetup(raceName, player.getLocation(), null, 0));
            sender.sendMessage("§aCircuit " + raceName + " creato. Usa /race set");
            return true;
        }

        // EDIT
        if (subcommand.equals("edit")) {
            if (args.length < 2) {
                sender.sendMessage("§c/race edit <nome>");
                return true;
            }

            String raceName = args[1];
            if (CircuitManager.loadCircuit(raceName) == null) {
                sender.sendMessage("§cCircuit non trovato");
                return true;
            }

            RaceSetup setup = new RaceSetup(
                raceName,
                CircuitManager.getStartLocation(raceName),
                CircuitManager.getEndLocation(raceName),
                CircuitManager.getLapsFromCircuit(raceName),
                CircuitManager.getOstFromCircuit(raceName)
            );
            raceSetup.put(player.getName(), setup);

            sender.sendMessage("§aModifica caricata per: §e" + raceName);
            sender.sendMessage("§7Usa /race set ... e poi /race save " + raceName);
            return true;
        }

        // SET
        if (subcommand.equals("set")) {
            if (args.length < 2) {
                sender.sendMessage("§c/race set inizio|fine|laps|ost|grid|powerup <valore>");
                return true;
            }

            String option = args[1].toLowerCase();

            // SET GRID - posizione di partenza (1-12)
            if (option.equals("grid")) {
                if (args.length < 3) {
                    sender.sendMessage("§c/race set grid <1-12>");
                    return true;
                }
                int slot;
                try {
                    slot = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInserisci un numero da 1 a 12");
                    return true;
                }
                if (slot < 1 || slot > 12) {
                    sender.sendMessage("§cLa posizione deve essere tra 1 e 12");
                    return true;
                }
                String circuitName = resolveCircuitName(player, args);
                if (circuitName == null) {
                    sender.sendMessage("§cPrima usa /race create <nome> per impostare un circuito");
                    return true;
                }
                plugin.getStartingGridManager().setGridPosition(circuitName, slot, player.getLocation());
                sender.sendMessage("§aPosizione griglia §e#" + slot + " §asalvata!");
                return true;
            }

            // SET POWERUP - aggiunge uno spawner power-up alla posizione corrente
            if (option.equals("powerup")) {
                String circuitName = resolveCircuitName(player, args);
                if (circuitName == null) {
                    sender.sendMessage("§cPrima usa /race create <nome> per impostare un circuito");
                    return true;
                }
                plugin.getPowerUpSpawnerManager().addSpawnerLocation(circuitName, player.getLocation());
                sender.sendMessage("§aSpawner power-up aggiunto alla posizione corrente!");
                return true;
            }

            // SET POWERUP-REMOVE - rimuove lo spawner più vicino
            if (option.equals("powerup-remove")) {
                String circuitName = resolveCircuitName(player, args);
                if (circuitName == null) {
                    sender.sendMessage("§cSpecifica il nome del circuito: /race set powerup-remove <nome>");
                    return true;
                }
                boolean removed = plugin.getPowerUpSpawnerManager().removeNearestSpawner(circuitName, player.getLocation());
                if (removed) {
                    sender.sendMessage("§aSpawner power-up rimosso!");
                } else {
                    sender.sendMessage("§cNessuno spawner trovato entro 2 blocchi");
                }
                return true;
            }

            if (!raceSetup.containsKey(player.getName())) {
                sender.sendMessage("§cFa prima /race create <nome>");
                return true;
            }

            RaceSetup setup = raceSetup.get(player.getName());

            if (option.equals("inizio")) {
                setup.start = player.getLocation();
                sender.sendMessage("§aInizio marcato");
                return true;
            }

            if (option.equals("fine")) {
                setup.end = player.getLocation();
                sender.sendMessage("§aFine marcata");
                return true;
            }

            if (option.equals("laps")) {
                if (args.length < 3) {
                    sender.sendMessage("§c/race set laps <numero>");
                    return true;
                }
                try {
                    setup.laps = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cNumero giri non valido");
                    return true;
                }
                if (setup.laps <= 0) {
                    sender.sendMessage("§cI giri devono essere maggiori di 0");
                    return true;
                }
                sender.sendMessage("§aGiri: " + setup.laps);
                return true;
            }

            if (option.equals("ost")) {
                if (args.length < 3) {
                    sender.sendMessage("§c/race set ost <nomeost>");
                    return true;
                }
                setup.ost = args[2];
                sender.sendMessage("§aOST impostata: " + setup.ost);
                return true;
            }

            return false;
        }

        // SAVE
        if (subcommand.equals("save")) {
            RaceSetup setup = raceSetup.get(player.getName());
            if (setup == null) {
                sender.sendMessage("§cNessuna modifica in corso. Usa /race create o /race edit");
                return true;
            }

            String raceName = setup.name;
            if (args.length >= 2 && !args[1].equalsIgnoreCase(setup.name)) {
                sender.sendMessage("§cStai modificando il circuito §e" + setup.name + "§c. Usa /race save " + setup.name);
                return true;
            }

            if (setup.start == null || setup.end == null || setup.laps <= 0) {
                sender.sendMessage("§cCompleta: /race set inizio|fine|laps");
                return true;
            }

            CircuitManager.saveCircuit(raceName, setup.start, setup.end, setup.laps, setup.ost);
            sender.sendMessage("§aCircuit §e" + raceName + " §asalvato!");
            return true;
        }

        // START
        if (subcommand.equals("start")) {
            if (args.length < 2) {
                sender.sendMessage("§c/race start <nome>");
                return true;
            }

            String raceName = args[1];

            // Se c'è un setup in memoria per questa race, persisti sempre i campi principali.
            // Questo evita di perdere start/end/laps/ost quando il file YAML è stato già creato
            // prima tramite /race set grid o /race set powerup.
            RaceSetup setup = raceSetup.get(player.getName());
            if (setup != null && setup.name.equals(raceName)) {
                if (setup.start == null || setup.end == null || setup.laps == 0) {
                    sender.sendMessage("§cCompleta: /race set inizio|fine|laps");
                    return true;
                }
                CircuitManager.saveCircuit(raceName, setup.start, setup.end, setup.laps, setup.ost);
                raceSetup.remove(player.getName());
            } else if (CircuitManager.loadCircuit(raceName) == null) {
                sender.sendMessage("§cCircuit non trovato");
                return true;
            }

            // Inizializza race manager
            plugin.getRaceManager().startRace(raceName);

            // Carica griglia di partenza e teletrasporta i giocatori
            plugin.getStartingGridManager().loadGrid(raceName);
            plugin.getStartingGridManager().teleportPlayersToGrid();

            // Usa l'ordine assegnato in griglia come ordine iniziale della tablist
            plugin.getRaceManager().setLeaderboardOrder(plugin.getStartingGridManager().getLastAssignedOrder());

            // Congela i giocatori fino al via
            plugin.getStartingGridManager().freezeAll();

            // Equipaggia kart ai giocatori
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                plugin.getKartManager().equipKart(p, com.ideovision.karts.KartType.DEFAULT);
            }

            // Carica e avvia gli spawner power-up (le caselle sono visibili, la raccolta parte al VIA)
            plugin.getPowerUpSpawnerManager().loadSpawners(raceName);
            plugin.getPowerUpSpawnerManager().startSpawners();

            // Avvia countdown
            new StartRaceManager().startRace(raceName);

            sender.sendMessage("§a✓ Gara " + raceName + " iniziata!");
            return true;
        }

        // DELETE
        if (subcommand.equals("delete")) {
            if (args.length < 2) {
                sender.sendMessage("§c/race delete <nome>");
                return true;
            }

            String raceName = args[1];
            boolean deleted = CircuitManager.deleteCircuit(raceName);
            if (!deleted) {
                sender.sendMessage("§cCircuit non trovato");
                return true;
            }

            RaceSetup setup = raceSetup.get(player.getName());
            if (setup != null && setup.name.equalsIgnoreCase(raceName)) {
                raceSetup.remove(player.getName());
            }

            sender.sendMessage("§aCircuit §e" + raceName + " §aeliminato!");
            return true;
        }

        // STOP
        if (subcommand.equals("stop")) {
            if (args.length < 2) {
                sender.sendMessage("§c/race stop <nome>");
                return true;
            }

            String raceName = args[1];
            if (CircuitManager.loadCircuit(raceName) == null) {
                sender.sendMessage("§cCircuit non trovato");
                return true;
            }

            StopRaceManager.stopRace(raceName);

            // Ferma race manager
            plugin.getRaceManager().stopRace();

            // Rimuovi kart
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                plugin.getKartManager().removeKart(p);
            }

            // Nascondi scoreboard
            plugin.getScoreboardManager().hideAllScoreboards();

            sender.sendMessage("§a✓ Gara " + raceName + " fermata!");
            return true;
        }

        return false;
    }

    private String resolveCircuitName(Player player, String[] args) {
        if (raceSetup.containsKey(player.getName())) {
            return raceSetup.get(player.getName()).name;
        }

        if (args.length >= 3 && CircuitManager.loadCircuit(args[2]) != null) {
            return args[2];
        }

        return null;
    }

    static class RaceSetup {
        String name;
        org.bukkit.Location start;
        org.bukkit.Location end;
        int laps;
        String ost;

        RaceSetup(String name, org.bukkit.Location start, org.bukkit.Location end, int laps) {
            this(name, start, end, laps, null);
        }

        RaceSetup(String name, org.bukkit.Location start, org.bukkit.Location end, int laps, String ost) {
            this.name = name;
            this.start = start;
            this.end = end;
            this.laps = laps;
            this.ost = ost;
        }
    }
}