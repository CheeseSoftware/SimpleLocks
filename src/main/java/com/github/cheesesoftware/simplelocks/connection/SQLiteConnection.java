package com.github.cheesesoftware.simplelocks.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.github.cheesesoftware.simplelocks.LockedBlock;
import com.github.cheesesoftware.simplelocks.SimpleLocks;

public class SQLiteConnection implements IConnection {

    private SimpleLocks plugin;
    private Connection conn;

    public SQLiteConnection(SimpleLocks plugin) {
        this.plugin = plugin;
        conn = openConnection();

        String groupsTable = "CREATE TABLE IF NOT EXISTS `lockedblocks` (`id` INTEGER,`x` INT NOT NULL,`y` INT NOT NULL,`z` INT NOT NULL,`world` VARCHAR(128) NOT NULL,`keyid` INT NOT NULL,`owneruuid` CHAR(36) NOT NULL,`locked` BIT NOT NULL, PRIMARY KEY (`id`));";
        try {
            getConnection().prepareStatement(groupsTable).execute();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        plugin.getLogger().info("Connected to SQLite.");
    }

    public synchronized Connection getConnection() {
        try {
            if (conn != null && conn.isClosed()) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
                conn = null;
            }

            if (conn == null) {
                conn = openConnection();
            }
            return conn;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private synchronized Connection openConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/lockedblocks.db");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<LockedBlock> getLockedBlocks() {
        List<LockedBlock> lockedBlocks = new ArrayList<LockedBlock>();
        try {
            PreparedStatement s = getConnection().prepareStatement("SELECT * FROM lockedblocks");
            s.execute();
            ResultSet result = s.getResultSet();
            while (result.next()) {
                Location location = new Location(Bukkit.getWorld(result.getString("world")), result.getInt("x"), result.getInt("y"), result.getInt("z"));
                int keyId = result.getInt("keyid");
                UUID owner = UUID.fromString(result.getString("owneruuid"));
                boolean locked = result.getBoolean("locked");

                LockedBlock lockedBlock = new LockedBlock(location, keyId, owner);
                lockedBlock.setLocked(locked);
                lockedBlocks.add(lockedBlock);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return lockedBlocks;
    }

    @Override
    public void createLockedBlock(final LockedBlock lockedBlock) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

            @Override
            public void run() {
                PreparedStatement s;
                try {
                    //s = getConnection().prepareStatement("INSERT INTO lockedblocks SET `x`=?, `y`=?, `z`=?, `world`=?, `keyid`=?, `owneruuid`=?, `locked`=?");
                    s = getConnection().prepareStatement("INSERT INTO lockedblocks VALUES (NULL, ?, ?, ?, ?, ?, ?, ?)");
                    s.setInt(1, lockedBlock.getLocation().getBlockX());
                    s.setInt(2, lockedBlock.getLocation().getBlockY());
                    s.setInt(3, lockedBlock.getLocation().getBlockZ());
                    s.setString(4, lockedBlock.getLocation().getWorld().getName());
                    s.setInt(5, lockedBlock.getKeyId());
                    s.setString(6, lockedBlock.getOwner().toString());
                    s.setBoolean(7, lockedBlock.isLocked());
                    s.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void removeLockedBlock(final LockedBlock lockedBlock) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

            @Override
            public void run() {
                try {
                    PreparedStatement s = getConnection().prepareStatement("DELETE FROM lockedblocks WHERE `x`=? AND `y`=? AND `z`=? AND `world`=?");
                    s.setInt(1, lockedBlock.getLocation().getBlockX());
                    s.setInt(2, lockedBlock.getLocation().getBlockY());
                    s.setInt(3, lockedBlock.getLocation().getBlockZ());
                    s.setString(4, lockedBlock.getLocation().getWorld().getName());
                    s.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void saveLockStatus(final LockedBlock lockedBlock) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

            @Override
            public void run() {
                try {
                    PreparedStatement s = getConnection().prepareStatement("UPDATE lockedblocks SET `locked`=? WHERE `x`=? AND `y`=? AND `z`=? AND `world`=?");
                    s.setBoolean(1, lockedBlock.isLocked());
                    s.setInt(2, lockedBlock.getLocation().getBlockX());
                    s.setInt(3, lockedBlock.getLocation().getBlockY());
                    s.setInt(4, lockedBlock.getLocation().getBlockZ());
                    s.setString(5, lockedBlock.getLocation().getWorld().getName());
                    s.execute();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

}
