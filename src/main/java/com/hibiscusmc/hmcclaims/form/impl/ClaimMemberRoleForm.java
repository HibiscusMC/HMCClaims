package com.hibiscusmc.hmcclaims.form.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRoleRegistry;
import com.hibiscusmc.hmcclaims.config.form.ClaimMemberRoleFormConfig;
import com.hibiscusmc.hmcclaims.config.form.FormTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.form.BaseForm;
import com.hibiscusmc.hmcclaims.form.FormRegistry;
import com.hibiscusmc.hmcclaims.form.FormService;
import com.hibiscusmc.hmcclaims.form.FormText;
import com.hibiscusmc.hmcclaims.form.SharedContext;
import com.hibiscusmc.hmcclaims.form.spec.SimpleFormSpec;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.StringUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ClaimMemberRoleForm implements BaseForm {

    @Inject
    private ConfigHolder<ClaimMemberRoleFormConfig> configHolder;

    @Inject
    private StorageHolder storageHolder;

    @Inject
    private FormRegistry forms;
    @Inject
    private FormService formService;

    @Inject
    private TextUtil text;

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
        render(player, metadata);
    }

    private void render(@NotNull Player player, @NotNull GuiMetadata metadata) {
        Claim claim = metadata.claim();
        ClaimMember member = metadata.member();

        ClaimRoleRegistry registry = claim.roleRegistry();
        List<ClaimRole> allRoles = registry.allRoles();

        Optional<ClaimMember> self = claim.getMember(player.getUniqueId());
        boolean canManage = self
                .map(executor -> executor.hasPermission(Permission.MANAGE_MEMBER_ROLES) && executor.canManage(member))
                .orElse(false);

        int selfPosition = allRoles.indexOf(self.map(ClaimMember::role).orElse(registry.everyoneRole()));

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
                        addRole(builder, context, player, claim, member, role,
                                canManage, selfPosition, allRoles.indexOf(role), registry);
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
            @NotNull Claim claim, @NotNull ClaimMember member, @NotNull ClaimRole role,
            boolean canManage, int selfPosition, int rolePosition, @NotNull ClaimRoleRegistry registry
    ) {
        boolean selected = member.role().equals(role);
        boolean assignable = canManage
                && selfPosition < rolePosition
                && !role.equals(registry.everyoneRole())
                && !role.equals(registry.ownerRole());

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

                    if (!assignable) {
                        text.send(player, config.cantAssign());
                        context.refresh();
                        return;
                    }

                    member.role(role);
                    storageHolder.get().claims().saveMembers(claim);

                    context.refresh();
                }
        );
    }
}