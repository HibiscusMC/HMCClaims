package com.hibiscusmc.hmcclaims.task;

import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.input.InputManager;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class InputDisplayTask extends BukkitRunnable {

    private final InputManager manager;
    private final ConfigHolder<Messages> holder;

    public static InputDisplayTask start(Plugin plugin, InputManager manager, ConfigHolder<Messages> holder) {
        InputDisplayTask task = new InputDisplayTask(manager, holder);
        task.runTaskTimerAsynchronously(plugin, 0L, 20L);

        return task;
    }

    @Override
    public void run() {
        Audience audience = manager.audiences();
        Messages.Inputs messages = holder.get().inputs();

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