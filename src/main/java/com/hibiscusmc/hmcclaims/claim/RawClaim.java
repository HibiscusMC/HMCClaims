package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingHolder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.server.players.NameAndId;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A lightweight, memory-optimized representation of a database row for a claim.
 */
public record RawClaim(
        UUID claimId, UUID owner, String name, @Nullable UUID parentId, Set<RawClaim> subClaims,
        String worldName, int minX, int maxX, int minZ, int maxZ,
        byte[] rolesBytes, byte[] membersBytes, byte[] settingsBytes,
        boolean locked, Instant claimedTimestamp
) {

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawClaim that = (RawClaim) o;
        return claimId.equals(that.claimId);
    }

    @Override
    public int hashCode() {
        return claimId.hashCode();
    }

    /**
     * Inflates this raw database snapshot into a {@link Claim} instance.
     */
    public Claim inflate(@Nullable Claim mainClaim) {
        ClaimRegion region = new ClaimRegion(
                worldName, minX, maxX, minZ, maxZ
        );

        List<ClaimRole> roles = ClaimSerializer.deserializeRoles(rolesBytes);

        Claim claim = new Claim(claimId, name, mainClaim, new NameAndId(owner, ""), region, roles, claimedTimestamp);
        claim.locked(locked);

        Set<ClaimMember> members = ClaimSerializer.deserializeMembers(membersBytes, claim, claim.roleRegistry());
        members.forEach(claim::addMember);

        for (Map.Entry<Setting<?>, SettingHolder<?>> settingEntry : ClaimSerializer.deserializeSettings(settingsBytes).entrySet()) {
            claim.settings().put(settingEntry.getKey(), settingEntry.getValue());
        }

        List<Claim> inflatedSubClaims = new ArrayList<>();
        for (RawClaim subClaim : subClaims) {
            inflatedSubClaims.add(subClaim.inflate(claim));
        }

        inflatedSubClaims.forEach(claim::addSubClaim);

        return claim;
    }

    public record CacheHolder(
            Long2ObjectMap<Set<RawClaim>> chunks,
            Map<UUID, Set<RawClaim>> players
    ) {
    }
}