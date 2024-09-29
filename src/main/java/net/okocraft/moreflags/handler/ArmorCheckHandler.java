package net.okocraft.moreflags.handler;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import net.okocraft.moreflags.CustomFlags;
import net.okocraft.moreflags.event.PlayerArmorDeniedEvent;
import net.okocraft.moreflags.util.FlagUtil;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ArmorCheckHandler extends Handler {

    public ArmorCheckHandler(Session session) {
        super(session);
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
        if (shouldCheckArmors(player, toSet)) {
            var deniedEvent = checkArmors(player);
            if (deniedEvent != null) {
                deniedEvent.callEvent();
            }
        }
        return true;
    }

    @SuppressWarnings("RedundantIfStatement")
    public static boolean shouldCheckArmors(@NotNull LocalPlayer player, @Nullable ApplicableRegionSet regions) {
        var platform = WorldGuard.getInstance().getPlatform();

        if (platform.getSessionManager().hasBypass(player, player.getWorld())) {
            return false;
        }

        ApplicableRegionSet regionSet;
        if (regions == null) {
            var rm = platform.getRegionContainer().get(player.getWorld());
            if (rm == null) {
                return false;
            }
            var loc = player.getLocation();
            var vec = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            regionSet = rm.getApplicableRegions(vec);
        } else {
            regionSet = regions;
        }

        var messageFlagValue = regionSet.queryValue(player, CustomFlags.MESSAGE_ON_ARMOR_DENIED);
        var teleportFlagValue = regionSet.queryValue(player, CustomFlags.TELEPORT_ON_ARMOR_DENIED);

        if ((messageFlagValue == null || messageFlagValue.isEmpty()) &&
            (teleportFlagValue == null || teleportFlagValue.isEmpty())) {
            // Don't check armors if these flags are not set.
            return false;
        }

        return true;
    }

    public static @Nullable PlayerArmorDeniedEvent checkArmors(@NotNull LocalPlayer player) {
        for (var slot : PlayerArmorChangeEvent.SlotType.values()) {
            var blacklisted = checkArmor(player, slot, null);
            if (blacklisted != null) {
                return blacklisted;
            }
        }

        return null;
    }

    public static @Nullable PlayerArmorDeniedEvent checkArmor(@NotNull LocalPlayer player, @NotNull PlayerArmorChangeEvent.SlotType slot, @Nullable ItemStack newItem) {
        if (!(player instanceof BukkitPlayer bukkitPlayer)) {
            return null;
        }

        var flag = switch (slot) {
            case HEAD -> CustomFlags.ARMOR_BLACKLIST_HEAD;
            case CHEST -> CustomFlags.ARMOR_BLACKLIST_CHEST;
            case LEGS -> CustomFlags.ARMOR_BLACKLIST_LEGS;
            case FEET -> CustomFlags.ARMOR_BLACKLIST_FEET;
        };

        var blacklistedItems = FlagUtil.queryValueForPlayer(player, flag);

        if (blacklistedItems == null) {
            return null;
        }

        ItemStack item;

        if (newItem != null) {
            item = newItem;
        } else {
            item = switch (slot) {
                case HEAD -> bukkitPlayer.getPlayer().getInventory().getHelmet();
                case CHEST -> bukkitPlayer.getPlayer().getInventory().getChestplate();
                case FEET -> bukkitPlayer.getPlayer().getInventory().getLeggings();
                case LEGS -> bukkitPlayer.getPlayer().getInventory().getBoots();
            };
        }

        if (item != null && !item.isEmpty() && blacklistedItems.contains(BukkitAdapter.adapt(item).getType())) {
            return new PlayerArmorDeniedEvent(bukkitPlayer.getPlayer(), slot, item);
        }

        return null;
    }
}
