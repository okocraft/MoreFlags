package net.okocraft.moreflags.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.entity.DamageEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.UseEntityEvent;
import com.sk89q.worldguard.bukkit.util.Entities;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.okocraft.moreflags.CustomFlags;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class WorldGuardInternalListener extends AbstractWorldGuardInternalListener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onUseEntity(UseEntityEvent event) {
        if (event.getResult() == Event.Result.ALLOW) return; // Don't care about events that have been pre-allowed
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause(), event.getWorld(), false)) return; // Whitelisted cause
        if (!Entities.isNPC(event.getEntity())) return;
        if (event.getCause().getRootCause() instanceof Player cause) {
            LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(cause);
            if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(lp, lp.getWorld())) {
                return;
            }
        }

        Location target = event.getTarget();
        RegionAssociable associable = createRegionAssociable(event.getCause());

        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        StateFlag.State state = query.queryState(BukkitAdapter.adapt(target), associable, combine(event, CustomFlags.VILLAGER_TRADE));
        if (state != StateFlag.State.ALLOW) {
            tellErrorMessage(event, event.getCause(), target, TranslatableComponent.of("worldguard.error.denied.what.use-that"));
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDamageEntity(DamageEntityEvent event) {
        if (event.getResult() == Event.Result.ALLOW) return; // Don't care about events that have been pre-allowed
        if (!isRegionSupportEnabled(event.getWorld())) return; // Region support disabled
        if (isWhitelisted(event.getCause(), event.getWorld(), false)) return;
        if (event.getEntity() instanceof Player) return;
        if (event.getEntity().getCustomName() == null) return;
        if (event.getCause().getRootCause() instanceof Player cause) {
            LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(cause);
            if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(lp, lp.getWorld())) {
                return;
            }
        }

        Location target = event.getTarget();
        RegionAssociable associable = createRegionAssociable(event.getCause());

        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        StateFlag.State state = query.queryState(BukkitAdapter.adapt(target), associable, combine(event, CustomFlags.DAMAGE_NAMED_ENTITY));
        if (state != StateFlag.State.ALLOW) {
            tellErrorMessage(event, event.getCause(), target, TranslatableComponent.of("worldguard.error.denied.what.harm-that"));
            event.setCancelled(true);
        }
    }

}
