package com.ideovision.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ideovision.MTKart;
import com.ideovision.powerups.PowerUp;
import com.ideovision.powerups.PowerUpType;

public class PowerUpCommand implements CommandExecutor, TabCompleter {
    private final MTKart plugin;

    public PowerUpCommand(MTKart plugin) {
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
            // Dai un power-up casuale
            plugin.getPowerUpManager().giveRandomPowerUp(player);
            return true;
        }

        // Admin: dai un power-up specifico
        if (sender.hasPermission("mtkart.admin")) {
            String powerUpName = args[0].toUpperCase();
            try {
                PowerUpType type = PowerUpType.valueOf(powerUpName);
                PowerUp powerUp = new PowerUp(type, plugin);
                player.getInventory().addItem(powerUp.toItemStack());
                player.sendMessage("§aHai ricevuto: " + powerUp.getType().getName());
                return true;
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cPower-up non trovato! Tipi: " + Arrays.toString(PowerUpType.values()));
                return true;
            }
        }

        player.sendMessage("§cComando non valido. Usa /powerup per ottenere un power-up casuale.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("mtkart.admin")) {
            return Arrays.asList(
                "GREEN_SHELL",
                "RED_SHELL",
                "BANANA",
                "MUSHROOM",
                "STAR",
                "LIGHTNING",
                "BLOOPER",
                "BOB_OMB"
            );
        }
        return null;
    }
}
