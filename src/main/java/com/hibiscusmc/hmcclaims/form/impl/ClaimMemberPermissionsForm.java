package com.hibiscusmc.hmcclaims.form.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.config.form.ClaimMemberPermissionsFormConfig;
import com.hibiscusmc.hmcclaims.config.form.PermissionFormTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.form.BaseForm;
import com.hibiscusmc.hmcclaims.form.FormRegistry;
import com.hibiscusmc.hmcclaims.form.FormService;
import com.hibiscusmc.hmcclaims.form.FormText;
import com.hibiscusmc.hmcclaims.form.SharedContext;
import com.hibiscusmc.hmcclaims.form.spec.CustomFormSpec;
import com.hibiscusmc.hmcclaims.form.spec.SimpleFormSpec;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
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
    private StorageHolder storageHolder;

    @Inject
    private FormRegistry forms;
    @Inject
    private FormService formService;

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
        render(player, metadata);
    }

    private void render(@NotNull Player player, @NotNull GuiMetadata metadata) {
        ClaimMember member = metadata.member();

        List<PermissionFormTemplate.PermissionCategory> categories =
                PermissionFormTemplate.usable(config.permissionCategories());

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
                        context.image(category.image(), null),
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

        boolean editable = claim.hasPermission(player.getUniqueId(), Permission.MANAGE_MEMBER_PERMISSIONS)
                && claim.getMember(player.getUniqueId())
                .map(self -> self.canManage(member))
                .orElse(false);

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

        for (Map.Entry<String, PermissionFormTemplate.PermissionEntry> entry : category.permissions().entrySet()) {
            PermissionFormTemplate.PermissionEntry row = entry.getValue();
            if (row == null || row.key() == null) {
                continue;
            }

            Permission permission = row.key();
            int current = stateIndex(overrides, permission);

            if (config.showDescriptions() && !row.description().isEmpty()) {
                builder.label(FormText.block(row.description(), player, Map.of()));
            }

            if (editable) {
                builder.dropdown(entry.getKey(), FormText.line(row.label(), player), options, current);
                rows.add(new Row(entry.getKey(), permission));
            } else {
                builder.label(FormText.line(config.readOnlyRow(), player, Map.of(
                        "label", row.label(),
                        "value", state(STATES.get(current))
                )));
            }
        }

        builder.onSubmit(values -> {
            if (editable) {
                for (Row row : rows) {
                    apply(overrides, row.permission(), values.index(row.key(), stateIndex(overrides, row.permission())));
                }

                storageHolder.get().claims().saveMembers(claim);
            }

            formService.back(player);
        });

        builder.onClose(() -> formService.back(player));

        formService.send(player, builder.build());
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