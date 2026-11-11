package com.hibiscusmc.hmcclaims.form.impl;

import com.hibiscusmc.hmcclaims.api.event.ClaimAccessChangeEvent;
import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimAccess;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.form.ClaimManageFormConfig;
import com.hibiscusmc.hmcclaims.config.form.FormTemplate;
import com.hibiscusmc.hmcclaims.config.form.MainClaimManageFormConfig;
import com.hibiscusmc.hmcclaims.form.BaseForm;
import com.hibiscusmc.hmcclaims.form.FormRegistry;
import com.hibiscusmc.hmcclaims.form.FormService;
import com.hibiscusmc.hmcclaims.form.FormText;
import com.hibiscusmc.hmcclaims.form.SharedContext;
import com.hibiscusmc.hmcclaims.form.input.FormInputs;
import com.hibiscusmc.hmcclaims.form.spec.SimpleFormSpec;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.selection.SelectionManager;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.EventUtil;
import com.hibiscusmc.hmcclaims.util.PlaceholderUtil;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.Map;
import java.util.UUID;

@Singleton
public class ClaimManageForm implements BaseForm {

    @Inject
    private ConfigHolder<MainClaimManageFormConfig> configHolder;
    @Inject
    protected ConfigHolder<Messages> messagesHolder;

    @Inject
    protected StorageHolder storageHolder;

    @Inject
    protected FormRegistry forms;
    @Inject
    protected FormService formService;
    @Inject
    protected FormInputs inputs;

    @Inject
    protected ClaimManager claimManager;

    @Inject
    protected UserManager userManager;

    @Inject
    protected SelectionManager selectionManager;

    @Inject
    protected PlaceholderUtil placeholders;
    @Inject
    protected SchedulerUtil scheduler;
    @Inject
    protected TextUtil text;
    @Inject
    protected ClaimAccess access;

    private MainClaimManageFormConfig config;

    @Override
    public void loadConfig() {
        MainClaimManageFormConfig loaded = configHolder.get();
        if (loaded == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        config = loaded;
    }

    @Override
    public void send(@NotNull Player player, @NotNull GuiMetadata metadata) {
        if (!access.canOpen(player, metadata.claim())) {
            text.send(player, messagesHolder.get().commands().notYourClaim());
            return;
        }

        render(player, metadata, config, ClaimManageForm.class);
    }

    protected void render(
            @NotNull Player player, @NotNull GuiMetadata metadata,
            @NotNull ClaimManageFormConfig config, @NotNull Class<? extends BaseForm> current
    ) {
        Claim claim = metadata.claim();
        Map<String, String> data = placeholders.claimInfo(claim);

        SharedContext context = new SharedContext(
                player, metadata, current, formService, forms, () -> render(player, metadata, config, current)
        ).navigation(config.nav())
                .extraButtons(config.extraButtons())
                .backButton(config.backButton());

        SimpleFormSpec.Builder builder = SimpleFormSpec.builder()
                .title(FormText.line(config.title().text(), player, Map.of(
                        "claim_name", FormText.shorten(claim.name(), config.title().maxLength())
                )))
                .content(FormText.block(config.content(), player, data));

        for (String section : config.order()) {
            if (renderSection(section, builder, context, player, metadata, config, data)) {
                continue;
            }

            renderShared(section, builder, context);
        }

        formService.send(player, builder.build());
    }

    protected boolean renderSection(
            @NotNull String section, @NotNull SimpleFormSpec.Builder builder, @NotNull SharedContext context,
            @NotNull Player player, @NotNull GuiMetadata metadata, @NotNull ClaimManageFormConfig config,
            @NotNull Map<String, String> data
    ) {
        Claim claim = metadata.claim();

        switch (section) {
            case "rename" -> button(builder, context, config.renameButton(), player, data,
                    ownerOnly(player, claim, () -> rename(player, metadata, context)));

            case "lock" -> {
                FormTemplate.Button button = claim.locked() ? config.unlockButton() : config.lockButton();

                button(builder, context, button, player, data, ownerOnly(player, claim, () -> {
                    claim.locked(!claim.locked());
                    storageHolder.get().claims().saveClaimMeta(claim);

                    if (claim.locked()) {
                        EventUtil.call(new ClaimAccessChangeEvent(claim, ClaimAccessChangeEvent.Cause.CLAIM_LOCKED, null));
                    }

                    context.refresh();
                }));
            }

            case "banned" -> button(builder, context, config.bannedButton(), player, data,
                    () -> context.navigateTo(ClaimBannedListForm.class));

            case "resize" -> button(builder, context, config.resizeButton(), player, data, ownerOnly(player, claim, () -> {
                formService.clear(player);

                selectionManager.beginResize(player, claim);
            }));

            case "delete" -> button(builder, context, config.deleteButton(), player, data,
                    ownerOnly(player, claim, () -> context.navigateTo(ClaimDeleteForm.class)));

            case "transfer" -> {
                if (!(config instanceof MainClaimManageFormConfig main)) {
                    return false;
                }

                button(builder, context, main.transferButton(), player, data,
                        ownerOnly(player, claim, () -> transfer(player, metadata, context)));
            }

            default -> {
                return false;
            }
        }

        return true;
    }

    protected void button(
            @NotNull SimpleFormSpec.Builder builder, @NotNull SharedContext context, @NotNull FormTemplate.Button button,
            @NotNull Player player, @NotNull Map<String, String> data, @NotNull Runnable action
    ) {
        builder.button(
                FormText.line(button.text(), player, data),
                context.image(button.image(), null),
                action
        );
    }

    protected Runnable ownerOnly(@NotNull Player player, @NotNull Claim claim, @NotNull Runnable action) {
        return () -> {
            if (!access.isOwner(player, claim)) {
                text.send(player, messagesHolder.get().commands().notYourClaim());
                return;
            }

            action.run();
        };
    }

    protected void rename(@NotNull Player player, @NotNull GuiMetadata metadata, @NotNull SharedContext context) {
        Claim claim = metadata.claim();

        inputs.prompt(
                player, inputs.config().renameClaim(), Map.of("claim_name", claim.name()), claim.name(),
                newName -> {
                    claim.rename(newName);
                    storageHolder.get().claims().saveClaimName(claim);

                    context.refresh();
                },
                context::refresh
        );
    }

    private void transfer(@NotNull Player player, @NotNull GuiMetadata metadata, @NotNull SharedContext context) {
        Claim claim = metadata.claim();
        UUID oldOwner = claim.owner();

        inputs.promptPlayer(
                player, inputs.config().transferOwnership(), Map.of("claim_name", claim.name()),
                target -> {
                    Messages messages = messagesHolder.get();

                    if (target.id().equals(claim.owner())) {
                        text.send(player, target.id().equals(player.getUniqueId())
                                ? messages.claims().selfAlreadyOwner()
                                : messages.claims().memberAlreadyOwner());

                        context.refresh();
                        return;
                    }

                    Player targetPlayer = Bukkit.getPlayer(target.id());
                    User recipient = targetPlayer == null ? null : userManager.getUser(target.id()).orElse(null);

                    if (recipient == null) {
                        text.send(player, messages.claims().transferTargetOffline(), Map.of("name", target.name()));

                        context.refresh();
                        return;
                    }

                    userManager.calculateUsedBlocks(recipient);

                    long required = claim.main() == null ? claim.region().getSurfaceArea() : 0;
                    long available = userManager.getRemainingBlocks(recipient);

                    if (available < required) {
                        text.send(player, messages.claims().transferNotEnoughBlocks(), Map.of(
                                "name", target.name(),
                                "required_blocks", required + "",
                                "current_blocks", available + ""
                        ));

                        context.refresh();
                        return;
                    }

                    claim.transfer(target);

                    text.send(player, messages.claims().claimTransferred(), Map.of(
                            "name", target.name(),
                            "player_head", "",
                            "claim", claim.name()
                    ));

                    claimManager.transferClaim(claim, oldOwner, recipient);

                    userManager.getUser(oldOwner).ifPresent(user -> userManager.calculateUsedBlocks(user));
                    userManager.calculateUsedBlocks(recipient);

                    EventUtil.call(new ClaimAccessChangeEvent(claim, ClaimAccessChangeEvent.Cause.OWNER_TRANSFERRED, target.id()));

                    formService.root(player, () -> forms.get(ClaimListForm.class).send(player));
                },
                context::refresh
        );
    }
}