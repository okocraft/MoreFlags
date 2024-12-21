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
import net.okocraft.moreflags.util.PlatformHelper;
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
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RaidListener implements Listener {

    public static final String CANCEL_HERO_META_KEY = "cancel_hero";

    private final Main plugin;
    private final FixedMetadataValue cancelHeroMeta;
    private final Advancement heroOfTheVillage;

    private final Map<Integer, UUID> raidIdCauseMap = new HashMap<>();

    public RaidListener(Main plugin) {
        this.plugin = plugin;
        this.cancelHeroMeta = new FixedMetadataValue(plugin, null);
        this.heroOfTheVillage = plugin.getServer()
                .getAdvancement(NamespacedKey.minecraft("adventure/hero_of_the_village"));
    }

    @EventHandler(ignoreCancelled = true)
    private void onRaidTrigger(RaidTriggerEvent event) {
        if (!canRaid(event.getRaid())) {
            event.setCancelled(true);
        } else {
            raidIdCauseMap.put(getRaidId(event.getRaid()), event.getPlayer().getUniqueId());
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

    @EventHandler
    private void onRaidStop(RaidStopEvent event) {
        raidIdCauseMap.remove(getRaidId(event.getRaid()));
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
        Raid raid = getParticipatingRaid(event.getPlayer());
        if (raid == null || canRaid(raid)) {
            return;
        }

        event.getPlayer().setMetadata(CANCEL_HERO_META_KEY, cancelHeroMeta);
        event.setCancelled(true);

        Set<String> current = Set.copyOf(event.getPlayer().getAdvancementProgress(heroOfTheVillage).getAwardedCriteria());

        PlatformHelper.runEntityTask(
                event.getPlayer(),
                player -> {
                    AdvancementProgress ap = player.getAdvancementProgress(heroOfTheVillage);
                    Set<String> awarded = new HashSet<>(ap.getAwardedCriteria());

                    if (!awarded.equals(current)) {
                        awarded.removeAll(current);
                        awarded.forEach(ap::revokeCriteria);
                        plugin.getLogger().info("The completion of challenge has been cancelled.");
                    }

                    player.removeMetadata(CANCEL_HERO_META_KEY, plugin);
                },
                1L
        );
    }

    @Nullable
    public static Raid getParticipatingRaid(Player player) {
        return player.getWorld().getRaids().stream()
                .filter(r -> r.getHeroes().contains(player.getUniqueId()))
                .findAny()
                .orElse(null);
    }

    private boolean canRaid(Raid raid) {
        LocalPlayer subject;
        Location raidPos = raid.getLocation();

        UUID causePlayerId = raidIdCauseMap.get(getRaidId(raid));
        if (causePlayerId == null) {
            subject = null;
        } else {
            Player causePlayer = plugin.getServer().getPlayer(causePlayerId);

            if (causePlayer != null) {
                subject = WorldGuardPlugin.inst().wrapPlayer(causePlayer);
                World weWorld = BukkitAdapter.adapt(raidPos.getWorld());

                if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(subject, weWorld)) {
                    return true;
                }
            } else {
                subject = null;
            }
        }

        return FlagUtil.queryValue(raidPos.getWorld(), raidPos, subject, CustomFlags.RAID) == StateFlag.State.ALLOW;
    }

    private static Field HANDLE_FIELD;
    private static Method GET_ID_METHOD;

    private static int getRaidId(Raid raid) {
        try {
            if (HANDLE_FIELD == null) {
                HANDLE_FIELD = raid.getClass().getDeclaredField("handle");
                HANDLE_FIELD.setAccessible(true);
            }
            HANDLE_FIELD.setAccessible(true);
            Object handle = HANDLE_FIELD.get(raid);
            if (GET_ID_METHOD == null) {
                GET_ID_METHOD = handle.getClass().getMethod("getId");
            }
            return (int) GET_ID_METHOD.invoke(handle);
        } catch (ReflectiveOperationException e) {
            return -1;
        }
    }
}
