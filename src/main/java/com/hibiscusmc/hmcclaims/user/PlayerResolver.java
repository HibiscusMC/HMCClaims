package com.hibiscusmc.hmcclaims.user;

import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import net.minecraft.server.players.NameAndId;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Turns a typed player name into a {@link NameAndId} for players that have joined this server.
 * <p>
 * Resolution order:
 * <ol>
 *     <li>Online players.</li>
 *     <li>The server's user cache ({@code usercache.json}).</li>
 *     <li>The plugin's own user storage, matched by last known name.</li>
 * </ol>
 * The server's cache alone is not enough: entries expire a month after the player's last
 * join and only the 1,000 most recent players survive a save, so on a busy or long-running
 * server a real player regularly isn't in it. Every player that has joined has a row in
 * the plugin's storage, which is why it's the last resort.
 * <p>
 * No Mojang lookups are ever made, so a name that has never joined this server resolves
 * to nothing.
 */
@Singleton
public class PlayerResolver {

    @Inject
    private StorageHolder storageHolder;

    @Inject
    private SchedulerUtil scheduler;

    /**
     * Resolves a name, delivering the result on the main thread.
     *
     * @param name     The typed name.
     * @param callback Receives the resolved player, or {@code null} if there is no such player.
     */
    public void resolve(@NotNull String name, @NotNull Consumer<@Nullable NameAndId> callback) {
        NameAndId known = resolveKnown(name);
        if (known != null) {
            callback.accept(known);
            return;
        }

        resolveStored(name).whenComplete((resolved, throwable) ->
                scheduler.schedule(() -> callback.accept(throwable == null ? resolved : null))
        );
    }

    /**
     * Resolves a name.
     *
     * @param name The typed name.
     * @return A future completing with the resolved player, or with {@code null} if there is
     * no such player. It may complete on a storage thread.
     */
    @NotNull
    public CompletableFuture<@Nullable NameAndId> resolve(@NotNull String name) {
        NameAndId known = resolveKnown(name);
        if (known != null) {
            return CompletableFuture.completedFuture(known);
        }

        return resolveStored(name);
    }

    /**
     * Checks the sources that answer immediately: online players and the server's user cache.
     */
    @Nullable
    private NameAndId resolveKnown(@NotNull String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return new NameAndId(online.getUniqueId(), online.getName());
        }

        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(name);
        if (cached != null && cached.getName() != null && cached.hasPlayedBefore()) {
            return new NameAndId(cached.getUniqueId(), cached.getName());
        }

        return null;
    }

    /**
     * Checks the plugin's user storage by last known name.
     */
    @NotNull
    private CompletableFuture<@Nullable NameAndId> resolveStored(@NotNull String name) {
        return storageHolder.get().users().getUserByName(name)
                .thenApply(user -> user == null ? null : new NameAndId(user.uuid(), user.lastKnownName()));
    }
}