package com.ideovision.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class RaceTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "set", "start");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return Arrays.asList("inizio", "fine", "laps", "ost");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("ost")) {
            return Arrays.asList("idle", "coconut_mall");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("start")){
            return Arrays.asList("coconut_mall");
        }
        return null;
    }
}
