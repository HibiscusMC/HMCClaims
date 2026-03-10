package com.hibiscusmc.hmcclaims.input.type;

import com.hibiscusmc.hmcclaims.input.Input;
import com.hibiscusmc.hmcclaims.input.InputErrorReason;
import net.minecraft.server.players.NameAndId;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class PlayerInput implements Input<NameAndId> {

    private Consumer<NameAndId> onSubmit;

    private Runnable onCancel;

    @Override
    public @Nullable InputErrorReason submit(String raw) {
        OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(raw);
        if (player == null || player.getName() == null || !player.hasPlayedBefore()) {
            return InputErrorReason.PLAYER_NOT_FOUND;
        }

        onSubmit.accept(new NameAndId(player.getUniqueId(), player.getName()));
        return null;
    }

    @Override
    public void cancel() {
        onCancel.run();
    }

    @Override
    public PlayerInput onSubmit(Consumer<NameAndId> onSubmit) {
        this.onSubmit = onSubmit;
        return this;
    }

    @Override
    public PlayerInput onCancel(Runnable onCancel) {
        this.onCancel = onCancel;
        return this;
    }

}