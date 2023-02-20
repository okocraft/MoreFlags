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

package net.okocraft.moreflags.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.formatting.text.BlockNbtComponent;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.ComponentBuilder;
import com.sk89q.worldedit.util.formatting.text.KeybindComponent;
import com.sk89q.worldedit.util.formatting.text.NbtComponent;
import com.sk89q.worldedit.util.formatting.text.SelectorComponent;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.serializer.gson.GsonComponentSerializer;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitConfigurationManager;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.BukkitWorldConfiguration;
import com.sk89q.worldguard.bukkit.BukkitWorldGuardPlatform;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.cause.Cause;
import com.sk89q.worldguard.bukkit.event.DelegateEvent;
import com.sk89q.worldguard.bukkit.internal.WGMetadata;
import com.sk89q.worldguard.bukkit.util.InteropUtils;
import com.sk89q.worldguard.commands.CommandUtils;
import com.sk89q.worldguard.config.WorldConfiguration;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.association.Associables;
import com.sk89q.worldguard.protection.association.DelayedRegionOverlapAssociation;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Abstract listener to ease creation of listeners.
 */
class AbstractWorldGuardInternalListener implements Listener {

    private static final String DENY_MESSAGE_KEY = "worldguard.region.lastMessage";
    private static final int LAST_MESSAGE_DELAY = 500;

    /**
     * Get the plugin.
     *
     * @return the plugin
     */
    protected static WorldGuardPlugin getPlugin() {
        return WorldGuardPlugin.inst();
    }

    /**
     * Get the global configuration.
     *
     * @return the configuration
     */
    protected static BukkitConfigurationManager getConfig() {
        return ((BukkitWorldGuardPlatform) WorldGuard.getInstance().getPlatform()).getGlobalStateManager();
    }

    /**
     * Get the world configuration given a world.
     *
     * @param world The world to get the configuration for.
     * @return The configuration for {@code world}
     */
    protected static BukkitWorldConfiguration getWorldConfig(String world) {
        return getWorldConfig(Bukkit.getWorld(world));
    }

    protected static BukkitWorldConfiguration getWorldConfig(org.bukkit.World world) {
        return getConfig().get(BukkitAdapter.adapt(world));
    }

    /**
     * Get the world configuration given a player.
     *
     * @param player The player to get the wold from
     * @return The {@link WorldConfiguration} for the player's world
     */
    protected static BukkitWorldConfiguration getWorldConfig(LocalPlayer player) {
        return getWorldConfig(((BukkitPlayer) player).getPlayer().getWorld());
    }

    /**
     * Return whether region support is enabled.
     *
     * @param world the world
     * @return true if region support is enabled
     */
    protected static boolean isRegionSupportEnabled(org.bukkit.World world) {
        return getWorldConfig(world).useRegions;
    }

    protected RegionAssociable createRegionAssociable(Cause cause) {
        Object rootCause = cause.getRootCause();

        if (!cause.isKnown()) {
            return Associables.constant(Association.NON_MEMBER);
        } else if (rootCause instanceof Player) {
            return getPlugin().wrapPlayer((Player) rootCause);
        } else if (rootCause instanceof OfflinePlayer) {
            return getPlugin().wrapOfflinePlayer((OfflinePlayer) rootCause);
        } else if (rootCause instanceof Entity) {
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            final Entity entity = (Entity) rootCause;
            BukkitWorldConfiguration config = getWorldConfig(entity.getWorld());
            Location loc;
            if (isPaper() && config.usePaperEntityOrigin) {
                loc = entity.getOrigin();
                // Origin world may be null, and thus a Location with a null world created, which cannot be adapted to a WorldEdit location
                if (loc == null || loc.getWorld() == null) {
                    loc = entity.getLocation();
                }
            } else {
                loc = entity.getLocation();
            }
            return new DelayedRegionOverlapAssociation(query, BukkitAdapter.adapt(loc),
                    config.useMaxPriorityAssociation);
        } else if (rootCause instanceof Block) {
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            Location loc = ((Block) rootCause).getLocation();
            return new DelayedRegionOverlapAssociation(query, BukkitAdapter.adapt(loc),
                    getWorldConfig(loc.getWorld()).useMaxPriorityAssociation);
        } else {
            return Associables.constant(Association.NON_MEMBER);
        }
    }

    private static Boolean isPaper = null;
    private static boolean isPaper() {
        if (isPaper == null) {
            try {
                Class.forName("com.destroystokyo.paper.PaperConfig");
                isPaper = true;
            } catch (Throwable t) {
                isPaper = false;
            }
        }
        return isPaper;
    }


    protected void tellErrorMessage(DelegateEvent event, Cause cause, Location location, TranslatableComponent what) {
        if (event.isSilent() || cause.isIndirect()) {
            return;
        }

        Object rootCause = cause.getRootCause();

        if (rootCause instanceof Player) {
            Player player = (Player) rootCause;
            tellErrorMessage(player, location, what);
        }
    }

    public static void tellErrorMessage(Player player, Location location, TranslatableComponent what) {
        long now = System.currentTimeMillis();
        Long lastTime = WGMetadata.getIfPresent(player, DENY_MESSAGE_KEY, Long.class);
        if (lastTime == null || now - lastTime >= LAST_MESSAGE_DELAY) {
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            LocalPlayer localPlayer = getPlugin().wrapPlayer(player);
            Component message = (Component) query.queryValue(
                    BukkitAdapter.adapt(location),
                    localPlayer,
                    Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), "deny-message-component")
            );
            formatAndSendDenyMessage(what, localPlayer, message);
            WGMetadata.put(player, DENY_MESSAGE_KEY, now);
        }
    }

    static void formatAndSendDenyMessage(TranslatableComponent what, LocalPlayer localPlayer, Component message) {
        if (message == null) return;

        String str = GsonComponentSerializer.INSTANCE.serialize(putTranslatableArgs(message, what));
        localPlayer.print(GsonComponentSerializer.INSTANCE.deserialize(CommandUtils.replaceColorMacros(
                WorldGuard.getInstance().getPlatform().getMatcher().replaceMacros(localPlayer, str))));
    }

    private static ComponentBuilder<?, ?> toBuilder(Component component) {
        if (component instanceof BlockNbtComponent) {
            return ((TextComponent) component).toBuilder();
        } else if (component instanceof NbtComponent) {
            return ((NbtComponent<?, ?>) component).toBuilder();
        } else if (component instanceof KeybindComponent) {
            return ((KeybindComponent) component).toBuilder();
        } else if (component instanceof SelectorComponent) {
            return ((SelectorComponent) component).toBuilder();
        } else if (component instanceof TextComponent) {
            return ((TextComponent) component).toBuilder();
        } else if (component instanceof TranslatableComponent) {
            return ((TranslatableComponent) component).toBuilder();
        } else {
            return null;
        }
    }

    private static Component putTranslatableArgs(Component component, Component... args) {
        ComponentBuilder<?, ?> builder = toBuilder(component);
        if (builder == null) return component;

        return builder.mapChildrenDeep(c -> {
            if (c instanceof TranslatableComponent) {
                return ((TranslatableComponent) c).args(args);
            } else {
                return c;
            }
        }).build();
    }

    protected boolean isWhitelisted(Cause cause, World world, boolean pvp) {
        Object rootCause = cause.getRootCause();

        if (rootCause instanceof Player) {
            Player player = (Player) rootCause;
            WorldConfiguration config = getWorldConfig(world);

            if (config.fakePlayerBuildOverride && InteropUtils.isFakePlayer(player)) {
                return true;
            }

            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            return !pvp && WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld());
        } else {
            return false;
        }
    }

    protected static StateFlag[] combine(DelegateEvent event, StateFlag... flag) {
        List<StateFlag> extra = event.getRelevantFlags();
        StateFlag[] flags = Arrays.copyOf(flag, flag.length + extra.size());
        for (int i = 0; i < extra.size(); i++) {
            flags[flag.length + i] = extra.get(i);
        }
        return flags;
    }
}
