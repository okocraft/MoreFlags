package net.okocraft.moreflags.protocollib;

import com.comphenix.protocol.ProtocolLibrary;
import net.okocraft.moreflags.Main;

public class ProtocolLibHook {

    private final Main plugin;

    public ProtocolLibHook(Main plugin) {
        this.plugin = plugin;
    }

    public void registerHandlers() {
        try {
            ProtocolLibrary.getProtocolManager().addPacketListener(new ClientboundPacketListener(plugin));
        } catch (NoClassDefFoundError ignored) {
        }
    }

    public void unregisterHandlers() {
        try {
            ProtocolLibrary.getProtocolManager().removePacketListeners(plugin);
        } catch (NoClassDefFoundError ignored) {
        }
    }
}
