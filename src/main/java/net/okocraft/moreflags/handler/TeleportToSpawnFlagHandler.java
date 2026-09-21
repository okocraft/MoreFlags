package net.okocraft.moreflags.handler;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import net.kyori.adventure.key.Key;
import net.okocraft.moreflags.CustomFlags;
import net.okocraft.moreflags.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class TeleportToSpawnFlagHandler extends Handler {

    public TeleportToSpawnFlagHandler(Session session) {
        super(session);
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
        String flagValue = toSet.queryValue(player, CustomFlags.TELEPORT_TO_SPAWN_ON_ENTRY);
        if (flagValue == null) {
            return true;
        }

        org.bukkit.Location spawnLocation = findWorldSpawnLocation(flagValue);
        if (spawnLocation == null) {
            return true;
        }

        Player bukkitPlayer = BukkitAdapter.adapt(player);
        bukkitPlayer.getScheduler().run(JavaPlugin.getPlugin(Main.class), _ -> bukkitPlayer.teleportAsync(spawnLocation), null);
        return true;
    }

    @SuppressWarnings("PatternValidation")
    private static @Nullable org.bukkit.Location findWorldSpawnLocation(String worldKey) {
        Key key;
        try {
            key = Key.key(worldKey);
        } catch (IllegalArgumentException e) {
            return null;
        }

        World world = Bukkit.getWorld(key);
        if (world == null) {
            return null;
        }

        return world.getSpawnLocation();
    }
}
