package com.github.cheesesoftware.simplelocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public class LockManager {

    private Map<Location, LockedBlock> lockedBlocks = new HashMap<Location, LockedBlock>();
    private SimpleLocks plugin;

    public LockManager(SimpleLocks plugin) {
        this.plugin = plugin;

        List<LockedBlock> lockedBlocks = plugin.getConnection().getLockedBlocks();
        for (LockedBlock lockedBlock : lockedBlocks) {
            this.lockedBlocks.put(lockedBlock.getLocation(), lockedBlock);
        }
    }

    public LockedBlock createLockedBlock(Block block, Player owner, int keyId) {
        LockedBlock lockedBlock = new LockedBlock(block.getLocation(), keyId, owner.getUniqueId());
        lockedBlocks.put(lockedBlock.getLocation(), lockedBlock);

        plugin.getConnection().createLockedBlock(lockedBlock);

        return lockedBlock;
    }

    public LockedBlock getLockedBlock(Block block) {
        if (block == null)
            return null;

        LockedBlock lockedBlock = lockedBlocks.get(block.getLocation());
        if (lockedBlock != null)
            return lockedBlock;

        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            InventoryHolder ih = chest.getInventory().getHolder();
            if (ih instanceof DoubleChest) {
                DoubleChest dc = (DoubleChest) ih;
                Chest left = (Chest) dc.getLeftSide();
                Chest right = (Chest) dc.getRightSide();
                if (left.getLocation().equals(block.getLocation())) {
                    lockedBlock = lockedBlocks.get(right.getLocation());
                    if (lockedBlock != null)
                        return lockedBlock;
                } else {
                    lockedBlock = lockedBlocks.get(left.getLocation());
                    if (lockedBlock != null)
                        return lockedBlock;
                }
            }
        } else if (block.getType() == Material.IRON_DOOR_BLOCK || block.getType() == Material.WOODEN_DOOR) {
            if (block.getRelative(BlockFace.UP).getType() == block.getType()) {
                // Found second part of the door up
                lockedBlock = lockedBlocks.get(block.getRelative(BlockFace.UP).getLocation());
                if (lockedBlock != null)
                    return lockedBlock;
            } else if (block.getRelative(BlockFace.DOWN).getType() == block.getType()) {
                // Found second part of the door down
                lockedBlock = lockedBlocks.get(block.getRelative(BlockFace.DOWN).getLocation());
                if (lockedBlock != null)
                    return lockedBlock;
            }
        }
        return null;
    }

    public boolean isBlockLocked(Block block) {
        LockedBlock lockedBlock = getLockedBlock(block);
        if (lockedBlock != null) {
            return lockedBlock.isLocked();
        }
        return false;
    }

    public boolean removeLockedBlock(Block block) {
        LockedBlock lockedBlock = getLockedBlock(block);
        if (lockedBlock != null) {
            lockedBlocks.remove(lockedBlock.getLocation());

            plugin.getConnection().removeLockedBlock(lockedBlock);

            return true;
        }
        return false;
    }

    public boolean setBlockLocked(Block block, boolean value) {
        LockedBlock lockedBlock = getLockedBlock(block);
        setBlockLocked(lockedBlock, value);
        return false;
    }

    public boolean setBlockLocked(LockedBlock block, boolean value) {
        if (block != null) {
            block.setLocked(value);

            plugin.getConnection().saveLockStatus(block);

            return true;
        }
        return false;
    }
}
