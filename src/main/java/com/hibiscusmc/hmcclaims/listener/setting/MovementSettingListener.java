package com.hibiscusmc.hmcclaims.listener.setting;

import com.hibiscusmc.hmcclaims.api.event.ClaimEnterEvent;
import com.hibiscusmc.hmcclaims.api.event.ClaimLeaveEvent;
import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingHolder;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.EventUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;

import java.util.List;
import java.util.Map;

/**
 * Join & Leave message
 */
public class MovementSettingListener implements Listener {

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private ClaimManager claimManager;
    @Inject
    private UserManager userManager;

    @Inject
    private TextUtil text;

    @EventHandler(priority = EventPriority.MONITOR)
    private void onClaimEnterOrLeave(PlayerMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        User user = userManager.getUser(player.getUniqueId())
                .orElse(null);
        if (user == null) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Claim userClaim = user.currentClaim();
        Claim toClaim = claimManager.getClaimAt(to).orElse(null);

        if ((userClaim == null && toClaim == null) || (userClaim != null && userClaim.equals(toClaim))) {
            return;
        }

        List<Claim> oldChain = chain(userClaim);
        List<Claim> newChain = chain(toClaim);

        for (Claim left : oldChain.reversed()) {
            if (newChain.contains(left)) {
                continue;
            }

            EventUtil.call(new ClaimLeaveEvent(player, left));
            sendLeaveMessage(player, left);
        }

        for (Claim entered : newChain) {
            if (oldChain.contains(entered)) {
                continue;
            }

            EventUtil.call(new ClaimEnterEvent(player, entered));
            sendJoinMessage(player, entered);
        }

        user.currentClaim(toClaim);
    }

    private List<Claim> chain(@Nullable Claim claim) {
        if (claim == null) {
            return List.of();
        }

        if (claim.main() == null) {
            return List.of(claim);
        }

        return List.of(claim.main(), claim);
    }

    private void sendJoinMessage(@NotNull Player player, @NotNull Claim claim) {
        SettingHolder<String> setting = (SettingHolder<String>) claim.settings().get(Setting.JOIN_MESSAGE);

        if (setting == null || setting.value() == null || setting.value().equals("null")) {
            return;
        }

        text.send(player, messagesHolder.get().claims().settings().joinMessage(), Map.of(
                "claim_name", claim.name(),
                "message", parseText(player, setting.value())
        ));
    }

    private void sendLeaveMessage(@NotNull Player player, @NotNull Claim claim) {
        SettingHolder<String> setting = (SettingHolder<String>) claim.settings().get(Setting.LEAVE_MESSAGE);

        if (setting == null || setting.value() == null || setting.value().equals("null")) {
            return;
        }

        text.send(player, messagesHolder.get().claims().settings().leaveMessage(), Map.of(
                "claim_name", claim.name(),
                "message", parseText(player, setting.value())
        ));
    }

    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    private Component parseText(@NotNull Player player, @NotNull String message) {
        return TextUtil.safe(
                message.replace("$PLAYER", player.getName())
        );
    }
}