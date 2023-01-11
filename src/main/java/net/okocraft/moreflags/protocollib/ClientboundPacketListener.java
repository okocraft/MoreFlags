package net.okocraft.moreflags.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.okocraft.moreflags.Main;
import net.okocraft.moreflags.listener.DeathMessageListener;
import org.bukkit.entity.Player;

public class ClientboundPacketListener extends PacketAdapter {

    private static final PacketType[] PACKET_TYPES_TO_LISTEN = {
            PacketType.Play.Server.SYSTEM_CHAT
    };

    ClientboundPacketListener(Main plugin) {
        super(plugin, PACKET_TYPES_TO_LISTEN);
        this.plugin = plugin;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT) {
            onSystemChatPacket(
                    event.getPlayer(),
                    event.getPacket().getStrings().read(0),
                    event.getPacket().getBooleans().read(0),
                    event
            );
        }
    }

    private void onSystemChatPacket(Player client, String content, boolean overlay, PacketEvent event) {
        if (content == null) {
            return;
        }
        LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(client);
        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(lp, lp.getWorld())) {
            return;
        }

        if (GsonComponentSerializer.gson().deserialize(content) instanceof TranslatableComponent translatable
                && translatable.key().startsWith("death.")
                && client.hasMetadata(DeathMessageListener.METADATA_KEY)) {
            event.setCancelled(true);
        }
    }
}
