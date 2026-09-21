package net.okocraft.moreflags.listener;

import org.bukkit.entity.WindCharge;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ProjectileListenerTest {

    @Test
    void testLaunchFromNonPlayerShooterIsIgnored() {
        ProjectileListener listener = new ProjectileListener();
        ProjectileLaunchEvent event = Mockito.mock(ProjectileLaunchEvent.class);
        WindCharge windCharge = Mockito.mock(WindCharge.class);
        Mockito.when(event.getEntity()).thenReturn(windCharge);
        Mockito.when(windCharge.getShooter()).thenReturn(null);

        listener.onLaunch(event);

        Mockito.verify(event, Mockito.never()).setCancelled(Mockito.anyBoolean());
    }

    @Test
    void testHitFromNonPlayerShooterIsIgnored() {
        ProjectileListener listener = new ProjectileListener();
        ProjectileHitEvent event = Mockito.mock(ProjectileHitEvent.class);
        WindCharge windCharge = Mockito.mock(WindCharge.class);
        Mockito.when(event.getEntity()).thenReturn(windCharge);
        Mockito.when(windCharge.getShooter()).thenReturn(null);

        listener.onHit(event);

        Mockito.verify(event, Mockito.never()).setCancelled(Mockito.anyBoolean());
        Mockito.verify(windCharge, Mockito.never()).remove();
    }
}
