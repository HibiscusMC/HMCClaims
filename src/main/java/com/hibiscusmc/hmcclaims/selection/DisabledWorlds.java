package com.hibiscusmc.hmcclaims.selection;

import com.hibiscusmc.hmcclaims.config.Settings;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.jetbrains.annotations.NotNull;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Checks worlds against the {@code disabled-worlds} patterns of the config.
 */
@Singleton
public class DisabledWorlds {

    private final Object2BooleanMap<String> results
            = new Object2BooleanOpenHashMap<>();

    @Inject
    private ConfigHolder<Settings> settingsHolder;

    /**
     * Checks if the world matches any entry in the disabled worlds set. Results are cached until {@link #clear()}.
     *
     * @param world The name of the world to check.
     * @return True if the world matches a disabled pattern, false otherwise.
     */
    public boolean isDisabled(@NotNull String world) {
        synchronized (results) {
            if (results.containsKey(world)) {
                return results.getBoolean(world);
            }

            boolean disabled = matches(world);
            results.put(world, disabled);

            return disabled;
        }
    }

    public void clear() {
        synchronized (results) {
            results.clear();
        }
    }

    private boolean matches(String world) {
        Set<String> disabledWorlds = settingsHolder.get().disabledWorlds();
        if (disabledWorlds == null || disabledWorlds.isEmpty()) {
            return false;
        }

        for (String pattern : disabledWorlds) {
            String regex = "^" + Pattern.quote(pattern).replace("%", "\\E.*\\Q") + "$";

            if (world.matches(regex)) {
                return true;
            }
        }

        return false;
    }
}
