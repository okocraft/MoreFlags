package net.okocraft.moreflags.listener;

import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Map;
import net.okocraft.moreflags.CustomFlags;
import net.okocraft.moreflags.util.FlagUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BeaconEffectListener implements Listener {

    @EventHandler
    private void onBeaconEffect(BeaconEffectEvent event) {
        LocalPlayer affected = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());
        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(affected, affected.getWorld())) {
            return;
        }
        Location affectedLoc = affected.getLocation();
        BlockVector3 affectedPoint = affectedLoc.toVector().toBlockPoint();

        Location beaconLoc = BukkitAdapter.adapt(event.getBlock().getLocation());
        BlockVector3 beaconPoint = beaconLoc.toVector().toBlockPoint();

        Map<ProtectedRegion, StateFlag.State> outsideFlagValues = FlagUtil
                .queryAllValues(affectedLoc, null, CustomFlags.PASSTHROUGH_OUTSIDE_BEACON);
        // beacon is outside checking region
        // check any region denying beacon effect from outside the region.
        if (outsideFlagValues.entrySet().stream().anyMatch(e -> e.getValue() == StateFlag.State.DENY && !e.getKey().contains(beaconPoint))) {
            event.setCancelled(true);
            return;
        }

        Map<ProtectedRegion, StateFlag.State> insideFlagValues = FlagUtil
                .queryAllValues(beaconLoc, null, CustomFlags.PASSTHROUGH_INSIDE_BEACON);
        // player is outside checking region.
        // check any region denying beacon effect from inside the region.
        if (insideFlagValues.entrySet().stream().anyMatch(e -> e.getValue() == StateFlag.State.DENY && !e.getKey().contains(affectedPoint))) {
            event.setCancelled(true);
            return;
        }
    }

}
