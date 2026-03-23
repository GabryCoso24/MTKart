package com.ideovision.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ideovision.managers.CircuitManager;
import com.ideovision.managers.StartRaceManager;
import com.ideovision.managers.StopRaceManager;

public class RaceCommand implements CommandExecutor {
    private static final Map<String, RaceSetup> raceSetup = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Solo players");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sender.sendMessage("§b/race create <nome>");
            sender.sendMessage("§b/race set inizio|fine|laps");
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

        // SET
        if (subcommand.equals("set")) {
            if (args.length < 2) {
                sender.sendMessage("§c/race set inizio|fine|laps <valore>");
                return true;
            }

            if (!raceSetup.containsKey(player.getName())) {
                sender.sendMessage("§cFa prima /race create <nome>");
                return true;
            }

            RaceSetup setup = raceSetup.get(player.getName());
            String option = args[1].toLowerCase();

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
                setup.laps = Integer.parseInt(args[2]);
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

        // START
        if (subcommand.equals("start")) {
            if (args.length < 2) {
                sender.sendMessage("§c/race start <nome>");
                return true;
            }

            String raceName = args[1];
            RaceSetup setup = raceSetup.get(player.getName());

            // Se il file yml esiste già, carica da lì
            if (CircuitManager.loadCircuit(raceName) != null) {
                new StartRaceManager().startRace(raceName);
                sender.sendMessage("§a✓ Gara " + raceName + " iniziata!");
                return true;
            }

            if (setup == null || !setup.name.equals(raceName)) {
                sender.sendMessage("§cCircuit non trovato");
                return true;
            }

            if (setup.start == null || setup.end == null || setup.laps == 0) {
                sender.sendMessage("§cCompleta: /race set inizio|fine|laps");
                return true;
            }

            // Salva e avvia
            CircuitManager.saveCircuit(raceName, setup.start, setup.end, setup.laps, setup.ost);
            new StartRaceManager().startRace(raceName);
            
            sender.sendMessage("§a✓ Gara " + raceName + " iniziata!");
            sender.sendMessage("§6Debug: ost salvata = " + (setup.ost == null ? "NULL" : setup.ost));
            raceSetup.remove(player.getName());
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
            sender.sendMessage("§a✓ Gara " + raceName + " fermata!");
            return true;
        }

        return false;
    }

    static class RaceSetup {
        String name;
        org.bukkit.Location start;
        org.bukkit.Location end;
        int laps;
        String ost;

        RaceSetup(String name, org.bukkit.Location start, org.bukkit.Location end, int laps) {
            this.name = name;
            this.start = start;
            this.end = end;
            this.laps = laps;
            this.ost = null;
        }
    }
}