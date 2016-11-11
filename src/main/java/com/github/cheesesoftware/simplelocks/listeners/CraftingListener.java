package com.github.cheesesoftware.simplelocks.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import com.github.cheesesoftware.simplelocks.CraftingManager;

public class CraftingListener implements Listener {

    private CraftingManager craftingManager;

    public CraftingListener(CraftingManager craftingManager) {
        this.craftingManager = craftingManager;
    }

    // Method summary: If there is incomplete key in anvil, generate key in output slot
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getInventory().getType() == InventoryType.ANVIL && e.getWhoClicked() instanceof Player) {
            AnvilInventory anvilInv = (AnvilInventory) e.getInventory();
            Player player = (Player) e.getWhoClicked();
            craftingManager.checkAnvilInventory(anvilInv, player, 0);
        }
    }

    // Method summary: If there is incomplete key in anvil, generate key in output slot
    // If player took output key, remove the incomplete key
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getClick() == ClickType.DOUBLE_CLICK)
            e.setCancelled(true);
        if (e.getInventory().getType() == InventoryType.ANVIL && e.getWhoClicked() instanceof Player) {
            AnvilInventory anvilInv = (AnvilInventory) e.getInventory();
            int slot = e.getRawSlot();
            Player player = (Player) e.getWhoClicked();

            if (player.hasPermission("simplelocks.player.craft.key") && slot == 2 && anvilInv.getItem(2) != null && anvilInv.getItem(2).getType() == Material.TRIPWIRE_HOOK
                    && anvilInv.getItem(2).getItemMeta() != null && (anvilInv.getItem(2).getItemMeta().getDisplayName().equals("Key")) && anvilInv.getItem(0) != null
                    && anvilInv.getItem(0).getType() == Material.TRIPWIRE_HOOK && anvilInv.getItem(0).getItemMeta() != null
                    && anvilInv.getItem(0).getItemMeta().getDisplayName().equals("Incomplete Key") && (anvilInv.getItem(1) == null || anvilInv.getItem(1).getType() == Material.AIR)
                    && (e.getWhoClicked().getItemOnCursor() == null || e.getWhoClicked().getItemOnCursor().getType() == Material.AIR)) {
                // Player took out a Key, input was Incomplete Key

                e.getWhoClicked().setItemOnCursor(anvilInv.getItem(2).clone());
                craftingManager.incrementHighestKeyId();

                if (anvilInv.getItem(0) != null && anvilInv.getItem(0).getType() != Material.AIR && anvilInv.getItem(0).getType() == Material.TRIPWIRE_HOOK) {
                    if (anvilInv.getItem(0).getAmount() > 1)
                        anvilInv.getItem(0).setAmount(anvilInv.getItem(0).getAmount() - 1);
                    else
                        anvilInv.setItem(0, new ItemStack(Material.AIR, 0));
                }

                anvilInv.setItem(2, new ItemStack(Material.AIR, 0));
            } else if (player.hasPermission("simplelocks.player.craft.copykey") && slot == 2 && anvilInv.getItem(2) != null && anvilInv.getItem(2).getType() == Material.TRIPWIRE_HOOK
                    && anvilInv.getItem(2).getItemMeta() != null && (anvilInv.getItem(2).getItemMeta().getDisplayName().equals("Key")) && anvilInv.getItem(0) != null
                    && anvilInv.getItem(0).getType() == Material.TRIPWIRE_HOOK && anvilInv.getItem(0).getItemMeta() != null && anvilInv.getItem(0).getItemMeta().getDisplayName().equals("Key")
                    && anvilInv.getItem(1) != null && anvilInv.getItem(1).getType() == Material.TRIPWIRE_HOOK && anvilInv.getItem(1).getItemMeta() != null
                    && anvilInv.getItem(1).getItemMeta().getDisplayName().equals("Incomplete Key")) {
                // Player took out a Key, input was Key and Incomplete Key
                ItemStack cursor = e.getWhoClicked().getItemOnCursor();
                if (cursor == null || cursor.getType() == Material.AIR)
                    e.getWhoClicked().setItemOnCursor(anvilInv.getItem(2).clone());
                else if (cursor.isSimilar(anvilInv.getItem(2)) && cursor.getAmount() < 64) {
                    cursor.setAmount(cursor.getAmount() + 1);
                    e.getWhoClicked().setItemOnCursor(cursor);
                } else {
                    // Player has item in hand, abort!
                    return;
                }

                if (anvilInv.getItem(1) != null && anvilInv.getItem(1).getType() != Material.AIR
                        && (anvilInv.getItem(1).getType() == Material.TRIPWIRE_HOOK || anvilInv.getItem(1).getType() == Material.STONE_BUTTON)) {
                    if (anvilInv.getItem(1).getAmount() > 1)
                        anvilInv.getItem(1).setAmount(anvilInv.getItem(1).getAmount() - 1);
                    else
                        anvilInv.setItem(1, new ItemStack(Material.AIR, 0));
                }

                anvilInv.setItem(2, new ItemStack(Material.AIR, 0));
            } else if (player.hasPermission("simplelocks.player.craft.lock") && slot == 2 && anvilInv.getItem(2) != null && anvilInv.getItem(2).getType() == Material.STONE_BUTTON
                    && anvilInv.getItem(2).getItemMeta() != null && (anvilInv.getItem(2).getItemMeta().getDisplayName().equals("Lock")) && anvilInv.getItem(0) != null
                    && anvilInv.getItem(0).getType() == Material.TRIPWIRE_HOOK && anvilInv.getItem(0).getItemMeta() != null && anvilInv.getItem(0).getItemMeta().getDisplayName().equals("Key")
                    && anvilInv.getItem(1) != null && anvilInv.getItem(1).getType() == Material.STONE_BUTTON && anvilInv.getItem(1).getItemMeta() != null
                    && anvilInv.getItem(1).getItemMeta().getDisplayName().equals("Incomplete Lock")) {
                // Player took out a Lock, input was Key and Incomplete Lock
                ItemStack cursor = e.getWhoClicked().getItemOnCursor();
                if (cursor == null || cursor.getType() == Material.AIR)
                    e.getWhoClicked().setItemOnCursor(anvilInv.getItem(2).clone());
                else if (cursor.isSimilar(anvilInv.getItem(2)) && cursor.getAmount() < 64) {
                    cursor.setAmount(cursor.getAmount() + 1);
                    e.getWhoClicked().setItemOnCursor(cursor);
                } else {
                    // Player has item in hand, abort!
                    return;
                }

                if (anvilInv.getItem(1) != null && anvilInv.getItem(1).getType() != Material.AIR
                        && (anvilInv.getItem(1).getType() == Material.TRIPWIRE_HOOK || anvilInv.getItem(1).getType() == Material.STONE_BUTTON)) {
                    if (anvilInv.getItem(1).getAmount() > 1)
                        anvilInv.getItem(1).setAmount(anvilInv.getItem(1).getAmount() - 1);
                    else
                        anvilInv.setItem(1, new ItemStack(Material.AIR, 0));
                }

                anvilInv.setItem(2, new ItemStack(Material.AIR, 0));
            }
            craftingManager.checkAnvilInventory(anvilInv, player, 2);
        }
    }
}
