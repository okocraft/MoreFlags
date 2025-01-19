package net.okocraft.moreflags;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import net.okocraft.moreflags.handler.ArmorCheckHandler;
import net.okocraft.moreflags.listener.ArmorListener;
import net.okocraft.moreflags.listener.BeaconEffectListener;
import net.okocraft.moreflags.listener.BlockListener;
import net.okocraft.moreflags.listener.CobwebPlaceListener;
import net.okocraft.moreflags.listener.DeathMessageListener;
import net.okocraft.moreflags.listener.EggSpawnChickListener;
import net.okocraft.moreflags.listener.ProjectileListener;
import net.okocraft.moreflags.listener.RaidListener;
import net.okocraft.moreflags.listener.VehicleMoveListener;
import net.okocraft.moreflags.listener.WorldGuardInternalListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onLoad() {
        CustomFlags.init();
    }

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ArmorListener(), this);
        pm.registerEvents(new BeaconEffectListener(), this);
        pm.registerEvents(new DeathMessageListener(this), this);
        pm.registerEvents(new VehicleMoveListener(), this);
        pm.registerEvents(new WorldGuardInternalListener(), this);
        pm.registerEvents(new RaidListener(this), this);
        pm.registerEvents(new EggSpawnChickListener(), this);
        pm.registerEvents(new BlockListener(), this);
        pm.registerEvents(new CobwebPlaceListener(), this);
        pm.registerEvents(new ProjectileListener(), this);

        WorldGuard.getInstance().getPlatform().getSessionManager().registerHandler(new Handler.Factory<>() {
            @Override
            public Handler create(Session session) {
                return new ArmorCheckHandler(session);
            }
        }, null);
    }
}
