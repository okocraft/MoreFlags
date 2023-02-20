package net.okocraft.moreflags.listener;

import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import net.okocraft.moreflags.CustomFlags;
import net.okocraft.moreflags.util.FlagUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEggThrowEvent;

public class EggSpawnChickListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onPlayerEggThrow(PlayerEggThrowEvent event) {
        if (!event.isHatching()) {
            return;
        }
        StateFlag.State state = FlagUtil.queryValue(
                event.getEgg().getWorld(),
                event.getEgg().getLocation(),
                WorldGuardPlugin.inst().wrapPlayer(event.getPlayer()),
                CustomFlags.EGG_SPAWN_CHICK
        );
        if (state == StateFlag.State.DENY) {
            event.setHatching(false);
            AbstractWorldGuardInternalListener.tellErrorMessage(
                    event.getPlayer(),
                    event.getEgg().getLocation(),
                    TranslatableComponent.of("worldguard.error.denied.what.use-that")
            );
        }
    }
}
