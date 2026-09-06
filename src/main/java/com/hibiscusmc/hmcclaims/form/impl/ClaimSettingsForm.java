package com.hibiscusmc.hmcclaims.form.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingHolder;
import com.hibiscusmc.hmcclaims.config.DefaultSettings;
import com.hibiscusmc.hmcclaims.config.form.ClaimSettingsFormConfig;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.form.BaseForm;
import com.hibiscusmc.hmcclaims.form.FormRegistry;
import com.hibiscusmc.hmcclaims.form.FormService;
import com.hibiscusmc.hmcclaims.form.FormText;
import com.hibiscusmc.hmcclaims.form.SharedContext;
import com.hibiscusmc.hmcclaims.form.spec.CustomFormSpec;
import com.hibiscusmc.hmcclaims.form.spec.FormComponent;
import com.hibiscusmc.hmcclaims.form.spec.SimpleFormSpec;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.PlaceholderUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class ClaimSettingsForm implements BaseForm {

    @Inject
    private ConfigHolder<ClaimSettingsFormConfig> configHolder;
    @Inject
    private ConfigHolder<DefaultSettings> defaultSettingsHolder;

    @Inject
    private StorageHolder storageHolder;

    @Inject
    private FormRegistry forms;
    @Inject
    private FormService formService;

    @Inject
    private PlaceholderUtil placeholders;

    private ClaimSettingsFormConfig config;

    @Override
    public void loadConfig() {
        ClaimSettingsFormConfig loaded = configHolder.get();
        if (loaded == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        config = loaded;
    }

    @Override
    public void send(@NotNull Player player, @NotNull GuiMetadata metadata) {
        List<ClaimSettingsFormConfig.SettingCategory> categories =
                ClaimSettingsFormConfig.usable(config.settingCategories());

        if (categories.size() == 1 && !config.alwaysShowCategories()) {
            openCategory(player, metadata, categories.getFirst(), true);
            return;
        }

        render(player, metadata, categories);
    }

    private void render(
            @NotNull Player player, @NotNull GuiMetadata metadata,
            @NotNull List<ClaimSettingsFormConfig.SettingCategory> categories
    ) {
        Claim claim = metadata.claim();

        SharedContext context = new SharedContext(
                player, metadata, ClaimSettingsForm.class, formService, forms,
                () -> render(player, metadata, categories)
        ).navigation(config.nav())
                .extraButtons(config.extraButtons())
                .backButton(config.backButton());

        SimpleFormSpec.Builder builder = SimpleFormSpec.builder()
                .title(FormText.line(config.title().text(), player, Map.of(
                        "claim_name", FormText.shorten(claim.name(), config.title().maxLength())
                )))
                .content(FormText.block(config.content(), player, placeholders.claimInfo(claim)));

        for (String section : config.order()) {
            if (section.equals("categories")) {
                categories.forEach(category -> builder.button(navigating(
                        FormText.line(category.name(), player),
                        context.image(category.image(), null),
                        context,
                        () -> openCategory(player, metadata, category, false)
                )));

                continue;
            }

            renderShared(section, builder, context);
        }

        formService.send(player, builder.build());
    }

    private void openCategory(
            @NotNull Player player, @NotNull GuiMetadata metadata,
            @NotNull ClaimSettingsFormConfig.SettingCategory category, boolean withNavigation
    ) {
        Claim claim = metadata.claim();

        SharedContext context = new SharedContext(
                player, metadata, ClaimSettingsForm.class, formService, forms,
                () -> openCategory(player, metadata, category, withNavigation)
        ).navigation(config.nav());

        CustomFormSpec.Builder builder = CustomFormSpec.builder()
                .title(FormText.line(config.categoryTitle().text(), player, Map.of(
                        "claim_name", FormText.shorten(claim.name(), config.categoryTitle().maxLength()),
                        "category", category.name()
                )));

        String content = FormText.block(category.content(), player, placeholders.claimInfo(claim));
        if (!content.isEmpty()) {
            builder.label(content);
        }

        List<Row> rows = new ArrayList<>();

        for (Map.Entry<String, ClaimSettingsFormConfig.SettingEntry> entry : category.settings().entrySet()) {
            ClaimSettingsFormConfig.SettingEntry row = entry.getValue();
            if (row == null || row.key() == null) {
                continue;
            }

            Setting<?> setting = row.key();
            SettingHolder<Object> holder = holder(claim, setting);
            String key = entry.getKey();

            if (config.showDescriptions() && !row.description().isEmpty()) {
                builder.label(FormText.block(row.description(), player, Map.of()));
            }

            String label = FormText.line(row.label(), player, Map.of());
            Object value = holder.value();

            if (value instanceof Boolean current) {
                builder.toggle(key, label, current);
            } else if (setting.defaultValue() instanceof Boolean fallback) {
                builder.toggle(key, label, fallback);
            } else {
                builder.input(
                        key, label,
                        FormText.line(config.valueNotSet(), player),
                        value == null ? "" : value.toString()
                );
            }

            rows.add(new Row(key, setting, holder));
        }

        FormComponent.Dropdown navigation = withNavigation ? navigationDropdown(context) : null;
        builder.component(navigation);

        builder.onSubmit(values -> {
            for (Row row : rows) {
                apply(row, values.has(row.key()) ? values.values().get(row.key()) : null);
            }

            storageHolder.get().claims().saveSettings(claim);

            if (navigation != null) {
                Class<? extends BaseForm> target = navigationTarget(values.index(NAVIGATION_KEY, 0), context);

                if (target != null) {
                    context.navigateTo(target);
                    return;
                }

                context.refresh();
                return;
            }

            formService.back(player);
        });

        builder.onClose(() -> {
            if (!withNavigation) {
                formService.back(player);
            }
        });

        formService.send(player, builder.build());
    }

    private void apply(@NotNull Row row, Object submitted) {
        if (submitted instanceof Boolean value) {
            row.holder().value(value);
            return;
        }

        if (!(submitted instanceof String value)) {
            return;
        }

        String trimmed = value.trim();
        row.holder().value(trimmed.isEmpty() ? null : row.setting().parser().apply(trimmed));
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private SettingHolder<Object> holder(@NotNull Claim claim, @NotNull Setting<?> setting) {
        return (SettingHolder<Object>) claim.settings().computeIfAbsent(setting, key -> {
            SettingHolder<Object> created = (SettingHolder<Object>) SettingHolder.from(setting);

            String raw = defaultSettingsHolder.get().defaultSettings().getOrDefault(setting, null);
            Object value = raw != null ? setting.parser().apply(raw) : null;
            created.value(value == null || value.equals("null") ? null : value);

            return created;
        });
    }

    private record Row(String key, Setting<?> setting, SettingHolder<Object> holder) {
    }
}