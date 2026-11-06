package com.hibiscusmc.hmcclaims.form.impl;

import com.hibiscusmc.hmcclaims.api.event.ClaimAccessChangeEvent;
import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.config.Permissions;
import com.hibiscusmc.hmcclaims.config.form.ClaimRoleManageFormConfig;
import com.hibiscusmc.hmcclaims.config.form.DialogsFormConfig;
import com.hibiscusmc.hmcclaims.config.form.PermissionFormTemplate;
import com.hibiscusmc.hmcclaims.form.BaseForm;
import com.hibiscusmc.hmcclaims.form.FormRegistry;
import com.hibiscusmc.hmcclaims.form.FormService;
import com.hibiscusmc.hmcclaims.form.FormText;
import com.hibiscusmc.hmcclaims.form.SharedContext;
import com.hibiscusmc.hmcclaims.form.input.FormInputs;
import com.hibiscusmc.hmcclaims.form.spec.CustomFormSpec;
import com.hibiscusmc.hmcclaims.form.spec.FormComponent;
import com.hibiscusmc.hmcclaims.form.spec.ModalFormSpec;
import com.hibiscusmc.hmcclaims.form.spec.SimpleFormSpec;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.EventUtil;
import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class ClaimRoleManageForm implements BaseForm {

    @Inject
    private ConfigHolder<ClaimRoleManageFormConfig> configHolder;

    @Inject
    private ConfigHolder<Permissions> permissionsHolder;

    @Inject
    private StorageHolder storageHolder;

    @Inject
    private FormInputs inputs;

    @Inject
    private FormRegistry forms;
    @Inject
    private FormService formService;

    @Inject
    private TextUtil text;

    private ClaimRoleManageFormConfig config;

    @Override
    public void loadConfig() {
        ClaimRoleManageFormConfig loaded = configHolder.get();
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
        ClaimRole role = metadata.role();

        List<PermissionFormTemplate.PermissionCategory> categories =
                config.categories(permissionsHolder.get());

        SharedContext context = new SharedContext(
                player, metadata, ClaimRoleManageForm.class, formService, forms, () -> render(player, metadata)
        ).navigation(config.nav())
                .extraButtons(config.extraButtons())
                .backButton(config.backButton());

        Map<String, String> data = Map.of("role_name", role.name());

        SimpleFormSpec.Builder builder = SimpleFormSpec.builder()
                .title(FormText.line(config.title().text(), player, Map.of(
                        "role_name", FormText.shorten(role.name(), config.title().maxLength())
                )))
                .content(FormText.block(config.content(), player, data));

        for (String section : config.order()) {
            switch (section) {
                case "categories" -> categories.forEach(category -> builder.button(navigating(
                        FormText.line(category.name(), player, data),
                        context.image(category.image(), category.fallback()),
                        context,
                        () -> openCategory(player, metadata, category)
                )));

                case "delete" -> {
                    if (metadata.canManageRole()) {
                        builder.button(
                                FormText.line(config.deleteButton().text(), player, data),
                                context.image(config.deleteButton().image(), null),
                                () -> delete(player, metadata)
                        );
                    }
                }

                default -> renderShared(section, builder, context);
            }
        }

        formService.send(player, builder.build());
    }

    private void openCategory(
            @NotNull Player player, @NotNull GuiMetadata metadata,
            @NotNull PermissionFormTemplate.PermissionCategory category
    ) {
        ClaimRole role = metadata.role();
        boolean editable = metadata.canManageRolePermissions();

        SharedContext context = new SharedContext(
                player, metadata, ClaimRoleManageForm.class, formService, forms,
                () -> openCategory(player, metadata, category)
        ).navigation(config.nav());

        Map<String, String> data = Map.of(
                "role_name", role.name(),
                "category", category.name()
        );

        CustomFormSpec.Builder builder = CustomFormSpec.builder()
                .title(FormText.line(config.categoryTitle().text(), player, Map.of(
                        "role_name", FormText.shorten(role.name(), config.categoryTitle().maxLength()),
                        "category", category.name()
                )));

        String header = FormText.block(
                editable ? category.content() : config.readOnlyContent(), player, data
        );

        if (!header.isEmpty()) {
            builder.label(header);
        }

        List<Row> rows = new ArrayList<>();

        for (PermissionFormTemplate.PermissionRow row : category.permissions()) {
            Permission permission = row.permission();
            String key = RegistryUtil.serialize(permission.key());
            boolean enabled = role.hasPermission(permission);
            String label = FormText.line(row.label(), player);

            if (config.showDescriptions() && !row.description().isEmpty()) {
                builder.label(FormText.block(row.description(), player, Map.of()));
            }

            if (editable) {
                builder.toggle(key, label, enabled);
                rows.add(new Row(key, permission));
            } else {
                builder.label(FormText.line(config.readOnlyRow(), player, Map.of(
                        "label", row.label(),
                        "value", state(enabled)
                )));
            }
        }

        FormComponent.Dropdown navigation = navigationDropdown(context);
        builder.component(navigation);

        builder.onSubmit(values -> {
            Runnable submit = () -> {
                if (editable) {
                    for (Row row : rows) {
                        if (values.toggle(row.key(), role.hasPermission(row.permission()))) {
                            role.addPermission(row.permission());
                        } else {
                            role.removePermission(row.permission());
                        }
                    }

                    storageHolder.get().claims().saveRoles(metadata.claim());

                    EventUtil.call(new ClaimAccessChangeEvent(metadata.claim(), ClaimAccessChangeEvent.Cause.ROLE_PERMISSIONS_CHANGED, null));
                }

                if (navigation != null) {
                    Class<? extends BaseForm> target = navigationTarget(values.index(NAVIGATION_KEY, 0), context);

                    if (target != null) {
                        context.navigateTo(target);
                        return;
                    }
                }

                formService.back(player);
            };

            List<String> warnings = new ArrayList<>();

            if (editable) {
                for (Row row : rows) {
                    boolean before = role.hasPermission(row.permission());
                    boolean after = values.toggle(row.key(), before);

                    if (before == after) {
                        continue;
                    }

                    Permissions.Entry entry = permissionsHolder.get().entry(row.permission());
                    List<String> warning = entry.warning(after);

                    if (!warning.isEmpty()) {
                        warnings.add(FormText.block(warning, player, Map.of("permission", entry.name())));
                    }
                }
            }

            if (warnings.isEmpty()) {
                submit.run();
                return;
            }

            DialogsFormConfig.Confirm confirm = inputs.config().confirm();

            formService.send(player, new ModalFormSpec(
                    FormText.line(confirm.title(), player),
                    String.join(confirm.separator(), warnings),
                    FormText.line(confirm.confirm(), player),
                    FormText.line(confirm.cancel(), player),
                    submit,
                    () -> openCategory(player, metadata, category),
                    () -> openCategory(player, metadata, category)
            ));
        });

        builder.onClose(() -> formService.back(player));

        formService.send(player, builder.build());
    }

    private void delete(@NotNull Player player, @NotNull GuiMetadata metadata) {
        Claim claim = metadata.claim();

        if (!metadata.canManageRole()) {
            text.send(player, config.cantDelete());
            return;
        }

        claim.roleRegistry().remove(metadata.role());
        storageHolder.get().claims().saveRoles(claim);

        EventUtil.call(new ClaimAccessChangeEvent(claim, ClaimAccessChangeEvent.Cause.ROLE_DELETED, null));

        formService.back(player);
    }

    @NotNull
    private String state(boolean enabled) {
        return config.states().getOrDefault(enabled ? "enabled" : "disabled", enabled ? "Enabled" : "Disabled");
    }

    private record Row(String key, Permission permission) {
    }
}