package com.hibiscusmc.hmcclaims.user;

import com.hibiscusmc.hmcclaims.selection.Selection;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a player's persistent and session-based data within the claim system.
 */
@Data
public class User {

    private final UUID uuid;
    private String lastKnownName;
    private long claimBlocks;

    @MonotonicNonNull
    private Instant lastOnline;

    /**
     * The number of blocks currently utilized by this user's claims.
     * <p>
     * This is a cached value managed by the {@code UserManager} and is not persisted.
     */
    @MonotonicNonNull
    private transient Long usedBlocks = null;

    /**
     * The user's current physical area selection, used for creating or resizing claims.
     */
    private transient Selection currentSelection;

    /**
     * Whether the user is currently bypassing claim restrictions (Staff mode).
     */
    private transient boolean bypassMode = false;

    /**
     * Timestamp of the last sent notification to prevent message spam.
     */
    private transient long lastNotificationSent = 0;

    /**
     * Constructs a new User with initial identity data.
     *
     * @param uuid The unique identifier of the player.
     * @param name The last known username of the player.
     */
    public User(UUID uuid, String name, long claimBlocks) {
        this.uuid = uuid;
        this.lastKnownName = name;
        this.claimBlocks = claimBlocks;
    }

    /**
     * Checks if the user currently has an active area selection.
     *
     * @return {@code true} if a selection exists.
     */
    public boolean hasActiveSelection() {
        return currentSelection != null;
    }

    /**
     * Determines if the user can receive a notification based on a 2-second cooldown.
     * <p>
     * <b>Side Effect:</b> If this returns {@code true}, the internal
     * {@code lastNotificationSent} timer is automatically updated to the current time.
     *
     * @return {@code true} if 2000ms have passed since the last notification.
     */
    public boolean canReceiveNotification() {
        long now = System.currentTimeMillis();
        if (now - lastNotificationSent > 2_000) {
            lastNotificationSent = now;
            return true;
        }

        return false;
    }
}