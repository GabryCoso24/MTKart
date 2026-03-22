package com.ideovision.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ideovision.managers.StartRaceManager;

public class StartRaceCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players");
            return true;
        }

        StartRaceManager startRaceManager = new StartRaceManager();
        startRaceManager.StartTimer();
        
        return true;
    }
}