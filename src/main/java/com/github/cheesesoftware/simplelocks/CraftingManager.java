package com.github.cheesesoftware.simplelocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class CraftingManager {

    private SimpleLocks plugin;
    private int highestId = 0;

    public CraftingManager(SimpleLocks plugin) {
        this.plugin = plugin;

        this.highestId = plugin.getConfig().getInt("highestkeyid");

        ItemStack lock = getLockItem();
        ShapedRecipe lockRecipe = new ShapedRecipe(lock);
        lockRecipe.shape(" B ", "BCB", " B ");
        lockRecipe.setIngredient('B', Material.IRON_INGOT);
        lockRecipe.setIngredient('C', Material.IRON_FENCE);
        plugin.getServer().addRecipe(lockRecipe);
        // Bukkit.getLogger().info("[SimpleLocks] " + (recipeAdded ? "Registered " : "Did NOT register ") + "the recipe for a lock.");

        ItemStack key = getKeyItem();
        ShapedRecipe keyRecipe = new ShapedRecipe(key);
        keyRecipe.shape("AA ", "AA ", "  B");
        keyRecipe.setIngredient('A', Material.STONE_PLATE);
        keyRecipe.setIngredient('B', Material.IRON_INGOT);
        plugin.getServer().addRecipe(keyRecipe);
        // Bukkit.getLogger().info("[SimpleLocks] " + (recipeAdded ? "Registered " : "Did NOT register ") + "the recipe for a key.");
    }

    public void incrementHighestKeyId() {
        highestId++;
        plugin.getConfig().set("highestkeyid", highestId);
        try {
            plugin.getConfig().save(plugin.getDataFolder() + "/config.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ItemStack generateUniqueKey() {
        int keyId = highestId;
        String keyOutput = String.format("%014d", keyId);

        ItemStack key = new ItemStack(Material.TRIPWIRE_HOOK, 1);
        ItemMeta meta = key.getItemMeta();
        meta.setDisplayName("Key");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.AQUA + " " + ChatColor.ITALIC + "Unlocks something.");
        lore.add(ChatColor.AQUA + "Key code:");
        lore.add(keyOutput);
        meta.setLore(lore);
        key.setItemMeta(meta);
        return key;
    }

    public ItemStack generateKey(int keyId) {
        String keyOutput = String.format("%014d", keyId);

        ItemStack key = new ItemStack(Material.TRIPWIRE_HOOK, 1);
        ItemMeta meta = key.getItemMeta();
        meta.setDisplayName("Key");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.AQUA + " " + ChatColor.ITALIC + "Unlocks something.");
        lore.add(ChatColor.AQUA + "Key code:");
        lore.add(keyOutput);
        meta.setLore(lore);
        key.setItemMeta(meta);
        return key;
    }

    public ItemStack generateAdminKey() {
        ItemStack key = new ItemStack(Material.TRIPWIRE_HOOK, 1);
        ItemMeta meta = key.getItemMeta();
        meta.setDisplayName("Key");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.AQUA + " " + ChatColor.ITALIC + "Unlocks everything.");
        lore.add(ChatColor.AQUA + "Key code:");
        lore.add(ChatColor.RED + "Admin");
        meta.setLore(lore);
        key.setItemMeta(meta);
        return key;
    }

    public ItemStack generateLock(int keyId) {
        String keyOutput = String.format("%014d", keyId);

        ItemStack lock = new ItemStack(Material.STONE_BUTTON, 1);
        ItemMeta meta = lock.getItemMeta();
        meta.setDisplayName("Lock");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.AQUA + " " + ChatColor.ITALIC + "Locks just about anything.");
        lore.add(ChatColor.AQUA + "Key code:");
        lore.add(keyOutput);
        meta.setLore(lore);
        lock.setItemMeta(meta);
        return lock;
    }

    public ItemStack getLockItem() {
        ItemStack lock = new ItemStack(Material.STONE_BUTTON, 1);
        ItemMeta meta = lock.getItemMeta();
        meta.setDisplayName("Incomplete Lock");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.AQUA + " " + ChatColor.ITALIC + "Locks nothing at all.");
        meta.setLore(lore);
        lock.setItemMeta(meta);
        return lock;
    }

    public ItemStack getKeyItem() {
        ItemStack key = new ItemStack(Material.TRIPWIRE_HOOK, 1);
        ItemMeta meta = key.getItemMeta();
        meta.setDisplayName("Incomplete Key");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.AQUA + " " + ChatColor.ITALIC + "Unlocks nothing at all.");
        meta.setLore(lore);
        key.setItemMeta(meta);
        return key;
    }

    public void checkAnvilInventory(final Inventory anvil, final Player player, int delay) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (anvil.getItem(0) != null && anvil.getItem(0).getItemMeta() != null) {
                    if (player.hasPermission("simplelocks.player.craft.key")) {
                        if (anvil.getItem(0).getType() == Material.TRIPWIRE_HOOK && anvil.getItem(0).getItemMeta().getDisplayName().equals("Incomplete Key")) {
                            // There is incomplete key in anvil
                            if (anvil.getItem(1) == null || anvil.getItem(1).getType() == Material.AIR) {
                                if (anvil.getItem(2) == null || anvil.getItem(2).getType() == Material.AIR) {
                                    // Output slot is empty
                                    ItemStack key = generateUniqueKey();
                                    anvil.setItem(2, key);
                                }
                            }
                        }
                    }
                    if (player.hasPermission("simplelocks.player.craft.copykey")) {
                        if (anvil.getItem(0).getType() == Material.TRIPWIRE_HOOK && anvil.getItem(0).getItemMeta().getDisplayName().equals("Key")) {
                            if (anvil.getItem(1) != null && anvil.getItem(1).getItemMeta() != null && anvil.getItem(1).getType() == Material.TRIPWIRE_HOOK
                                    && anvil.getItem(1).getItemMeta().getDisplayName().equals("Incomplete Key")) {
                                ItemStack toClone = anvil.getItem(0);
                                ItemMeta meta = toClone.getItemMeta();
                                List<String> lore = meta.getLore();
                                String id = lore.get(2);
                                int keyId = Integer.parseInt(id);
                                ItemStack cloned = generateKey(keyId);
                                anvil.setItem(2, cloned);

                            }
                        }
                    }
                    if (player.hasPermission("simplelocks.player.craft.lock")) {
                        if (anvil.getItem(0).getType() == Material.TRIPWIRE_HOOK && anvil.getItem(0).getItemMeta().getDisplayName().equals("Key")) {
                            if (anvil.getItem(1) != null && anvil.getItem(1).getItemMeta() != null && anvil.getItem(1).getType() == Material.STONE_BUTTON
                                    && anvil.getItem(1).getItemMeta().getDisplayName().equals("Incomplete Lock")) {
                                ItemStack toClone = anvil.getItem(0);
                                ItemMeta meta = toClone.getItemMeta();
                                List<String> lore = meta.getLore();
                                String id = lore.get(2);
                                int keyId = Integer.parseInt(id);
                                ItemStack cloned = generateLock(keyId);
                                anvil.setItem(2, cloned);

                            }
                        }
                    }
                }
            }
        }, delay);
    }

}
