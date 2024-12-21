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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.okocraft.moreflags.CustomFlags;
import net.okocraft.moreflags.Main;
import net.okocraft.moreflags.util.PlatformHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class VehicleMoveListener extends AbstractWorldGuardInternalListener {

    private final Map<UUID, BlockVector3> locationHistory = new ConcurrentHashMap<>();
    private final Map<UUID, Set<ProtectedRegion>> toRegionsHistory = new ConcurrentHashMap<>();

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
        BlockVector3 to = BukkitAdapter.adapt(event.getVehicle().getLocation()).toVector().toBlockPoint();
        if (from == null || !from.equals(to)) {
            locationHistory.put(event.getVehicle().getUniqueId(), to);
            onPlayerChangeBlockPoint(event, to);
        }
    }

    private void onPlayerChangeBlockPoint(VehicleMoveEvent event, BlockVector3 to) {
        var vehicle = event.getVehicle();
        var passengers = vehicle.getPassengers();
        if (passengers.isEmpty() || !(passengers.get(0) instanceof Player player)) {
            return;
        }
        LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(player);
        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(lp, lp.getWorld())) {
            return;
        }

        RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(vehicle.getWorld()));
        if (rm == null) {
            return;
        }

        ApplicableRegionSet toRegionSet = rm.getApplicableRegions(to);

        Set<ProtectedRegion> previousToRegions = toRegionsHistory.get(vehicle.getUniqueId());
        Set<ProtectedRegion> toRegions = new HashSet<>(toRegionSet.getRegions());
        if (previousToRegions == null || !previousToRegions.equals(toRegions)) {
            toRegionsHistory.put(vehicle.getUniqueId(), toRegions);
        }
        if (previousToRegions == null) {
            previousToRegions = new HashSet<>();
        }
        if (previousToRegions.equals(toRegions)) {
            return;
        }

        RegionResultSet rrs = new RegionResultSet(toRegions, rm.getRegion("__global__"));
        StateFlag.State state = rrs.queryState(WorldGuardPlugin.inst().wrapPlayer(player), CustomFlags.VEHICLE_ENTRY);

        if (state == StateFlag.State.ALLOW) {
            return;
        }

        List<UUID> passengerUuids;

        if (passengers.size() == 1) {
            passengerUuids = List.of(player.getUniqueId());
            vehicle.removePassenger(player);
        } else {
            passengerUuids = new ArrayList<>(passengers.size());
            for (var passenger : passengers) {
                passengerUuids.add(passenger.getUniqueId());
                vehicle.removePassenger(passenger);
            }
        }

        var fromLoc = event.getFrom().clone();
        var toLoc = event.getTo().clone();

        if (PlatformHelper.isFolia()) {
            // In Folia, Entity#teleport throws UnsupportedOperationException
            vehicle.teleportAsync(fromLoc);
        } else {
            vehicle.teleport(fromLoc);
        }

        vehicle.getScheduler().runDelayed(
                Main.getPlugin(Main.class),
                    ignored -> {
                    var teleportTo = fromLoc.multiply(2).subtract(toLoc);

                    if (PlatformHelper.isFolia()) {
                        // In Folia, the returning CompletableFuture can be calling #join. In Paper, this may cause the deadlock.
                        vehicle.teleportAsync(teleportTo).join();
                    } else {
                        vehicle.teleport(teleportTo);
                    }

                    for (var passengerUuid : passengerUuids) {
                        var passenger = vehicle.getWorld().getEntity(passengerUuid);
                        // The passenger is gone or no longer in the same region of the vehicle.
                        if (passenger == null || !passenger.isValid() || !PlatformHelper.isOwnedByCurrentRegion(passenger)) {
                            continue;
                        }

                        vehicle.addPassenger(passenger);
                    }
                }, null, player.getPing() / 25L + 1 // ping * 2 / 50 + 1 ticks
        );
    }
}
