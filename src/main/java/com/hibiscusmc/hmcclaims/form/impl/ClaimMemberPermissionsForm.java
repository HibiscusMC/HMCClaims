package com.hibiscusmc.hmcclaims.form.impl;

import com.hibiscusmc.hmcclaims.api.event.ClaimAccessChangeEvent;
import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Permissions;
import com.hibiscusmc.hmcclaims.config.form.ClaimMemberPermissionsFormConfig;
import com.hibiscusmc.hmcclaims.config.form.DialogsFormConfig;
import com.hibiscusmc.hmcclaims.config.form.PermissionFormTemplate;
import com.hibiscusmc.hmcclaims.form.BaseForm;
import com.hibiscusmc.hmcclaims.form.FormRegistry;
import com.hibiscusmc.hmcclaims.form.FormService;
import com.hibiscusmc.hmcclaims.form.FormText;
import com.hibiscusmc.hmcclaims.form.SharedContext;
import com.hibiscusmc.hmcclaims.form.input.FormInputs;
import com.hibiscusmc.hmcclaims.form.spec.CustomFormSpec;
import com.hibiscusmc.hmcclaims.form.spec.ModalFormSpec;
import com.hibiscusmc.hmcclaims.form.spec.SimpleFormSpec;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.gui.impl.MenuGuard;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.EventUtil;
import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class ClaimMemberPermissionsForm implements BaseForm {

    private final static List<String> STATES = List.of("enabled", "unset", "disabled");

    @Inject
    private ConfigHolder<ClaimMemberPermissionsFormConfig> configHolder;

    @Inject
    private MenuGuard guard;

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
    private ConfigHolder<Messages> messagesHolder;
    @Inject
    private TextUtil text;

    private ClaimMemberPermissionsFormConfig config;

    @Override
    public void loadConfig() {
        ClaimMemberPermissionsFormConfig loaded = configHolder.get();
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
        ClaimMember member = metadata.member();

        List<PermissionFormTemplate.PermissionCategory> categories =
                config.categories(permissionsHolder.get());

        SharedContext context = new SharedContext(
                player, metadata, ClaimMemberPermissionsForm.class, formService, forms, () -> render(player, metadata)
        ).extraButtons(config.extraButtons())
                .backButton(config.backButton());

        Map<String, String> data = Map.of("member_name", member.lastKnownName());

        SimpleFormSpec.Builder builder = SimpleFormSpec.builder()
                .title(FormText.line(config.title().text(), player, Map.of(
                        "member_name", FormText.shorten(member.lastKnownName(), config.title().maxLength())
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

                case "tabs" -> {
                    builder.button(navigating(
                            FormText.line(config.tabs().roleButton().text(), player, data),
                            context.image(config.tabs().roleButton().image(), null),
                            context,
                            () -> forms.get(ClaimMemberRoleForm.class).send(player, metadata)
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

    private void openCategory(
            @NotNull Player player, @NotNull GuiMetadata metadata,
            @NotNull PermissionFormTemplate.PermissionCategory category
    ) {
        Claim claim = metadata.claim();
        ClaimMember member = metadata.member();

        boolean editable = canEdit(player, claim, member);

        Map<String, String> data = Map.of(
                "member_name", member.lastKnownName(),
                "category", category.name()
        );

        CustomFormSpec.Builder builder = CustomFormSpec.builder()
                .title(FormText.line(config.categoryTitle().text(), player, Map.of(
                        "member_name", FormText.shorten(member.lastKnownName(), config.categoryTitle().maxLength()),
                        "category", category.name()
                )));

        String header = FormText.block(
                editable ? category.content() : config.readOnlyContent(), player, data
        );

        if (!header.isEmpty()) {
            builder.label(header);
        }

        List<String> options = STATES.stream()
                .map(state -> FormText.line(state(state), player))
                .toList();

        Object2BooleanMap<Permission> overrides = member.permissions();
        List<Row> rows = new ArrayList<>();

        for (PermissionFormTemplate.PermissionRow row : category.permissions()) {
            Permission permission = row.permission();
            String key = RegistryUtil.serialize(permission.key());
            int current = stateIndex(overrides, permission);

            if (config.showDescriptions() && !row.description().isEmpty()) {
                builder.label(FormText.block(row.description(), player, Map.of()));
            }

            if (editable && guard.canGrant(player, claim, permission)) {
                builder.dropdown(key, FormText.line(row.label(), player), options, current);
                rows.add(new Row(key, permission));
            } else {
                builder.label(FormText.line(config.readOnlyRow(), player, Map.of(
                        "label", row.label(),
                        "value", state(STATES.get(current))
                )));
            }
        }

        builder.onSubmit(values -> {
            if (!editable) {
                formService.back(player);
                return;
            }

            Runnable submit = () -> {
                if (!canEdit(player, claim, member)) {
                    guard.deny(player);
                    return;
                }

                for (Row row : rows) {
                    if (!guard.canGrant(player, claim, row.permission())) {
                        continue;
                    }

                    apply(overrides, row.permission(), values.index(row.key(), stateIndex(overrides, row.permission())));
                }

                storageHolder.get().claims().saveMembers(claim);

                EventUtil.call(new ClaimAccessChangeEvent(claim, ClaimAccessChangeEvent.Cause.MEMBER_PERMISSIONS_CHANGED, member.uuid()));

                formService.back(player);
            };

            if (!canEdit(player, claim, member)) {
                guard.deny(player);
                return;
            }

            List<String> warnings = new ArrayList<>();

            for (Row row : rows) {
                Permission permission = row.permission();
                if (!guard.canGrant(player, claim, permission)) {
                    continue;
                }

                int selected = values.index(row.key(), stateIndex(overrides, permission));

                boolean before = member.hasPermission(permission);
                boolean after = switch (selected < 0 || selected >= STATES.size() ? "unset" : STATES.get(selected)) {
                    case "enabled" -> true;
                    case "disabled" -> false;
                    default -> member.role().hasPermission(permission);
                };

                if (before == after) {
                    continue;
                }

                Permissions.Entry entry = permissionsHolder.get().entry(permission);
                List<String> warning = entry.warning(after);

                if (!warning.isEmpty()) {
                    warnings.add(FormText.block(warning, player, Map.of("permission", entry.name())));
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

    private boolean canEdit(@NotNull Player player, @NotNull Claim claim, @NotNull ClaimMember member) {
        return guard.hasPermission(player, claim, Permission.MANAGE_MEMBER_PERMISSIONS)
                && guard.canManage(player, claim, member);
    }

    private void apply(@NotNull Object2BooleanMap<Permission> overrides, @NotNull Permission permission, int selected) {
        switch (selected < 0 || selected >= STATES.size() ? "unset" : STATES.get(selected)) {
            case "enabled" -> overrides.put(permission, true);
            case "disabled" -> overrides.put(permission, false);
            default -> overrides.removeBoolean(permission);
        }
    }

    private int stateIndex(@NotNull Object2BooleanMap<Permission> overrides, @NotNull Permission permission) {
        if (!overrides.containsKey(permission)) {
            return STATES.indexOf("unset");
        }

        return STATES.indexOf(overrides.getBoolean(permission) ? "enabled" : "disabled");
    }

    @NotNull
    private String state(@NotNull String key) {
        return config.states().getOrDefault(key, key);
    }

    private record Row(String key, Permission permission) {
    }
}