package net.okocraft.moreflags.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import net.okocraft.moreflags.CustomFlags;
import net.okocraft.moreflags.Main;
import net.okocraft.moreflags.util.FlagUtil;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Raid;
import org.bukkit.Statistic;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RaidListener implements Listener {

    private static final NamespacedKey CAUSE_ID_KEY = new NamespacedKey("moreflags", "raid/cause_id");

    private final Main plugin;
    private final Advancement heroOfTheVillage;

    public RaidListener(Main plugin) {
        this.plugin = plugin;
        this.heroOfTheVillage = plugin.getServer()
                .getAdvancement(NamespacedKey.minecraft("adventure/hero_of_the_village"));
    }

    @EventHandler(ignoreCancelled = true)
    private void onRaidTrigger(RaidTriggerEvent event) {
        if (!canRaid(event.getRaid())) {
            event.setCancelled(true);
        } else {
            UUID causeID = event.getPlayer().getUniqueId();
            event.getRaid().getPersistentDataContainer().set(CAUSE_ID_KEY, PersistentDataType.LONG_ARRAY, new long[]{causeID.getMostSignificantBits(), causeID.getLeastSignificantBits()});
        }
    }

    @EventHandler
    private void onRaidSpawnWave(RaidSpawnWaveEvent event) {
        if (!canRaid(event.getRaid())) {
            // DON'T USE event.getRaiders(). IT'S BROKEN.
            event.getRaid().getRaiders().forEach(Entity::remove);
        }
    }

    @EventHandler
    private void onRaidFinish(RaidFinishEvent event) {
        if (!canRaid(event.getRaid())) {
            event.getWinners().forEach(p -> p.getPotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE));
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if (event.getCause() != EntityPotionEffectEvent.Cause.POTION_DRINK ||
            event.getNewEffect() == null ||
            event.getNewEffect().getType() != PotionEffectType.BAD_OMEN ||
            !(event.getEntity() instanceof Player player)) {
            return;
        }
        Raid raid = getParticipatingRaid(player);
        if (raid != null && !canRaid(raid)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerStatisticIncrement(PlayerStatisticIncrementEvent event) {
        if (event.getStatistic() != Statistic.RAID_WIN) {
            return;
        }
        Player player = event.getPlayer();
        Raid raid = getParticipatingRaid(player);
        if (raid == null || canRaid(raid)) {
            return;
        }

        event.setCancelled(true);

        Set<String> current = Set.copyOf(player.getAdvancementProgress(heroOfTheVillage).getAwardedCriteria());

        player.getScheduler().runDelayed(
                this.plugin,
                ignored -> {
                    AdvancementProgress ap = player.getAdvancementProgress(heroOfTheVillage);
                    Set<String> awarded = new HashSet<>(ap.getAwardedCriteria());

                    if (!awarded.equals(current)) {
                        awarded.removeAll(current);
                        awarded.forEach(ap::revokeCriteria);
                        plugin.getLogger().info("The completion of challenge has been cancelled.");
                    }
                }, null, 1L
        );
    }

    @Nullable
    public static Raid getParticipatingRaid(Player player) {
        return player.getWorld().getRaids().stream()
                .filter(r -> r.getHeroes().contains(player.getUniqueId()))
                .sorted(Comparator.comparing(raid -> raid.getLocation().distanceSquared(player.getLocation())))
                .findAny()
                .orElse(null);
    }

    private boolean canRaid(Raid raid) {
        LocalPlayer subject;
        Location raidPos = raid.getLocation();

        long[] causeIdData = raid.getPersistentDataContainer().get(CAUSE_ID_KEY, PersistentDataType.LONG_ARRAY);
        if (causeIdData != null && causeIdData.length  == 2) {
            Player causePlayer = plugin.getServer().getPlayer(new UUID(causeIdData[0], causeIdData[1]));

            if (causePlayer != null) {
                subject = WorldGuardPlugin.inst().wrapPlayer(causePlayer);
                World weWorld = BukkitAdapter.adapt(raidPos.getWorld());

                if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(subject, weWorld)) {
                    return true;
                }
            } else {
                subject = null;
            }
        } else {
            subject = null;
        }
        return FlagUtil.queryValue(raidPos.getWorld(), raidPos, subject, CustomFlags.RAID) == StateFlag.State.ALLOW;
    }
}
