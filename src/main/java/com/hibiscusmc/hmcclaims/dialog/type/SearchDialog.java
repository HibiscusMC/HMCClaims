package com.hibiscusmc.hmcclaims.dialog.type;

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
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings({"UnstableApiUsage"})
public class SearchDialog implements Dialog {

    private io.papermc.paper.dialog.Dialog dialog;

    private Consumer<DialogResponseView> onSubmit;
    private Runnable onCancel;

    @Override
    public Dialog create() {
        this.dialog = io.papermc.paper.dialog.Dialog.create(builder -> builder.empty()
                .base(DialogBase
                        .builder(TextUtil.parse("Search"))
                        .canCloseWithEscape(true)
                        .inputs(List.of(
                                DialogInput
                                        .text("query", TextUtil.parse("Query"))
                                        .maxLength(48)
                                        .build(),
                                DialogInput
                                        .singleOption("option", TextUtil.parse("Search by"), List.of(
                                                SingleOptionDialogInput.OptionEntry.create("name", TextUtil.parse("Claim Name"), true),
                                                SingleOptionDialogInput.OptionEntry.create("id", TextUtil.parse("Claim Id"), false),
                                                SingleOptionDialogInput.OptionEntry.create("main", TextUtil.parse("Main Claim Name"), false),
                                                SingleOptionDialogInput.OptionEntry.create("member", TextUtil.parse("Member Name"), false)
                                        ))
                                        .build()
                        ))
                        .build()
                )
                .type(DialogType.confirmation(
                        ActionButton.create(
                                TextUtil.parse("Search"),
                                TextUtil.parse("Click to search!"),
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
                                TextUtil.parse("<red>Cancel"),
                                TextUtil.parse("Click to cancel"),
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