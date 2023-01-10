package net.okocraft.moreflags;

import net.okocraft.moreflags.listener.BeaconEffectListener;
import net.okocraft.moreflags.listener.DeathMessageListener;
import net.okocraft.moreflags.listener.RaidListener;
import net.okocraft.moreflags.listener.VehicleMoveListener;
import net.okocraft.moreflags.listener.WorldGuardInternalListener;
import net.okocraft.moreflags.protocollib.ProtocolLibHook;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private final ProtocolLibHook protocolLibHook = new ProtocolLibHook(this);

    @Override
    public void onLoad() {
        CustomFlags.init();
    }

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BeaconEffectListener(), this);
        pm.registerEvents(new DeathMessageListener(this), this);
        pm.registerEvents(new VehicleMoveListener(this), this);
        pm.registerEvents(new WorldGuardInternalListener(), this);
        pm.registerEvents(new RaidListener(this), this);

        if (!protocolLibHook.registerHandlers()) {
            getLogger().info("ProtocolLib is not installed. death-message flag will have some side effects...");
        }
    }

    @Override
    public void onDisable() {
        if (protocolLibHook.hasProtocolLib()) {
            protocolLibHook.unregisterHandlers();
        }
    }

    public ProtocolLibHook getProtocolLibHook() {
        return this.protocolLibHook;
    }
}
