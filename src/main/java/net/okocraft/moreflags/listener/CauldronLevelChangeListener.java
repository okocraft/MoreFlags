package net.okocraft.moreflags.listener;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import net.okocraft.moreflags.CustomFlags;
import net.okocraft.moreflags.util.FlagUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CauldronLevelChangeEvent;

public class CauldronLevelChangeListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void onCauldronLevelChange(CauldronLevelChangeEvent event) {
        CauldronLevelChangeEvent.ChangeReason reason = event.getReason();
        Entity entity = event.getEntity();

        if (entity == null || entity.getType() != EntityType.PLAYER) return;
        if (isNaturalInteract(reason)) return;

        Block block = event.getBlock();
        LocalPlayer player = WorldGuardPlugin.inst().wrapPlayer((Player) entity);
        StateFlag.State state = FlagUtil.queryValue(block.getWorld(), block.getLocation(), player, CustomFlags.INTERACT_CAULDRON);
        if (state == StateFlag.State.DENY) {
            event.setCancelled(true);
        }
    }

    private boolean isNaturalInteract(CauldronLevelChangeEvent.ChangeReason reason) {
        return reason == CauldronLevelChangeEvent.ChangeReason.EXTINGUISH || reason == CauldronLevelChangeEvent.ChangeReason.EVAPORATE || reason == CauldronLevelChangeEvent.ChangeReason.NATURAL_FILL;
    }
}
