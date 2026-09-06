package com.hibiscusmc.hmcclaims.form.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRoleRegistry;
import com.hibiscusmc.hmcclaims.config.form.ClaimRolesFormConfig;
import com.hibiscusmc.hmcclaims.config.form.FormTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.form.BaseForm;
import com.hibiscusmc.hmcclaims.form.FormRegistry;
import com.hibiscusmc.hmcclaims.form.FormService;
import com.hibiscusmc.hmcclaims.form.FormText;
import com.hibiscusmc.hmcclaims.form.SharedContext;
import com.hibiscusmc.hmcclaims.form.input.FormInputs;
import com.hibiscusmc.hmcclaims.form.spec.SimpleFormSpec;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.StringUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public class ClaimRolesForm implements BaseForm {

    @Inject
    private ConfigHolder<ClaimRolesFormConfig> configHolder;

    @Inject
    private StorageHolder storageHolder;

    @Inject
    private FormRegistry forms;
    @Inject
    private FormService formService;
    @Inject
    private FormInputs inputs;

    @Inject
    private SchedulerUtil scheduler;
    @Inject
    private TextUtil text;

    private ClaimRolesFormConfig config;

    @Override
    public void loadConfig() {
        ClaimRolesFormConfig loaded = configHolder.get();
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

        scheduler.scheduleAsync(() -> {
            List<ClaimRole> roles = claim.roleRegistry().allRoles();

            scheduler.schedule(() -> {
                SharedContext context = new SharedContext(
                        player, metadata, ClaimRolesForm.class, formService, forms, () -> render(player, metadata)
                ).navigation(config.nav())
                        .extraButtons(config.extraButtons())
                        .backButton(config.backButton());

                SimpleFormSpec.Builder builder = SimpleFormSpec.builder()
                        .title(FormText.line(config.title().text(), player, Map.of(
                                "claim_name", FormText.shorten(claim.name(), config.title().maxLength())
                        )))
                        .content(FormText.block(config.content(), player, Map.of(
                                "total", String.valueOf(roles.size())
                        )));

                for (String section : config.order()) {
                    switch (section) {
                        case "create-role" -> builder.button(
                                FormText.line(config.createRoleButton().text(), player),
                                context.image(config.createRoleButton().image(), null),
                                () -> createRole(player, metadata)
                        );

                        case "roles" -> roles.forEach(role -> builder.button(navigating(
                                FormText.block(config.roleEntry().text(), player, rolePlaceholders(claim, role)),
                                context.image(config.roleEntry().image(), null),
                                context,
                                () -> openActions(player, metadata, role)
                        )));

                        default -> renderShared(section, builder, context);
                    }
                }

                formService.send(player, builder.build());
            });
        });
    }

    private void openActions(@NotNull Player player, @NotNull GuiMetadata metadata, @NotNull ClaimRole role) {
        Claim claim = metadata.claim();
        ClaimRoleRegistry registry = claim.roleRegistry();
        List<ClaimRole> allRoles = registry.allRoles();

        int rolePosition = allRoles.indexOf(role);
        int total = allRoles.size();

        ClaimMember self = claim.getMember(player.getUniqueId()).orElse(null);
        ClaimRole selfRole = self != null ? self.role() : registry.everyoneRole();
        int selfPosition = allRoles.indexOf(selfRole);

        boolean isOwnerRole = role.equals(registry.ownerRole());
        boolean isDefaultRole = isOwnerRole || role.equals(registry.defaultRole()) || role.equals(registry.everyoneRole());

        boolean canManage = self != null && self.hasPermission(Permission.MANAGE_ROLES)
                && selfPosition < rolePosition && !isOwnerRole;
        boolean canManagePermissions = self != null && self.hasPermission(Permission.MANAGE_ROLE_PERMISSIONS)
                && !isOwnerRole;
        boolean canRename = (selfRole.equals(registry.ownerRole()) || canManage) && !role.equals(registry.everyoneRole());
        boolean canMoveUp = canManage && rolePosition > 1 && selfPosition < rolePosition - 1 && rolePosition < total - 2;
        boolean canMoveDown = canManage && rolePosition < total - 3;
        boolean canDelete = canManage && !isDefaultRole;

        FormTemplate.SubForm actions = config.roleActions();
        Map<String, String> data = rolePlaceholders(claim, role);
        Runnable reopen = () -> openActions(player, metadata, role);

        SimpleFormSpec.Builder builder = SimpleFormSpec.builder()
                .title(FormText.line(actions.title().text(), player, Map.of(
                        "name", FormText.shorten(role.name(), actions.title().maxLength())
                )))
                .content(FormText.block(actions.content(), player, data));

        for (Map.Entry<String, FormTemplate.Button> entry
                : ordered(actions.buttons(), "permissions", "rename", "move-up", "move-down", "delete").entrySet()) {
            FormTemplate.Button button = entry.getValue();
            String label = FormText.line(button.text(), player, data);

            switch (entry.getKey()) {
                case "permissions" -> {
                    if (canManage || canManagePermissions) {
                        builder.button(label, image(button.image(), formService), () -> {
                            formService.push(player, reopen);

                            forms.get(ClaimRoleManageForm.class).send(player, metadata
                                    .role(role)
                                    .canManageRole(canDelete)
                                    .canManageRolePermissions(canManagePermissions));
                        });
                    }
                }

                case "rename" -> {
                    if (canRename) {
                        builder.button(label, image(button.image(), formService), () -> rename(player, metadata, role));
                    }
                }

                case "move-up" -> {
                    if (canMoveUp) {
                        builder.button(label, image(button.image(), formService), () -> {
                            registry.swap(allRoles.get(rolePosition - 1), role);
                            storageHolder.get().claims().saveRoles(claim);

                            formService.back(player);
                        });
                    }
                }

                case "move-down" -> {
                    if (canMoveDown) {
                        builder.button(label, image(button.image(), formService), () -> {
                            registry.swap(role, allRoles.get(rolePosition + 1));
                            storageHolder.get().claims().saveRoles(claim);

                            formService.back(player);
                        });
                    }
                }

                case "delete" -> {
                    if (canDelete) {
                        builder.button(label, image(button.image(), formService), () -> {
                            registry.remove(role);
                            storageHolder.get().claims().saveRoles(claim);

                            formService.back(player);
                        });
                    }
                }

                case "back" ->
                        builder.button(label, image(button.image(), formService), () -> formService.back(player));

                default -> builder.button(label, image(button.image(), formService), reopen);
            }
        }

        formService.send(player, builder.build());
    }

    private void rename(@NotNull Player player, @NotNull GuiMetadata metadata, @NotNull ClaimRole role) {
        Runnable reopen = () -> openActions(player, metadata, role);

        inputs.prompt(
                player, inputs.config().renameRole(), Map.of("role_name", role.name()), role.name(),
                newName -> {
                    role.rename(newName);
                    storageHolder.get().claims().saveRoles(metadata.claim());

                    reopen.run();
                },
                reopen
        );
    }

    private void createRole(@NotNull Player player, @NotNull GuiMetadata metadata) {
        Claim claim = metadata.claim();
        Runnable reopen = () -> render(player, metadata);

        ClaimMember self = claim.getMember(player.getUniqueId()).orElse(null);
        if (self == null || !self.hasPermission(Permission.MANAGE_ROLES)) {
            text.send(player, config.cantCreate());
            reopen.run();
            return;
        }

        inputs.prompt(
                player, inputs.config().createRole(), Map.of("claim_name", claim.name()), "",
                name -> {
                    ClaimRoleRegistry registry = claim.roleRegistry();
                    registry.add(new ClaimRole(UUID.randomUUID(), name, registry.defaultRole().permissions()));

                    storageHolder.get().claims().saveRoles(claim);

                    reopen.run();
                },
                reopen
        );
    }

    @NotNull
    private Map<String, String> rolePlaceholders(@NotNull Claim claim, @NotNull ClaimRole role) {
        long members = claim.members().stream()
                .filter(member -> member.role().equals(role))
                .count();

        return Map.of(
                "name", role.name(),
                "members", String.valueOf(members),
                "creation_date", StringUtil.formatDate(role.creationTimestamp())
        );
    }
}