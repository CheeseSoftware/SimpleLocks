package com.github.cheesesoftware.simplelocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.cheesesoftware.simplelocks.commands.CommandSimpleLocks;
import com.github.cheesesoftware.simplelocks.connection.IConnection;
import com.github.cheesesoftware.simplelocks.connection.MySQLConnection;
import com.github.cheesesoftware.simplelocks.connection.SQLiteConnection;
import com.github.cheesesoftware.simplelocks.listeners.CraftingListener;

public class SimpleLocks extends JavaPlugin implements Listener {

    private CraftingManager craftingManager;
    private CraftingListener craftingListener;
    private LockManager lockManager;

    private IConnection connection;

    private static ArrayList<Material> lockableBlocks = new ArrayList<Material>();

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        if (getConfig().getString("dbtype").equals("mysql"))
            connection = new MySQLConnection(this, getConfig().getString("host"), getConfig().getString("database"), getConfig().getInt("port"), getConfig().getString("username"),
                    getConfig().getString("password"));
        else
            connection = new SQLiteConnection(this);
        // else
        // flatfile

        craftingManager = new CraftingManager(this);
        craftingListener = new CraftingListener(craftingManager);
        Bukkit.getPluginManager().registerEvents(craftingListener, this);
        Bukkit.getPluginManager().registerEvents(this, this);

        this.getCommand("simplelocks").setExecutor(new CommandSimpleLocks(craftingManager));

        lockManager = new LockManager(this);

        List<String> lockableBlocksList = getConfig().getStringList("lockableblocks");
        for (String s : lockableBlocksList) {
            try {
                lockableBlocks.add(Material.getMaterial(s));
                Bukkit.getLogger().info("added material " + s);
                Bukkit.getLogger().info("added materialasdasd " + Material.getMaterial(s));
            } catch (Exception e) {
                getLogger().warning("[SimpleLocks] Encountered invalid material \"" + s + "\" in configuration.");
            }
        }
    }

    @Override
    public void onDisable() {

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && lockableBlocks.contains(e.getClickedBlock().getType())) {
            ItemStack item = e.getItem();
            if ((player.hasPermission("simplelocks.player.createlock.*") || player.hasPermission("simplelocks.player.createlock." + e.getClickedBlock().getType().toString())) && player.isSneaking()
                    && item != null && item.getType() == Material.STONE_BUTTON && item.getItemMeta() != null && item.getItemMeta().getDisplayName().equals("Lock")) {
                e.setCancelled(true);
                // Get lock ID
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();
                String id = lore.get(2);
                int keyId = Integer.parseInt(id);

                if (lockManager.getLockedBlock(e.getClickedBlock()) != null) {
                    player.sendMessage(getMessage("lockexists", e.getClickedBlock()));
                    return;
                }

                // Proceed by locking
                lockManager.createLockedBlock(e.getClickedBlock(), player, keyId);
                if (e.getPlayer().getInventory().getItemInMainHand().getAmount() > 1)
                    e.getPlayer().getInventory().getItemInMainHand().setAmount(e.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                else
                    e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR, 1));

                player.sendMessage(getMessage("lockplaced", e.getClickedBlock()));

            } else if (player.hasPermission("simplelocks.player.lock") && item != null && item.getType() == Material.TRIPWIRE_HOOK && item.getItemMeta() != null
                    && item.getItemMeta().getDisplayName().equals("Key")) {
                LockedBlock lockedBlock = lockManager.getLockedBlock(e.getClickedBlock());
                if (lockedBlock != null) {
                    e.setCancelled(true);

                    // Get lock ID
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = meta.getLore();
                    String id = lore.get(2);
                    int keyId = -1;
                    if (!id.endsWith("Admin"))
                        keyId = Integer.parseInt(id);

                    if (id.endsWith("Admin") || lockedBlock.getKeyId() == keyId) {
                        lockManager.setBlockLocked(lockedBlock, !lockedBlock.isLocked());
                        if (lockedBlock.isLocked())
                            player.sendMessage(getMessage("blocklocked", e.getClickedBlock()));
                        else
                            player.sendMessage(getMessage("blockunlocked", e.getClickedBlock()));
                        return;
                    } else
                        player.sendMessage(getMessage("keynomatch", e.getClickedBlock()));
                    return;
                }
            }
        }

        Block block = e.getClickedBlock();
        if (lockManager.isBlockLocked(block)) {
            if (player.hasPermission("simplelocks.admin") || player.getGameMode() == GameMode.CREATIVE) {
                player.sendMessage(getMessage("adminopen", e.getClickedBlock()));
                return;
            }
            e.setCancelled(true);
            player.sendMessage(getMessage("blockislocked", block));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerPlaceBlock(BlockPlaceEvent e) {
        if (e.getItemInHand() != null && e.getItemInHand().getItemMeta() != null) {
            String name = e.getItemInHand().getItemMeta().getDisplayName();
            if (name != null && (name.equals("Incomplete Lock") || name.equals("Lock")))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (!lockManager.isBlockLocked(block) || e.getPlayer().hasPermission("simplelocks.admin") || e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            lockManager.removeLockedBlock(block);
        } else
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBurn(BlockBurnEvent e) {
        // if we allow nature destruction, remove the lockedblock
        // else if there is locked block, setcancelled
        if (getConfig().getBoolean("allownaturedestruction")) {
            Block block = e.getBlock();
            if (lockManager.getLockedBlock(block) != null) {
                lockManager.removeLockedBlock(block);
            }
        } else {
            Block block = e.getBlock();
            if (lockManager.getLockedBlock(block) != null) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent e) {
        // if we allow nature destruction, remove all blown up lockedblocks
        // else if there is any locked blocks, remove them
        if (getConfig().getBoolean("allownaturedestruction")) {
            for (Block block : e.blockList()) {
                if (lockManager.getLockedBlock(block) != null) {
                    lockManager.removeLockedBlock(block);
                }
            }
        } else {
            Iterator<Block> it = e.blockList().iterator();
            while (it.hasNext()) {
                Block block = it.next();
                if (lockManager.getLockedBlock(block) != null) {
                    it.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityBreakDoor(EntityBreakDoorEvent e) {
        // if we allow nature destruction, remove the lockedblock
        // else if there is locked block, setcancelled
        if (getConfig().getBoolean("allownaturedestruction")) {
            Block block = e.getBlock();
            if (lockManager.getLockedBlock(block) != null) {
                lockManager.removeLockedBlock(block);
            }
        } else {
            Block block = e.getBlock();
            if (lockManager.getLockedBlock(block) != null) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        // if we allow nature destruction, remove the lockedblock
        // else if there is locked block, setcancelled
        if (getConfig().getBoolean("allownaturedestruction")) {
            Block block = e.getBlock();
            if (lockManager.getLockedBlock(block) != null) {
                lockManager.removeLockedBlock(block);
            }
        } else {
            Block block = e.getBlock();
            if (lockManager.getLockedBlock(block) != null) {
                e.setCancelled(true);
            }
        }
    }

    public IConnection getConnection() {
        return this.connection;
    }

    private String getMessage(String which, Block block) {
        return ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages." + which).replaceAll("%b", block.getType().toString().toLowerCase().replace("_", " ")));
    }

}
