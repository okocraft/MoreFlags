package net.okocraft.moreflags;

import net.okocraft.moreflags.listener.BeaconEffectListener;
import net.okocraft.moreflags.listener.CauldronLevelChangeListener;
import net.okocraft.moreflags.listener.DeathMessageListener;
import net.okocraft.moreflags.listener.EggSpawnChickListener;
import net.okocraft.moreflags.listener.RaidListener;
import net.okocraft.moreflags.listener.VehicleMoveListener;
import net.okocraft.moreflags.listener.WorldGuardInternalListener;
import net.okocraft.moreflags.protocollib.ProtocolLibHook;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private ProtocolLibHook protocolLibHook = null;

    public Main() {
        try {
            this.protocolLibHook = new ProtocolLibHook(this);
            getLogger().info("Using ProtocolLib.");
        } catch (NoClassDefFoundError ignored) {
        }
    }

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
        pm.registerEvents(new EggSpawnChickListener(), this);
        pm.registerEvents(new CauldronLevelChangeListener(), this);
        if (protocolLibHook != null) {
            protocolLibHook.registerHandlers();
        }
    }

    @Override
    public void onDisable() {
        if (protocolLibHook != null) {
            protocolLibHook.unregisterHandlers();
        }
    }

    public boolean hasProtocolLib() {
        return this.protocolLibHook != null;
    }
}
