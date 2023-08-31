package net.okocraft.moreflags.listener;

import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.event.DelegateEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.protection.flags.StateFlag;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import java.util.List;
import net.okocraft.moreflags.CustomFlags;
import net.okocraft.moreflags.util.FlagUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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

        processEvent(
                signChangeEvent.getPlayer(), signChangeEvent.getBlock(),
                event, event.getBlocks(),
                "worldguard.error.denied.what.place-that-block"
        );
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onOpenLow(@NotNull UseBlockEvent event) {
        if (!(event.getOriginalEvent() instanceof PlayerOpenSignEvent openSignEvent) ||
                !getConfig().get(openSignEvent.getSign().getBlock().getWorld().getName()).useRegions) {
            return;
        }

        event.setSilent(true); // To prevent sending message from RegionProtectionListener
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onOpenHigh(@NotNull UseBlockEvent event) {
        if (!(event.getOriginalEvent() instanceof PlayerOpenSignEvent openSignEvent) ||
                !getConfig().get(openSignEvent.getSign().getBlock().getWorld().getName()).useRegions) {
            return;
        }

        processEvent(
                openSignEvent.getPlayer(), openSignEvent.getSign().getBlock(),
                event, event.getBlocks(),
                "worldguard.error.denied.what.open-that"
        );
    }

    private void processEvent(@NotNull Player player, @NotNull Block block,
                              @NotNull DelegateEvent weEvent, @NotNull List<Block> eventBlocks,
                              @NotNull String denyMessageKey) {
        var localPlayer = getPlugin().wrapPlayer(player);

        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
            return;
        }

        var state = FlagUtil.queryValue(block.getWorld(), block.getLocation(), getPlugin().wrapPlayer(player), CustomFlags.SIGN_EDIT);

        if (weEvent.getResult() == Event.Result.DENY && state == StateFlag.State.ALLOW) {
            // Other flags cancel sign editing, but the sign-edit flag allows it.
            weEvent.setResult(Event.Result.ALLOW);
            eventBlocks.add(block); // Re-add the sign block
        } else if (weEvent.getResult() != Event.Result.DENY && state == StateFlag.State.DENY) {
            // Other flags allow sign editing, but the sign-edit flag disallows it.
            weEvent.setResult(Event.Result.DENY);
            eventBlocks.remove(block); // Remove the sign block
        } /* else {
          // The sign-edit flag is not set, so we follow other flags such as "build" flag.
        } */

        weEvent.setSilent(false);

        if (weEvent.getResult() == Event.Result.DENY) {
            tellErrorMessage(player, block.getLocation(), TranslatableComponent.of(denyMessageKey));
        }
    }
}
