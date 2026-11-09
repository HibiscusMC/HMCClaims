package com.hibiscusmc.hmcclaims.listener;

import com.hibiscusmc.hmcclaims.api.event.ClaimAccessChangeEvent;
import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimEjector;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.UUID;

@Singleton
public class ClaimAccessListener implements Listener {

    @Inject
    private ClaimEjector ejector;

    @Inject
    private ClaimManager claimManager;

    @Inject
    private SchedulerUtil scheduler;

    @EventHandler
    public void onAccessChange(ClaimAccessChangeEvent event) {
        Claim claim = event.claim();
        UUID memberId = event.memberId();

        Runnable eject = () -> {
            if (memberId == null) {
                ejector.ejectDenied(claim);
                return;
            }

            Player player = Bukkit.getPlayer(memberId);

            if (player != null) {
                ejector.ejectIfDenied(player, claim);
            }
        };

        if (Bukkit.isPrimaryThread()) {
            eject.run();
            return;
        }

        scheduler.schedule(eject);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        for (Claim claim : claimManager.getClaimsAt(player.getLocation())) {
            if (ejector.ejectIfDenied(player, claim)) {
                return;
            }
        }
    }
}
