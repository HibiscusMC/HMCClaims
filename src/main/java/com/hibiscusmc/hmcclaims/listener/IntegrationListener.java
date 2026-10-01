package com.hibiscusmc.hmcclaims.listener;

import com.hibiscusmc.hmcclaims.form.FormRegistry;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import me.lojosho.hibiscuscommons.api.events.HibiscusHooksAllActiveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import team.unnamed.inject.Inject;

/**
 * Handles external plugin integrations and the synchronization of
 * configuration-dependent resources like GUIs.
 */
public class IntegrationListener implements Listener {

    @Inject
    private GuiRegistry guis;

    @Inject
    private FormRegistry forms;

    @Inject
    private SchedulerUtil scheduler;

    @EventHandler
    public void onItemsLoad(HibiscusHooksAllActiveEvent event) {
        scheduler.schedule(() -> {
            guis.load();
            forms.load();
        });
    }
}