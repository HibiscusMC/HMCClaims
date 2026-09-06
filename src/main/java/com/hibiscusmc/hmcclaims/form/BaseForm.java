package com.hibiscusmc.hmcclaims.form;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.config.form.FormTemplate;
import com.hibiscusmc.hmcclaims.form.impl.ClaimManageForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimMemberListForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimRolesForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimSettingsForm;
import com.hibiscusmc.hmcclaims.form.impl.SubClaimManageForm;
import com.hibiscusmc.hmcclaims.form.spec.FormButton;
import com.hibiscusmc.hmcclaims.form.spec.FormComponent;
import com.hibiscusmc.hmcclaims.form.spec.FormImage;
import com.hibiscusmc.hmcclaims.form.spec.SimpleFormSpec;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The core interface for all Bedrock forms within the plugin.
 *
 * @see com.hibiscusmc.hmcclaims.gui.BaseGui
 */
public interface BaseForm {

    /**
     * Prefix marking an {@code order} entry as one of the configured extra buttons.
     */
    String EXTRA_PREFIX = "extra:";

    /**
     * Key the trailing navigation dropdown submits its selection under.
     */
    String NAVIGATION_KEY = "hmcclaims:navigation";

    /**
     * Instructs the form to (re)load its text, buttons and layout from configuration.
     */
    void loadConfig();

    /**
     * Sends the standard version of this form to the player.
     *
     * @param player The Bedrock player who will see the form.
     */
    default void send(@NotNull Player player) {
    }

    /**
     * Sends a context-specific version of this form to the player.
     *
     * @param player   The Bedrock player who will see the form.
     * @param metadata Contextual metadata required to render the form.
     */
    default void send(@NotNull Player player, @NotNull GuiMetadata metadata) {
    }

    /**
     * Renders an {@code order} entry that every form understands.
     *
     * @param section The entry from the {@code order} list.
     * @param builder The form being assembled.
     * @param context Everything the shared sections need to render.
     * @return {@code true} if the entry was recognised and handled.
     */
    default boolean renderShared(@NotNull String section, @NotNull SimpleFormSpec.Builder builder, @NotNull SharedContext context) {
        if (section.startsWith(EXTRA_PREFIX)) {
            addExtra(builder, section.substring(EXTRA_PREFIX.length()), context);
            return true;
        }

        return switch (section) {
            case "nav" -> {
                addNavigation(builder, context);
                yield true;
            }

            case "back" -> {
                addBack(builder, context);
                yield true;
            }

            default -> false;
        };
    }

    /**
     * Adds one of the configured extra buttons, if it exists.
     */
    private void addExtra(@NotNull SimpleFormSpec.Builder builder, @NotNull String key, @NotNull SharedContext context) {
        FormTemplate.ActionButton button = context.extraButtons().get(key);
        if (button == null) {
            return;
        }

        Player player = context.player();
        List<Action> actions = button.actions();

        builder.button(
                FormText.line(button.text(), player),
                context.image(button.image(), null),
                () -> actions.forEach(action -> action.execute(player))
        );
    }

    /**
     * Adds the tab row, skipping the destination the player is already on.
     */
    private void addNavigation(@NotNull SimpleFormSpec.Builder builder, @NotNull SharedContext context) {
        FormTemplate.Navigation nav = context.navigation();
        if (nav == null) {
            return;
        }

        for (Map.Entry<Class<? extends BaseForm>, FormTemplate.Button> entry : navigationTargets(nav, context).entrySet()) {
            Class<? extends BaseForm> target = entry.getKey();
            FormTemplate.Button button = entry.getValue();

            builder.button(
                    FormText.line(button.text(), context.player()),
                    context.image(button.image(), null),
                    () -> context.navigateTo(target)
            );
        }
    }

    /**
     * Adds the Back button, but only when there is somewhere to go back to.
     */
    private void addBack(@NotNull SimpleFormSpec.Builder builder, @NotNull SharedContext context) {
        FormTemplate.Button back = context.backButton();
        if (back == null || !context.forms().hasHistory(context.player())) {
            return;
        }

        builder.button(
                FormText.line(back.text(), context.player()),
                context.image(back.image(), null),
                () -> context.forms().back(context.player())
        );
    }

    /**
     * Resolves the navigation row into concrete form classes, in config order and minus
     * the current screen.
     */
    private Map<Class<? extends BaseForm>, FormTemplate.Button> navigationTargets(
            @NotNull FormTemplate.Navigation nav, @NotNull SharedContext context
    ) {
        Map<Class<? extends BaseForm>, FormTemplate.Button> targets = new LinkedHashMap<>();
        Claim claim = context.claim();

        for (String key : List.of("members", "roles", "settings", "manage")) {
            FormTemplate.Button button = nav.buttons().get(key);
            if (button == null) {
                continue;
            }

            Class<? extends BaseForm> target = switch (key) {
                case "members" -> ClaimMemberListForm.class;
                case "roles" -> ClaimRolesForm.class;
                case "settings" -> ClaimSettingsForm.class;
                case "manage" -> claim != null && claim.main() != null
                        ? SubClaimManageForm.class
                        : ClaimManageForm.class;
                default -> null;
            };

            if (target == null || target.equals(context.current())) {
                continue;
            }

            targets.put(target, button);
        }

        return targets;
    }

    /**
     * Builds the trailing navigation dropdown used by the forms that cannot hold buttons.
     *
     * @param context The rendering context.
     * @return The dropdown, or {@code null} when there is nowhere to navigate.
     */
    @Nullable
    default FormComponent.Dropdown navigationDropdown(@NotNull SharedContext context) {
        FormTemplate.Navigation nav = context.navigation();
        if (nav == null) {
            return null;
        }

        Map<Class<? extends BaseForm>, FormTemplate.Button> targets = navigationTargets(nav, context);
        if (targets.isEmpty()) {
            return null;
        }

        List<String> options = new ArrayList<>();
        options.add(FormText.line(nav.dropdownStay(), context.player()));

        for (FormTemplate.Button button : targets.values()) {
            options.add(FormText.line(button.text(), context.player()));
        }

        return new FormComponent.Dropdown(
                NAVIGATION_KEY,
                FormText.line(nav.dropdownLabel(), context.player()),
                options,
                0
        );
    }

    /**
     * Resolves a navigation dropdown selection back into the form it points at.
     *
     * @param selected The submitted index; index zero means "stay here".
     * @param context  The rendering context the dropdown was built with.
     * @return The form to open, or {@code null} to stay put.
     */
    @Nullable
    default Class<? extends BaseForm> navigationTarget(int selected, @NotNull SharedContext context) {
        FormTemplate.Navigation nav = context.navigation();
        if (nav == null || selected <= 0) {
            return null;
        }

        List<Class<? extends BaseForm>> targets = new ArrayList<>(navigationTargets(nav, context).keySet());
        int index = selected - 1;

        return index < targets.size() ? targets.get(index) : null;
    }

    /**
     * Resolves a button icon outside of a {@link SharedContext}, such as on the small
     * action forms opened from a list row.
     *
     * @param image The image block from the config.
     * @param forms The form service, consulted for the material fallback setting.
     * @return The icon, or {@code null} for a text-only button.
     */
    @Nullable
    default FormImage image(@Nullable FormTemplate.Image image, @NotNull FormService forms) {
        return FormImages.resolve(image, null, forms.materialImageFallback());
    }

    /**
     * Orders an action-button map so the rendered layout doesn't depend on hash ordering.
     *
     * @param buttons   The configured buttons.
     * @param preferred The keys to place first.
     * @return The buttons in display order.
     */
    @NotNull
    default Map<String, FormTemplate.Button> ordered(
            @NotNull Map<String, FormTemplate.Button> buttons, @NotNull String... preferred
    ) {
        Map<String, FormTemplate.Button> ordered = new LinkedHashMap<>();

        for (String key : preferred) {
            FormTemplate.Button button = buttons.get(key);

            if (button != null) {
                ordered.put(key, button);
            }
        }

        buttons.forEach((key, button) -> {
            if (!ordered.containsKey(key) && !key.equals("back")) {
                ordered.put(key, button);
            }
        });

        FormTemplate.Button back = buttons.get("back");
        if (back != null) {
            ordered.put("back", back);
        }

        return ordered;
    }

    /**
     * Convenience wrapper for a button that navigates somewhere, recording the current
     * screen so Back returns to it.
     *
     * @param text    The already-converted button label.
     * @param image   The button icon, or {@code null}.
     * @param context The rendering context.
     * @param open    Opens the destination.
     * @return The assembled button.
     */
    @NotNull
    default FormButton navigating(
            @NotNull String text, @Nullable FormImage image,
            @NotNull SharedContext context, @NotNull Runnable open
    ) {
        return new FormButton(text, image, () -> {
            context.pushCurrent();
            open.run();
        });
    }
}