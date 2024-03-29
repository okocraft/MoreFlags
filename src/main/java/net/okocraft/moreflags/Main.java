package net.okocraft.moreflags;

import net.okocraft.moreflags.listener.BeaconEffectListener;
import net.okocraft.moreflags.listener.BlockListener;
import net.okocraft.moreflags.listener.DeathMessageListener;
import net.okocraft.moreflags.listener.EggSpawnChickListener;
import net.okocraft.moreflags.listener.EnderPearlListener;
import net.okocraft.moreflags.listener.RaidListener;
import net.okocraft.moreflags.listener.SignEditListener;
import net.okocraft.moreflags.listener.VehicleMoveListener;
import net.okocraft.moreflags.listener.WorldGuardInternalListener;
import net.okocraft.moreflags.protocollib.ProtocolLibHook;
import net.okocraft.moreflags.util.PlatformHelper;
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
        pm.registerEvents(new VehicleMoveListener(), this);
        pm.registerEvents(new WorldGuardInternalListener(), this);
        pm.registerEvents(new RaidListener(this), this);
        pm.registerEvents(new EggSpawnChickListener(), this);
        pm.registerEvents(new SignEditListener(), this);
        pm.registerEvents(new BlockListener(), this);

        if (PlatformHelper.isFolia()) {
            pm.registerEvents(new EnderPearlListener(), this);
        }

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
