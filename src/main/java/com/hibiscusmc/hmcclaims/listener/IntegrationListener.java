package com.hibiscusmc.hmcclaims.listener;

import com.hibiscusmc.hmcclaims.config.gui.ClaimListConfig;
import com.hibiscusmc.hmcclaims.config.gui.ClaimMemberListConfig;
import com.hibiscusmc.hmcclaims.config.internal.ConfigFactory;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import lombok.extern.java.Log;
import me.lojosho.hibiscuscommons.api.events.HibiscusHooksAllActiveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import team.unnamed.inject.Inject;

/**
 * Handles external plugin integrations and the synchronization of
 * configuration-dependent resources like GUIs.
 */
@Log(topic = "HMCClaims")
public class IntegrationListener implements Listener {

    @Inject
    private GuiRegistry guis;

    @Inject
    private SchedulerUtil scheduler;

    @EventHandler
    public void onItemsLoad(HibiscusHooksAllActiveEvent event) {
        scheduler.schedule(() -> {
            try {
                ConfigFactory.reload(ClaimListConfig.class);
                ConfigFactory.reload(ClaimMemberListConfig.class);
            } catch (Exception ex) {
                log.severe("Failed to reload GUI configs");
                ex.printStackTrace();
            }

            guis.load();
        });
    }
}