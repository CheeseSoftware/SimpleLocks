package com.github.cheesesoftware.simplelocks.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.cheesesoftware.simplelocks.CraftingManager;

public class CommandSimpleLocks implements CommandExecutor {

    CraftingManager craftingManager;

    public CommandSimpleLocks(CraftingManager craftingManager) {
        this.craftingManager = craftingManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("adminkey")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.getInventory().addItem(craftingManager.generateAdminKey());
                Bukkit.getLogger().info("[SimpleLocks] an admin key was given to " + player.getName());
                sender.sendMessage("You have been given an admin key.");
            } else
                sender.sendMessage("I can't give the console an admin key.");
        }

        sender.sendMessage("/simplelocks adminkey");
        return true;
    }

}
