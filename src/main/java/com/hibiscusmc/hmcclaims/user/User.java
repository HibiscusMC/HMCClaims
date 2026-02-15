package com.hibiscusmc.hmcclaims.user;

import com.hibiscusmc.hmcclaims.selection.Selection;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class User {

    private final UUID uuid;
    private String lastKnownName;
    private long claimBlocks;
    private Instant lastOnline;

    private transient Selection currentSelection;
    private transient boolean bypassMode = false;
    private transient long lastNotificationSent = 0;

    public User(UUID uuid, String name) {
        this.uuid = uuid;
        this.lastKnownName = name;
    }

    public boolean hasActiveSelection() {
        return currentSelection != null;
    }

    public boolean canReceiveNotification() {
        long now = System.currentTimeMillis();
        if (now - lastNotificationSent > 2000) {
            lastNotificationSent = now;
            return true;
        }

        return false;
    }

}