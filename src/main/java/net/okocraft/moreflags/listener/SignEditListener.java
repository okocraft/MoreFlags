package net.okocraft.moreflags.listener;

import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.protection.flags.StateFlag;
import net.okocraft.moreflags.CustomFlags;
import net.okocraft.moreflags.util.FlagUtil;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;

public class SignEditListener extends AbstractWorldGuardInternalListener {

    @EventHandler(priority = EventPriority.LOW)
    public void onChangeLow(@NotNull PlaceBlockEvent event) {
        if (!(event.getOriginalEvent() instanceof SignChangeEvent signChangeEvent) ||
                !getConfig().get(signChangeEvent.getBlock().getWorld().getName()).useRegions) {
            return;
        }

        event.setSilent(true); // To prevent sending message from RegionProtectionListener
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChangeHigh(@NotNull PlaceBlockEvent event) {
        if (!(event.getOriginalEvent() instanceof SignChangeEvent signChangeEvent) ||
                !getConfig().get(signChangeEvent.getBlock().getWorld().getName()).useRegions) {
            return;
        }

        var player = signChangeEvent.getPlayer();
        var localPlayer = getPlugin().wrapPlayer(player);

        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
            return;
        }

        var block = signChangeEvent.getBlock();

        var state = FlagUtil.queryValue(
                block.getWorld(),
                block.getLocation(),
                getPlugin().wrapPlayer(player),
                CustomFlags.SIGN_EDIT
        );

        if (event.getResult() == Event.Result.DENY && state == StateFlag.State.ALLOW) {
            // Other flags cancel sign editing, but the sign-edit flag allows it.
            event.setResult(Event.Result.ALLOW);
            event.getBlocks().add(block); // Re-add the sign block
        } else if (event.getResult() != Event.Result.DENY && state == StateFlag.State.DENY) {
            // Other flags allow sign editing, but the sign-edit flag disallows it.
            event.setResult(Event.Result.DENY);
            event.getBlocks().remove(block); // Remove the sign block
        } /* else {
          // The sign-edit flag is not set, so we follow other flags such as "build" flag.
        } */

        event.setSilent(false);

        if (event.getResult() == Event.Result.DENY) {
            tellErrorMessage(player, block.getLocation(), TranslatableComponent.of("worldguard.error.denied.what.place-that-block"));
        }
    }
}
