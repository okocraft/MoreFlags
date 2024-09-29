package net.okocraft.moreflags.listener;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.okocraft.moreflags.CustomFlags;
import net.okocraft.moreflags.event.PlayerArmorDeniedEvent;
import net.okocraft.moreflags.handler.ArmorCheckHandler;
import net.okocraft.moreflags.util.FlagUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class ArmorListener extends WorldGuardInternalListener {

    public static final Pattern SEPARATOR = Pattern.compile(",", Pattern.LITERAL);

    private final Set<UUID> teleporting = Collections.synchronizedSet(new HashSet<>());

    @EventHandler
    public void onChange(@NotNull PlayerArmorChangeEvent event) {
        var localPlayer = getPlugin().wrapPlayer(event.getPlayer());

        if (!ArmorCheckHandler.shouldCheckArmors(localPlayer, null)) {
            return;
        }

        var deniedEvent = ArmorCheckHandler.checkArmor(localPlayer, event.getSlotType(), event.getNewItem());
        if (deniedEvent != null) {
            deniedEvent.callEvent();
        }
    }

    @EventHandler
    public void onDenied(@NotNull PlayerArmorDeniedEvent event) {
        var player = event.getPlayer();
        var localPlayer = getPlugin().wrapPlayer(event.getPlayer());

        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
            return;
        }

        this.sendMessageIfFlagExists(player, localPlayer);
        this.teleportPlayerIfFlagExists(player, localPlayer);
    }

    private void sendMessageIfFlagExists(Player bukkitPlayer, LocalPlayer localPlayer) {
        var message = FlagUtil.queryValueForPlayer(localPlayer, CustomFlags.MESSAGE_ON_ARMOR_DENIED);
        if (message != null && !message.isEmpty()) {
            bukkitPlayer.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
        }
    }

    private void teleportPlayerIfFlagExists(Player bukkitPlayer, LocalPlayer localPlayer) {
        var location = toLocation(bukkitPlayer, FlagUtil.queryValueForPlayer(localPlayer, CustomFlags.TELEPORT_ON_ARMOR_DENIED));
        var uuid = bukkitPlayer.getUniqueId();

        if (location != null && this.teleporting.add(uuid)) {

            bukkitPlayer.teleportAsync(location).thenRunAsync(() -> this.teleporting.remove(uuid));
        }
    }

    private static org.bukkit.Location toLocation(Player source, String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        var elements = SEPARATOR.split(str);

        if (elements.length < 3) {
            return null;
        }

        double x;
        double y;
        double z;

        try {
            x = Double.parseDouble(elements[0]);
            y = Double.parseDouble(elements[1]);
            z = Double.parseDouble(elements[2]);
        } catch (NumberFormatException ignored) {
            return null;
        }

        float yaw;

        if (elements.length < 4) {
            yaw = source.getYaw();
        } else {
            try {
                yaw = Float.parseFloat(elements[3]);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        float pitch;

        if (elements.length < 5) {
            pitch = source.getPitch();
        } else {
            try {
                pitch = Float.parseFloat(elements[4]);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        World world;
        if (elements.length < 6) {
            world = source.getWorld();
        } else {
            world = Bukkit.getWorld(elements[5]);
            if (world == null) {
                return null;
            }
        }

        return new org.bukkit.Location(world, x, y, z, yaw, pitch);
    }
}
