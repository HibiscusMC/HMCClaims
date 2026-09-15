package com.hibiscusmc.hmcclaims.input.type;

import com.hibiscusmc.hmcclaims.input.Input;
import com.hibiscusmc.hmcclaims.input.InputErrorReason;
import com.hibiscusmc.hmcclaims.user.PlayerResolver;
import net.minecraft.server.players.NameAndId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PlayerInput implements Input<NameAndId> {

    private final PlayerResolver resolver;

    private Consumer<NameAndId> onSubmit;

    private Runnable onCancel;

    public PlayerInput(@NotNull PlayerResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public CompletableFuture<@Nullable InputErrorReason> submit(String raw) {
        CompletableFuture<InputErrorReason> result = new CompletableFuture<>();

        // The resolver hands the result back on the main thread, so the callback (and with
        // it the completion of this future) never runs on a storage thread
        resolver.resolve(raw, player -> {
            if (player == null) {
                result.complete(InputErrorReason.PLAYER_NOT_FOUND);
                return;
            }

            onSubmit.accept(player);
            result.complete(null);
        });

        return result;
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