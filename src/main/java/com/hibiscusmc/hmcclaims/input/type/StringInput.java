package com.hibiscusmc.hmcclaims.input.type;

import com.hibiscusmc.hmcclaims.input.Input;
import com.hibiscusmc.hmcclaims.input.InputErrorReason;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class StringInput implements Input<String> {

    private Consumer<String> onSubmit;

    private Runnable onCancel;

    @Override
    public @Nullable InputErrorReason submit(String raw) {
        onSubmit.accept(raw);

        return null;
    }

    @Override
    public void cancel() {
        onCancel.run();
    }

    @Override
    public StringInput onSubmit(Consumer<String> onSubmit) {
        this.onSubmit = onSubmit;
        return this;
    }

    @Override
    public StringInput onCancel(Runnable onCancel) {
        this.onCancel = onCancel;
        return this;
    }

}