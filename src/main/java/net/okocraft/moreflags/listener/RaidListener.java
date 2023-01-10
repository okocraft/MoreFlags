package net.okocraft.moreflags.listener;

import com.sk89q.worldguard.protection.flags.StateFlag;
import java.util.List;
import net.okocraft.moreflags.CustomFlags;
import net.okocraft.moreflags.Main;
import net.okocraft.moreflags.util.FlagUtil;
import org.bukkit.entity.Player;
import org.bukkit.entity.Raider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidEvent;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.potion.PotionEffectType;

public class RaidListener implements Listener {

    private final Main plugin;

    public RaidListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    private void onRaidTrigger(RaidTriggerEvent event) {
        if (!canRaid(event)) {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    private void onRaidSpawnWave(RaidSpawnWaveEvent event) {
        if (!canRaid(event)) {
            event.getRaiders().forEach(Raider::remove);
        }
    }

    @EventHandler
    private void onRaid(RaidFinishEvent event) {
        if (!canRaid(event)) {
            List<Player> winners = event.getWinners();
            plugin.getServer().getScheduler().runTask(
                    plugin,
                    () -> winners.forEach(p -> p.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE))
            );
        }
    }

    private boolean canRaid(RaidEvent event) {
        StateFlag.State s = FlagUtil.queryValue(event.getWorld(), event.getRaid().getLocation(), null, CustomFlags.RAID);
        return s != null && s != StateFlag.State.DENY;
    }
}
