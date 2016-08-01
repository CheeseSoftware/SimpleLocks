package com.github.cheesesoftware.simplelocks;

import java.util.UUID;

import org.bukkit.Location;

public class LockedBlock {
    
    private Location location;
    private int keyId;
    private UUID owner;
    private boolean locked = false;
    
    public LockedBlock(Location location, int keyId, UUID owner) {
        this.location = location;
        this.keyId = keyId;
        this.owner = owner;
    }
    
    public int getKeyId() {
        return this.keyId;
    }
    
    public UUID getOwner() {
        return this.owner;
    }
    
    public Location getLocation() {
        return this.location;
    }
    
    public boolean isLocked() {
        return this.locked;
    }
    
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

}
