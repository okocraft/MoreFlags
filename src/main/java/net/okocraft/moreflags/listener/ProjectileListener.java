package net.okocraft.moreflags.listener;

import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import net.okocraft.moreflags.CustomFlags;
import net.okocraft.moreflags.util.FlagUtil;
import net.okocraft.moreflags.util.PlatformHelper;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileListener extends AbstractWorldGuardInternalListener {

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent event) {
        boolean shouldCancel = switch (event.getEntity()) {
            case EnderPearl enderPearl -> PlatformHelper.isFolia() && this.shouldCancel(enderPearl, Flags.ENDERPEARL);
            case WindCharge windCharge -> this.shouldCancel(windCharge, CustomFlags.WIND_CHARGE);
            default -> false;
        };

        if (shouldCancel) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        boolean shouldCancel = switch (event.getEntity()) {
            case EnderPearl enderPearl -> PlatformHelper.isFolia() && this.shouldCancel(enderPearl, Flags.ENDERPEARL);
            case WindCharge windCharge -> this.shouldCancel(windCharge, CustomFlags.WIND_CHARGE);
            default -> false;
        };

        if (shouldCancel) {
            event.setCancelled(true);
            event.getEntity().remove();
        }
    }

    private <T extends Projectile> boolean shouldCancel(T entity, StateFlag flag) {
        if (!(entity.getShooter() instanceof Player player)) {
            return false;
        }

        var localPlayer = getPlugin().wrapPlayer(player);

        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
            return false;
        }

        StateFlag.State state = FlagUtil.queryValue(
                entity.getWorld(),
                entity.getLocation(),
                localPlayer,
                flag
        );

        if (state == StateFlag.State.DENY) {
            tellErrorMessage(
                    player,
                    entity.getLocation(),
                    TranslatableComponent.of("worldguard.error.denied.what.use-that")
            );
            return true;
        }

        return false;
    }
}
