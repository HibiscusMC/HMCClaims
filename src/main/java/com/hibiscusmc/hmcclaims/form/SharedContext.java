package com.hibiscusmc.hmcclaims.form;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.config.form.FormTemplate;
import com.hibiscusmc.hmcclaims.form.spec.FormImage;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Everything the shared sections of {@link BaseForm} need to render one screen.
 */
public final class SharedContext {

    private final Player player;
    private final GuiMetadata metadata;
    private final Class<? extends BaseForm> current;
    private final FormService forms;
    private final FormRegistry registry;
    private final Runnable reopen;

    private FormTemplate.Navigation navigation;
    private Map<String, FormTemplate.ActionButton> extraButtons = Map.of();
    private FormTemplate.Button backButton;

    /**
     * @param player   The Bedrock player being shown the form.
     * @param metadata The claim context, or {@code null} for the root claim list.
     * @param current  The form class being rendered, skipped in the navigation row.
     * @param forms    The form service.
     * @param registry The form registry, used to reach navigation destinations.
     * @param reopen   Re-renders the current screen; recorded when navigating deeper.
     */
    public SharedContext(
            @NotNull Player player, @Nullable GuiMetadata metadata, @NotNull Class<? extends BaseForm> current,
            @NotNull FormService forms, @NotNull FormRegistry registry, @NotNull Runnable reopen
    ) {
        this.player = player;
        this.metadata = metadata;
        this.current = current;
        this.forms = forms;
        this.registry = registry;
        this.reopen = reopen;
    }

    /**
     * Declares the navigation row this form renders.
     */
    public SharedContext navigation(@Nullable FormTemplate.Navigation navigation) {
        this.navigation = navigation;
        return this;
    }

    /**
     * Declares the custom buttons this form can place with {@code extra:<key>}.
     */
    public SharedContext extraButtons(@Nullable Map<String, FormTemplate.ActionButton> extraButtons) {
        this.extraButtons = extraButtons == null ? Map.of() : extraButtons;
        return this;
    }

    /**
     * Declares the Back button this form renders.
     */
    public SharedContext backButton(@Nullable FormTemplate.Button backButton) {
        this.backButton = backButton;
        return this;
    }

    @NotNull
    public Player player() {
        return player;
    }

    @Nullable
    public GuiMetadata metadata() {
        return metadata;
    }

    /**
     * @return The claim this screen belongs to, or {@code null} at the root list.
     */
    @Nullable
    public Claim claim() {
        return metadata == null ? null : metadata.claim();
    }

    @NotNull
    public Class<? extends BaseForm> current() {
        return current;
    }

    @NotNull
    public FormService forms() {
        return forms;
    }

    @NotNull
    public FormRegistry registry() {
        return registry;
    }

    @Nullable
    public FormTemplate.Navigation navigation() {
        return navigation;
    }

    @NotNull
    public Map<String, FormTemplate.ActionButton> extraButtons() {
        return extraButtons;
    }

    @Nullable
    public FormTemplate.Button backButton() {
        return backButton;
    }

    /**
     * Resolves a configured button icon.
     *
     * @param config           The image block from the config.
     * @param fallbackMaterial The material to borrow a texture from, or {@code null}.
     * @return The icon, or {@code null} for a text-only button.
     */
    @Nullable
    public FormImage image(@Nullable FormTemplate.Image config, @Nullable Material fallbackMaterial) {
        return FormImages.resolve(config, fallbackMaterial, forms.materialImageFallback());
    }

    /**
     * Records the current screen so the next Back returns to it.
     */
    public void pushCurrent() {
        forms.push(player, reopen);
    }

    /**
     * Re-renders the current screen without touching the history. Used after an action
     * that changed something the screen displays.
     */
    public void refresh() {
        reopen.run();
    }

    /**
     * Opens another form, recording the current screen first.
     *
     * @param target The form to open.
     */
    public void navigateTo(@NotNull Class<? extends BaseForm> target) {
        BaseForm form = registry.get(target);
        if (form == null) {
            return;
        }

        pushCurrent();

        if (metadata == null) {
            form.send(player);
        } else {
            form.send(player, metadata);
        }
    }
}