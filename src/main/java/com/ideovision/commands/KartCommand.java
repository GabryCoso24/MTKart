package com.ideovision.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ideovision.MTKart;
import com.ideovision.config.PluginConfig;
import com.ideovision.karts.KartType;

public class KartCommand implements CommandExecutor {
    private final MTKart plugin;
    private final PluginConfig config;

    public KartCommand(MTKart plugin) {
        this.plugin = plugin;
        this.config = PluginConfig.getInstance(plugin);
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

        String subcommand = args[0].toLowerCase();

        if (subcommand.equals("equip")) {
            if (args.length < 2) {
                player.sendMessage("§c/kart equip <tipo>");
                player.sendMessage("§7Tipi: default, speed, balance, acceleration");
                return true;
            }

            KartType kartType = parseKartType(args[1]);
            plugin.getKartManager().equipKart(player, kartType);
            player.sendMessage("§aHai equipaggiato il " + kartType.getColoredDisplayName());
            return true;
        }

        if (subcommand.equals("remove")) {
            plugin.getKartManager().removeKart(player);
            player.sendMessage("§aKart rimosso");
            return true;
        }

        if (subcommand.equals("list")) {
            player.sendMessage("§6§lTipi di Kart disponibili:");
            for (KartType type : KartType.values()) {
                player.sendMessage("  " + type.getColoredDisplayName() +
                    " §7- Model ID: " + config.getKartModelId(type.getConfigName()));
            }
            return true;
        }

        sendHelp(player);
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§lMTKart - Comandi Kart");
        player.sendMessage("§e/kart equip <tipo> §7- Equipaggia un kart");
        player.sendMessage("§e/kart remove §7- Rimuovi il kart");
        player.sendMessage("§e/kart list §7- Lista kart disponibili");
    }

    private KartType parseKartType(String type) {
        switch (type.toLowerCase()) {
            case "speed":
            case "speed_kart":
                return KartType.SPEED_KART;
            case "balance":
            case "balance_kart":
                return KartType.BALANCE_KART;
            case "acceleration":
            case "acceleration_kart":
                return KartType.ACCELERATION_KART;
            default:
                return KartType.DEFAULT;
        }
    }
}
