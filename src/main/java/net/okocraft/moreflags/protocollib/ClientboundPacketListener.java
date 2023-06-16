package net.okocraft.moreflags.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.MinecraftKey;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.okocraft.moreflags.Main;
import net.okocraft.moreflags.listener.RaidListener;
import org.bukkit.entity.Player;

public class ClientboundPacketListener extends PacketAdapter {

    private static final PacketType[] PACKET_TYPES_TO_LISTEN = {
            PacketType.Play.Server.SYSTEM_CHAT,
            PacketType.Play.Server.ADVANCEMENTS
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
        } else if (event.getPacketType() == PacketType.Play.Server.ADVANCEMENTS) {
            onUpdateAdvancements(
                    event.getPlayer(),
                    event.getPacket().getBooleans().read(0),
                    event.getPacket().getMaps(MinecraftKey.getConverter(), Converters.passthrough(null)).read(0),
                    event.getPacket().getSets(MinecraftKey.getConverter()).read(0),
                    event.getPacket().getMaps(MinecraftKey.getConverter(), Converters.passthrough(null)).read(1),
                    event
            );
        }
    }

    private void onSystemChatPacket(Player client, String content, boolean overlay, PacketEvent event) {
        TranslatableComponent translatable;
        if (content != null && GsonComponentSerializer.gson().deserialize(content) instanceof TranslatableComponent t) {
            translatable = t;
        } else {
            Object adventureContent = event.getPacket().getStructures().read(0).getHandle();
            if (adventureContent instanceof TranslatableComponent t) {
                translatable = t;
            } else {
                return;
            }
        }

        if (translatable.key().equals("chat.type.advancement.challenge")
                && translatable.args().size() >= 2
                && translatable.args().get(1) instanceof TranslatableComponent bracket
                && bracket.key().equals("chat.square_brackets")
                && bracket.args().size() >= 1
                && bracket.args().get(0) instanceof TranslatableComponent title
                && title.key().equals("advancements.adventure.hero_of_the_village.title")
                && client.hasMetadata(RaidListener.CANCEL_HERO_META_KEY)) {
            event.setCancelled(true);
        }
    }

    private void onUpdateAdvancements(Player client, boolean reset, Map<MinecraftKey, Object> added, Set<MinecraftKey> removed, Map<MinecraftKey, Object> progress, PacketEvent event) {
        if (added.keySet().stream().noneMatch(k -> k.getKey().contains("hero_of_the_village"))) {
            return;
        }

        if (client.hasMetadata(RaidListener.CANCEL_HERO_META_KEY)) {
            event.setCancelled(true);
        }
    }
}
