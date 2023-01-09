package net.okocraft.moreflags.protocollib;

import com.comphenix.protocol.ProtocolLibrary;
import net.okocraft.moreflags.Main;

public class ProtocolLibHook {

    private final Main plugin;

    private boolean hasProtocolLib = false;

    public ProtocolLibHook(Main plugin) {
        this.plugin = plugin;
    }

    public boolean registerHandlers() {
        try {
            ProtocolLibrary.getProtocolManager().addPacketListener(new ClientboundPacketListener(plugin));
            hasProtocolLib = true;
        } catch (NoClassDefFoundError e) {
            hasProtocolLib = false;
        }
        return hasProtocolLib;
    }

    public void unregisterHandlers() {
        try {
            ProtocolLibrary.getProtocolManager().removePacketListeners(plugin);
        } catch (NoClassDefFoundError ignored) {
        }
        hasProtocolLib = false;
    }

    public boolean hasProtocolLib() {
        return this.hasProtocolLib;
    }
}
