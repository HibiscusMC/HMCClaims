package com.hibiscusmc.hmcclaims.claim;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Singleton;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Claims that are stored but not loaded, indexed by world chunk and by owner.
 */
@Getter
@Singleton
public class UnloadedClaims {

    private final Map<String, Long2ObjectMap<Set<RawClaim>>> chunks
            = new ConcurrentHashMap<>();

    private final Map<UUID, Set<RawClaim>> players
            = new ConcurrentHashMap<>();

    public @NotNull Set<RawClaim> at(@NotNull String worldName, long chunkKey) {
        Long2ObjectMap<Set<RawClaim>> world = chunks.get(worldName);

        if (world == null) {
            return Set.of();
        }

        Set<RawClaim> claims = world.get(chunkKey);

        return claims == null ? Set.of() : claims;
    }
}