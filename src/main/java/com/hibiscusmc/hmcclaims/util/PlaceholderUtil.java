package com.hibiscusmc.hmcclaims.util;

import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

/**
 * Central utility for generating and managing placeholder data maps.
 * <p>
 * This class serves as a provider for various plugin-related statistics and
 * information, converting complex domain objects into string-keyed maps for
 * use in messages, GUIs, and third-party integrations.
 */
@Singleton
public class PlaceholderUtil {

    @Inject
    private UserManager userManager;

    @Inject
    private ConfigHolder<Settings> settingsHolder;

    /**
     * Generates a placeholder map containing claim block statistics for a specific user.
     * <p>
     * <b>Available Placeholders:</b>
     * <ul>
     * <li>{@code starting_blocks}: The base blocks granted by configuration.</li>
     * <li>{@code obtained_blocks}: Blocks earned or purchased by the user.</li>
     * <li>{@code total_blocks}: The sum of starting and obtained blocks.</li>
     * <li>{@code used_blocks}: Total blocks currently utilized in claims.</li>
     * <li>{@code available_blocks}: Remaining blocks the user can spend.</li>
     * </ul>
     *
     * @param user The {@link User} to process; if {@code null}, returns default
     *             starting values with zeroed user statistics.
     * @return A newly constructed, mutable {@link Map} of claim block placeholders.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public Map<String, String> claimBlocks(@Nullable User user) {
        long startingBlocks = settingsHolder.get().claimBlocks().startingAmount();
        long obtainedBlocks = user != null ? user.claimBlocks() : 0;
        long totalBlocks = startingBlocks + obtainedBlocks;
        long availableBlocks = userManager.getRemainingBlocks(user);
        long usedBlocks = totalBlocks - availableBlocks;

        Map<String, String> map = new HashMap<>();
        map.put("starting_blocks", startingBlocks + "");
        map.put("obtained_blocks", obtainedBlocks + "");
        map.put("total_blocks", totalBlocks + "");
        map.put("used_blocks", usedBlocks + "");
        map.put("available_blocks", availableBlocks + "");

        return map;
    }
}