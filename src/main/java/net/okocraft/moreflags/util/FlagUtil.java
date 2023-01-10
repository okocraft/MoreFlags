/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.okocraft.moreflags.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.FlagValueCalculator;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.bukkit.entity.Player;

public final class FlagUtil {

    private FlagUtil() {
    }

    public static boolean testState(Player player, StateFlag flag) {
        return testState(WorldGuardPlugin.inst().wrapPlayer(player), flag);
    }

    public static boolean testState(LocalPlayer player, StateFlag flag) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer()
                .createQuery().getApplicableRegions(player.getLocation())
                .testState(player, flag);
    }

    public static <T> T queryValue(org.bukkit.World world, org.bukkit.Location pos, @Nullable RegionAssociable subject, Flag<T> flag) {
        RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(world));
        if (rm == null) {
            return null;
        }

        return rm.getApplicableRegions(BlockVector3.at(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()))
                .queryValue(subject, flag);
    }

    public static boolean contains(Player player, Collection<ProtectedRegion> regions) {
        BlockVector3 pos = WorldGuardPlugin.inst().wrapPlayer(player).getLocation().toVector().toBlockPoint();
        return regions.stream().anyMatch(r -> r.contains(pos));
    }

    public static <V> Map<ProtectedRegion, V> queryAllValues(Location location, @Nullable RegionAssociable subject, Flag<V> flag) {
        checkNotNull(flag);

        // Check to see whether we have a subject if this is BUILD
        if (flag.requiresSubject() && subject == null) {
            throw new NullPointerException("The " + flag.getName() + " flag is handled in a special fashion and requires a non-null subject parameter");
        }

        if (!(location.getExtent() instanceof World world)) {
            return new HashMap<>();
        }

        RegionContainer rc = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager rm = rc.get(world);
        if (rm == null) {
            return new HashMap<>();
        }


        ApplicableRegionSet regions = rc.createQuery().getApplicableRegions(location);
        ProtectedRegion globalRegion = rm.getRegion("__global__");
        FlagValueCalculator calculator = new FlagValueCalculator(new ArrayList<>(regions.getRegions()), globalRegion);

        int minimumPriority = Integer.MIN_VALUE;

        Map<ProtectedRegion, V> consideredValues = new HashMap<>();
        Set<ProtectedRegion> ignoredParents = new HashSet<>();

        for (ProtectedRegion region : regions) {
            int priority = region.getPriority();

            if (priority < minimumPriority) {
                break;
            }

            if (ignoredParents.contains(region)) {
                continue;
            }

            V value = calculator.getEffectiveFlag(region, flag, subject);

            if (value != null) {
                if (priority > minimumPriority) {
                    minimumPriority = priority;
                    consideredValues.clear();
                }
                consideredValues.put(region, value);
            }

            ProtectedRegion parent = region.getParent();
            while (parent != null) {
                ignoredParents.add(parent);
                parent = parent.getParent();
            }

            // The BUILD flag is implicitly set on every region where
            // PASSTHROUGH is not set to ALLOW
            if (priority != minimumPriority && flag.implicitlySetWithMembership()
                    && FlagValueCalculator.getEffectiveFlagOf(region, Flags.PASSTHROUGH, subject) != StateFlag.State.ALLOW) {
                minimumPriority = priority;
            }
        }

        if (flag.usesMembershipAsDefault() && consideredValues.isEmpty()) {
            if (calculator.getMembership(subject) == FlagValueCalculator.Result.SUCCESS) {
                consideredValues.put(globalRegion, (V) StateFlag.State.ALLOW);
            }
            return consideredValues;
        }

        if (consideredValues.isEmpty()) {
            V fallback = flag.getDefault();
            if (fallback != null) {
                consideredValues.put(globalRegion, fallback);
            }
        }

        return consideredValues;
    }

}
