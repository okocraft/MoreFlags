package net.okocraft.moreflags.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.RegionResultSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.okocraft.moreflags.CustomFlags;
import net.okocraft.moreflags.Main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class VehicleMoveListener extends AbstractWorldGuardInternalListener {

    private final Map<UUID, BlockVector3> locationHistory = new HashMap<>();
    private final Map<UUID, Set<ProtectedRegion>> toRegionsHistory = new HashMap<>();

    private final Main plugin;

    public VehicleMoveListener(Main plugin) {
        this.plugin = plugin;
    }

    private static BlockVector3 getLocation(Entity player) {
        return BlockVector3.at(
                player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ()
        );
    }

    @EventHandler
    private void onWorldChange(PlayerChangedWorldEvent event) {
        locationHistory.remove(event.getPlayer().getUniqueId());
        toRegionsHistory.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        locationHistory.remove(event.getPlayer().getUniqueId());
        toRegionsHistory.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    private void onMoveEvent(VehicleMoveEvent event) {
        BlockVector3 from = locationHistory.get(event.getVehicle().getUniqueId());
        BlockVector3 to = getLocation(event.getVehicle());
        if (from == null || !from.equals(to)) {
            locationHistory.put(event.getVehicle().getUniqueId(), to);
            onPlayerChangeBlockPoint(event, from, to);
        }
    }

    private void onPlayerChangeBlockPoint(VehicleMoveEvent event, BlockVector3 from, BlockVector3 to) {
        if (event.getVehicle().getPassengers().isEmpty() || !(event.getVehicle().getPassengers().get(0) instanceof Player player)) {
            return;
        }
        LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(player);
        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(lp, lp.getWorld())) {
            return;
        }
        RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(event.getVehicle().getWorld()));
        if (rm == null) {
            return;
        }

        ApplicableRegionSet toRegionSet = rm.getApplicableRegions(to);

        Set<ProtectedRegion> previousToRegions = toRegionsHistory.get(event.getVehicle().getUniqueId());
        Set<ProtectedRegion> toRegions = new HashSet<>(toRegionSet.getRegions());
        if (previousToRegions == null || !previousToRegions.equals(toRegions)) {
            toRegionsHistory.put(event.getVehicle().getUniqueId(), toRegions);
        }
        if (previousToRegions == null) {
            previousToRegions = new HashSet<>();
        }
        if (previousToRegions.equals(toRegions)) {
            return;
        }

        RegionResultSet rrs = new RegionResultSet(toRegions, rm.getRegion("__global__"));
        StateFlag.State state = rrs.queryState(WorldGuardPlugin.inst().wrapPlayer(player), CustomFlags.VEHICLE_ENTRY);
        if (state != StateFlag.State.ALLOW) {
            List<Entity> passengers = event.getVehicle().getPassengers();

            passengers.forEach(event.getVehicle()::removePassenger);
            event.getVehicle().teleport(event.getFrom());

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                event.getVehicle().teleport(event.getFrom().clone().subtract(event.getTo().clone().subtract(event.getFrom())));
                passengers.forEach(event.getVehicle()::addPassenger);
            }, 3L);
        }

    }
}
