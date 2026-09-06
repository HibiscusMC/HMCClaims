package com.hibiscusmc.hmcclaims.form.input;

import com.hibiscusmc.hmcclaims.config.form.DialogsFormConfig;
import com.hibiscusmc.hmcclaims.config.form.FormTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.form.FormService;
import com.hibiscusmc.hmcclaims.form.FormText;
import com.hibiscusmc.hmcclaims.form.spec.CustomFormSpec;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import net.minecraft.server.players.NameAndId;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Bedrock replacements for the plugin's text-entry flows.
 */
@Singleton
public class FormInputs {

    /**
     * The key the single text field is submitted under.
     */
    private final static String INPUT_KEY = "input";

    @Inject
    private ConfigHolder<DialogsFormConfig> configHolder;

    @Inject
    private FormService forms;

    @Inject
    private TextUtil text;

    /**
     * @return The loaded {@code forms/dialogs.yml}.
     */
    @NotNull
    public DialogsFormConfig config() {
        DialogsFormConfig config = configHolder.get();
        if (config == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        return config;
    }

    /**
     * Shows a one-field form and reports what the player typed.
     *
     * @param player       The Bedrock player.
     * @param dialog       The configured form.
     * @param placeholders Placeholders available to the title, body and field.
     * @param initialValue Pre-fills the field; pass an empty string to leave it blank.
     * @param onSubmit     Receives the trimmed, non-empty submission.
     * @param onCancel     Runs when the player dismisses the form.
     */
    public void prompt(
            @NotNull Player player, @NotNull DialogsFormConfig.SingleInput dialog,
            @NotNull Map<String, ?> placeholders, @NotNull String initialValue,
            @NotNull Consumer<String> onSubmit, @NotNull Runnable onCancel
    ) {
        FormTemplate.Input input = dialog.input();

        CustomFormSpec.Builder builder = CustomFormSpec.builder()
                .title(FormText.line(dialog.title(), player, placeholders));

        String content = FormText.block(dialog.content(), player, placeholders);
        if (!content.isEmpty()) {
            builder.label(content);
        }

        builder.input(
                INPUT_KEY,
                FormText.line(input.label(), player, placeholders),
                FormText.line(input.placeholder(), player, placeholders),
                initialValue
        );

        builder.onSubmit(values -> {
            String submitted = values.input(INPUT_KEY);

            if (submitted == null || submitted.isBlank()) {
                text.send(player, config().emptyInput());
                onCancel.run();
                return;
            }

            onSubmit.accept(trim(submitted.trim(), input.maxLength()));
        });

        builder.onClose(onCancel);

        forms.send(player, builder.build());
    }

    /**
     * Shows a one-field form asking for a player name and resolves it the same way the
     * chat prompt does.
     *
     * @param player       The Bedrock player.
     * @param dialog       The configured form.
     * @param placeholders Placeholders available to the title, body and field.
     * @param onSubmit     Receives the resolved player.
     * @param onCancel     Runs when the player dismisses the form.
     */
    public void promptPlayer(
            @NotNull Player player, @NotNull DialogsFormConfig.SingleInput dialog,
            @NotNull Map<String, ?> placeholders,
            @NotNull Consumer<NameAndId> onSubmit, @NotNull Runnable onCancel
    ) {
        prompt(player, dialog, placeholders, "", submitted -> {
            NameAndId resolved = resolve(submitted);

            if (resolved == null) {
                text.send(player, config().playerNotFound());
                onCancel.run();
                return;
            }

            onSubmit.accept(resolved);
        }, onCancel);
    }

    /**
     * Resolves a typed name into a known player.
     * <p>
     * Matches {@link com.hibiscusmc.hmcclaims.input.type.PlayerInput}: the player must
     * have joined this server before.
     *
     * @param name The typed name.
     * @return The resolved player, or {@code null} when there is no such player.
     */
    @Nullable
    private NameAndId resolve(@NotNull String name) {
        OfflinePlayer offline = Bukkit.getOfflinePlayerIfCached(name);

        if (offline == null || offline.getName() == null || !offline.hasPlayedBefore()) {
            return null;
        }

        return new NameAndId(offline.getUniqueId(), offline.getName());
    }

    /**
     * Bedrock does not enforce a length limit on a form field, so submissions are trimmed
     * server-side instead.
     *
     * @param value     The submitted text.
     * @param maxLength The configured limit, or a negative number to keep it as-is.
     * @return The possibly shortened text.
     */
    @NotNull
    private String trim(@NotNull String value, int maxLength) {
        if (maxLength < 0 || value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }
}