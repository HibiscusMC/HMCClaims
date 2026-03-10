package com.hibiscusmc.hmcclaims.dialog.type;

import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.dialog.Dialog;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.ClickCallback;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@SuppressWarnings({"UnstableApiUsage"})
public class SearchDialog implements Dialog {

    private io.papermc.paper.dialog.Dialog dialog;

    private Consumer<DialogResponseView> onSubmit;
    private Runnable onCancel;

    private final List<String> options;

    public SearchDialog() {
        options = new ArrayList<>();
    }

    public SearchDialog(List<String> options) {
        this.options = options;
    }

    @Override
    public Dialog create(Messages.Dialogs messages) {
        List<DialogInput> inputs = new ArrayList<>();
        inputs.add(DialogInput
                .text("query", TextUtil.parse(messages.search().query()))
                .maxLength(48)
                .build());

        if (!options.isEmpty()) {
            AtomicBoolean first = new AtomicBoolean(true);
            inputs.add(DialogInput
                    .singleOption("option", TextUtil.parse(messages.search().optionTitle()), options.stream().map(option ->
                            SingleOptionDialogInput.OptionEntry.create(option, TextUtil.parse(messages.search().options().get(option)), first.getAndSet(false))
                    ).toList())
                    .build());
        }

        this.dialog = io.papermc.paper.dialog.Dialog.create(builder -> builder.empty()
                .base(DialogBase
                        .builder(TextUtil.parse(messages.search().title()))
                        .canCloseWithEscape(true)
                        .inputs(inputs)
                        .build()
                )
                .type(DialogType.confirmation(
                        ActionButton.create(
                                TextUtil.parse(messages.search().buttons().get("submit").label()),
                                TextUtil.parse(messages.search().buttons().get("submit").tooltip()),
                                100,
                                DialogAction.customClick(
                                        (view, audience) -> {
                                            if (this.onSubmit != null) {
                                                this.onSubmit.accept(view);
                                            }
                                        },
                                        ClickCallback.Options.builder()
                                                .uses(1)
                                                .lifetime(Duration.of(1, ChronoUnit.HOURS))
                                                .build()
                                )
                        ),
                        ActionButton.create(
                                TextUtil.parse(messages.search().buttons().get("cancel").label()),
                                TextUtil.parse(messages.search().buttons().get("cancel").tooltip()),
                                100,
                                DialogAction.customClick(
                                        (view, audience) -> {
                                            if (this.onCancel != null) {
                                                this.onCancel.run();
                                            }
                                        },
                                        ClickCallback.Options.builder()
                                                .uses(1)
                                                .lifetime(Duration.of(1, ChronoUnit.HOURS))
                                                .build()
                                )
                        )
                ))
        );

        return this;
    }

    @Override
    public void show(Audience audience) {
        if (dialog == null) {
            throw new NullPointerException("Trying to show search dialog before creating it.");
        }

        audience.showDialog(dialog);
    }

    @Override
    public Dialog onSubmit(Consumer<DialogResponseView> onSubmit) {
        this.onSubmit = onSubmit;
        return this;
    }

    @Override
    public Dialog onCancel(Runnable onCancel) {
        this.onCancel = onCancel;
        return this;
    }
}