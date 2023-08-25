package net.okocraft.moreflags.listener;

import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import net.okocraft.moreflags.util.FlagUtil;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.jetbrains.annotations.NotNull;

public class EnderPearlListener extends WorldGuardInternalListener {

    @EventHandler
    public void onHit(@NotNull ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof EnderPearl enderPearl) || !(enderPearl.getShooter() instanceof Player player)) {
            return;
        }

        var localPlayer = getPlugin().wrapPlayer(player);

        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
            return;
        }

        StateFlag.State state = FlagUtil.queryValue(
                enderPearl.getWorld(),
                enderPearl.getLocation(),
                localPlayer,
                Flags.ENDERPEARL
        );

        if (state == StateFlag.State.DENY) {
            event.setCancelled(true);
            enderPearl.remove();
            AbstractWorldGuardInternalListener.tellErrorMessage(
                    player,
                    enderPearl.getLocation(),
                    TranslatableComponent.of("worldguard.error.denied.what.use-that")
            );
        }
    }
}
