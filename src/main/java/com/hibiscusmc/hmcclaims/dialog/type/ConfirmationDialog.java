package com.hibiscusmc.hmcclaims.dialog.type;

import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.dialog.Dialog;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickCallback;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class ConfirmationDialog implements Dialog {

    private io.papermc.paper.dialog.Dialog dialog;

    private Runnable onSubmit;
    private Runnable onCancel;

    public Dialog create(Messages.Dialogs.Confirm messages, Map<String, String> titlePlaceholders, List<String> body) {
        return create(messages.title(), messages.buttons(), titlePlaceholders, body);
    }

    public Dialog create(String title, Map<String, Messages.Dialogs.Button> buttons, Map<String, String> titlePlaceholders, List<String> body) {
        Component text = Component.join(
                JoinConfiguration.newlines(),
                body.stream().map(line -> TextUtil.parse(line, titlePlaceholders)).toList()
        );

        this.dialog = io.papermc.paper.dialog.Dialog.create(builder -> builder.empty()
                .base(DialogBase
                        .builder(TextUtil.parse(title, titlePlaceholders))
                        .canCloseWithEscape(true)
                        .body(List.of(DialogBody.plainMessage(text)))
                        .build()
                )
                .type(DialogType.confirmation(
                        ActionButton.create(
                                TextUtil.parse(buttons.get("confirm").label()),
                                TextUtil.parse(buttons.get("confirm").tooltip()),
                                100,
                                DialogAction.customClick(
                                        (view, audience) -> {
                                            if (this.onSubmit != null) {
                                                this.onSubmit.run();
                                            }
                                        },
                                        ClickCallback.Options.builder()
                                                .uses(1)
                                                .lifetime(Duration.of(1, ChronoUnit.HOURS))
                                                .build()
                                )
                        ),
                        ActionButton.create(
                                TextUtil.parse(buttons.get("cancel").label()),
                                TextUtil.parse(buttons.get("cancel").tooltip()),
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
            throw new NullPointerException("Trying to show confirmation dialog before creating it.");
        }

        audience.showDialog(dialog);
    }

    @Override
    public Dialog onSubmit(Runnable onSubmit) {
        this.onSubmit = onSubmit;
        return this;
    }

    @Override
    public Dialog onCancel(Runnable onCancel) {
        this.onCancel = onCancel;
        return this;
    }
}
