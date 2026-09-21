package net.okocraft.moreflags;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import net.okocraft.moreflags.handler.ArmorCheckHandler;
import net.okocraft.moreflags.handler.TeleportToSpawnFlagHandler;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.function.Function;

public class Main extends JavaPlugin {

    @Override
    public void onLoad() {
        CustomFlags.init();
    }

    @Override
    public void onEnable() {
        List.of(
                new ArmorListener(),
                new BeaconEffectListener(),
                new DeathMessageListener(this),
                new VehicleMoveListener(),
                new WorldGuardInternalListener(),
                new RaidListener(this),
                new EggSpawnChickListener(),
                new BlockListener(),
                new CobwebPlaceListener(),
                new ProjectileListener()
        ).forEach(l -> this.getServer().getPluginManager().registerEvents(l, this));

        List.<Function<Session, Handler>>of(
                ArmorCheckHandler::new,
                TeleportToSpawnFlagHandler::new
        ).forEach(f -> WorldGuard.getInstance().getPlatform().getSessionManager().registerHandler(new Handler.Factory<Handler>() {
            @Override
            public Handler create(Session session) {
                return f.apply(session);
            }
        }, null));
    }
}
