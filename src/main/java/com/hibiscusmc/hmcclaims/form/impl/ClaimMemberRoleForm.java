package com.hibiscusmc.hmcclaims.form.impl;

import com.hibiscusmc.hmcclaims.api.event.ClaimAccessChangeEvent;
import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.form.ClaimMemberRoleFormConfig;
import com.hibiscusmc.hmcclaims.config.form.FormTemplate;
import com.hibiscusmc.hmcclaims.form.BaseForm;
import com.hibiscusmc.hmcclaims.form.FormRegistry;
import com.hibiscusmc.hmcclaims.form.FormService;
import com.hibiscusmc.hmcclaims.form.FormText;
import com.hibiscusmc.hmcclaims.form.SharedContext;
import com.hibiscusmc.hmcclaims.form.spec.SimpleFormSpec;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.gui.impl.MenuGuard;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.EventUtil;
import com.hibiscusmc.hmcclaims.util.StringUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.List;
import java.util.Map;

@Singleton
public class ClaimMemberRoleForm implements BaseForm {

    @Inject
    private ConfigHolder<ClaimMemberRoleFormConfig> configHolder;

    @Inject
    private MenuGuard guard;

    @Inject
    private StorageHolder storageHolder;

    @Inject
    private FormRegistry forms;
    @Inject
    private FormService formService;

    @Inject
    private TextUtil text;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    private ClaimMemberRoleFormConfig config;

    @Override
    public void loadConfig() {
        ClaimMemberRoleFormConfig loaded = configHolder.get();
        if (loaded == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        config = loaded;
    }

    @Override
    public void send(@NotNull Player player, @NotNull GuiMetadata metadata) {
        if (!guard.canOpen(player, metadata.claim())) {
            text.send(player, messagesHolder.get().commands().notYourClaim());
            return;
        }

        render(player, metadata);
    }

    private void render(@NotNull Player player, @NotNull GuiMetadata metadata) {
        Claim claim = metadata.claim();
        ClaimMember member = metadata.member();

        List<ClaimRole> allRoles = claim.roleRegistry().allRoles();

        SharedContext context = new SharedContext(
                player, metadata, ClaimMemberRoleForm.class, formService, forms, () -> render(player, metadata)
        ).extraButtons(config.extraButtons())
                .backButton(config.backButton());

        Map<String, String> data = Map.of(
                "member_name", member.lastKnownName(),
                "role", member.role().name()
        );

        SimpleFormSpec.Builder builder = SimpleFormSpec.builder()
                .title(FormText.line(config.title().text(), player, Map.of(
                        "member_name", FormText.shorten(member.lastKnownName(), config.title().maxLength())
                )))
                .content(FormText.block(config.content(), player, data));

        for (String section : config.order()) {
            switch (section) {
                case "roles" -> {
                    for (ClaimRole role : allRoles) {
                        addRole(builder, context, player, claim, member, role);
                    }
                }

                case "tabs" -> {
                    builder.button(navigating(
                            FormText.line(config.tabs().permissionsButton().text(), player, data),
                            context.image(config.tabs().permissionsButton().image(), null),
                            context,
                            () -> forms.get(ClaimMemberPermissionsForm.class).send(player, metadata)
                    ));

                    builder.button(navigating(
                            FormText.line(config.tabs().memberListButton().text(), player, data),
                            context.image(config.tabs().memberListButton().image(), null),
                            context,
                            () -> forms.get(ClaimMemberListForm.class).send(player, metadata)
                    ));
                }

                default -> renderShared(section, builder, context);
            }
        }

        formService.send(player, builder.build());
    }

    private void addRole(
            @NotNull SimpleFormSpec.Builder builder, @NotNull SharedContext context, @NotNull Player player,
            @NotNull Claim claim, @NotNull ClaimMember member, @NotNull ClaimRole role
    ) {
        boolean selected = member.role().equals(role);
        boolean assignable = guard.canAssignRole(player, claim, member, role);

        FormTemplate.Entry entry;
        if (selected) {
            entry = config.selectedRoleEntry();
        } else if (assignable) {
            entry = config.roleEntry();
        } else {
            entry = config.unavailableRoleEntry();
        }

        long members = claim.members().stream()
                .filter(other -> other.role().equals(role))
                .count();

        Map<String, String> data = Map.of(
                "name", role.name(),
                "members", String.valueOf(members),
                "creation_date", StringUtil.formatDate(role.creationTimestamp()),
                "player_name", member.lastKnownName()
        );

        builder.button(
                FormText.block(entry.text(), player, data),
                context.image(entry.image(), null),
                () -> {
                    if (selected) {
                        context.refresh();
                        return;
                    }

                    if (!guard.canAssignRole(player, claim, member, role)) {
                        text.send(player, config.cantAssign());
                        context.refresh();
                        return;
                    }

                    member.role(role);
                    storageHolder.get().claims().saveMembers(claim);

                    EventUtil.call(new ClaimAccessChangeEvent(claim, ClaimAccessChangeEvent.Cause.MEMBER_ROLE_CHANGED, member.uuid()));

                    context.refresh();
                }
        );
    }
}