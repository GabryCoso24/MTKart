package com.ideovision.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ideovision.MTKart;

public class GUICommand implements CommandExecutor {
    private final MTKart plugin;

    public GUICommand(MTKart plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSolo i giocatori possono usare questo comando!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String guiType = args[0].toLowerCase();

        if (guiType.equals("map") || guiType.equals("mappa")) {
            plugin.getRaceGUI().openMapGUI(player);
            return true;
        }

        if (guiType.equals("leaderboard") || guiType.equals("classifica")) {
            plugin.getRaceGUI().openLeaderboardGUI(player);
            return true;
        }

        if (guiType.equals("powerups") || guiType.equals("powerup") || guiType.equals("oggetti")) {
            plugin.getRaceGUI().openPowerUpInventory(player);
            return true;
        }

        sendHelp(player);
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§lMTKart - GUI Commands");
        player.sendMessage("§e/mtkgui mappa §7- Apri mappa del circuito");
        player.sendMessage("§e/mtkgui classifica §7- Apri classifica");
        player.sendMessage("§e/mtkgui powerups §7- Apri inventario oggetti");
    }
}
