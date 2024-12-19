package net.okocraft.moreflags.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import net.okocraft.moreflags.CustomFlags;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.jetbrains.annotations.NotNull;

public class CobwebPlaceListener implements Listener {

    @EventHandler
    public void onChangeBlock(@NotNull EntityChangeBlockEvent e) {
        if (e.getTo() != Material.COBWEB) {
            return;
        }

        if (!StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().queryState(BukkitAdapter.adapt(e.getBlock().getLocation()), null, CustomFlags.PLACE_COBWEB_ON_DEATH))) {
            e.setCancelled(true);
        }
    }

}
