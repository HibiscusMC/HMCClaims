package com.hibiscusmc.hmcclaims.task;

import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.selection.SelectionManager;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Keeps a persistent title (and optionally an action bar) on screen for every
 * player currently in resize mode, reminding them of the available controls.
 */
@RequiredArgsConstructor
public class ResizeDisplayTask extends BukkitRunnable {

    private final SelectionManager manager;
    private final ConfigHolder<Messages> holder;

    public static ResizeDisplayTask start(Plugin plugin, SelectionManager manager, ConfigHolder<Messages> holder) {
        ResizeDisplayTask task = new ResizeDisplayTask(manager, holder);
        task.runTaskTimerAsynchronously(plugin, 0L, 20L);

        return task;
    }

    @Override
    public void run() {
        Audience audience = manager.resizingAudiences();
        Messages.Claims.Resizing messages = holder.get().claims().resizing();

        if (messages.title().enabled()) {
            audience.showTitle(Title.title(
                    TextUtil.parse(messages.title().title()),
                    TextUtil.parse(messages.title().subtitle()),
                    0,
                    21,
                    20
            ));
        }

        if (messages.actionBar().enabled()) {
            audience.sendActionBar(TextUtil.parse(messages.actionBar().text()));
        }
    }

    public void clear(Audience audience) {
        audience.clearTitle();
        audience.sendActionBar(Component.empty());
    }
}
