package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingHolder;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.gui.ClaimSettingsConfig;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.dialog.type.SettingDialog;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.gui.BaseGui;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.List;
import java.util.Map;

@Singleton
@SuppressWarnings({"UnstableApiUsage"})
public class ClaimSettingsGui implements BaseGui {

    @Inject
    private ConfigHolder<ClaimSettingsConfig> configHolder;
    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private StorageHolder storageHolder;

    @Inject
    private GuiRegistry guis;

    @Inject
    private SchedulerUtil scheduler;

    private GuiTemplate.GuiTitle title;
    private int rows = 1;

    private GuiTemplate.SimpleIcon backIcon;
    private GuiTemplate.SimpleIcon deleteIcon;

    private GuiTemplate.SimpleIcon membersTab;
    private GuiTemplate.SimpleIcon rolesTab;
    private GuiTemplate.SimpleIcon settingsTab;
    private GuiTemplate.SimpleIcon manageTab;

    private List<GuiTemplate.Icon> icons;

    private GuiTemplate.SimpleIcon previousPage;
    private GuiTemplate.SimpleIcon nextPage;

    private Map<Integer, List<ClaimSettingsConfig.SettingIcon<?>>> settingPages;

    @Override
    public void loadConfig() {
        ClaimSettingsConfig config = configHolder.get();
        if (config == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        title = config.title();
        rows = config.rows();

        backIcon = config.backIcon();
        deleteIcon = config.deleteIcon();

        membersTab = config.tabs().get("members-tab");
        rolesTab = config.tabs().get("roles-tab");
        settingsTab = config.tabs().get("settings-tab");
        manageTab = config.tabs().get("manage-tab");

        icons = config.extraIcons().values().stream().toList();

        previousPage = config.pages().get("previous-page");
        nextPage = config.pages().get("next-page");

        settingPages = config.settingPages();
    }

    @Override
    public void open(@NotNull Player player, Object... args) {
        Claim claim = (Claim) args[0];
        int currentPage = args.length > 1 ? (int) args[1] : 1;

        Gui gui = Gui.gui()
                .title(TextUtil.parse(title.text(), Map.of(
                        "claim_name", parseName(claim.name(), title.maxLength())
                )))
                .rows(rows)
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(action -> {
            Storage storage = storageHolder.get();

            storage.claims().saveSettings(claim);
        });

        scheduler.scheduleAsync(() -> {
            buildIcons(player, gui, claim, currentPage);

            scheduler.schedule(() -> gui.open(player));
        });
    }

    private void buildIcons(@NotNull Player player, @NotNull Gui gui, @NotNull Claim claim, int currentPage) {
        for (GuiTemplate.Icon icon : icons) {
            gui.setItem(icon.slot(), new GuiItem(icon.item(), action -> {
                for (Action iconAction : action.isLeftClick() ? icon.leftClickActions() : icon.rightClickActions()) {
                    iconAction.execute(player);
                }
            }));
        }

        gui.setItem(membersTab.slot(), new GuiItem(membersTab.item(), action -> guis.get(ClaimMemberListGui.class)
                .open(player, claim)));
        gui.setItem(rolesTab.slot(), new GuiItem(rolesTab.item(), action -> player.sendRichMessage("<green>viewing roles")));
        gui.setItem(settingsTab.slot(), new GuiItem(settingsTab.item()));
        gui.setItem(manageTab.slot(), new GuiItem(manageTab.item(), action -> {
            BaseGui tab = claim.main() == null ? guis.get(ClaimManageGui.class) : guis.get(SubClaimManageGui.class);

            tab.open(player, claim);
        }));

        gui.setItem(deleteIcon.slot(), new GuiItem(deleteIcon.item(), action -> player.sendRichMessage("<green>viewing delete")));

        gui.setItem(backIcon.slot(), new GuiItem(backIcon.item(), action ->
                guis.get(ClaimListGui.class).open(player)
        ));

        for (ClaimSettingsConfig.SettingIcon<?> settingIcon : settingPages.getOrDefault(currentPage, List.of())) {
            buildSetting(player, gui, claim, settingIcon);
        }

        gui.setItem(previousPage.slot(), new GuiItem(previousPage.item(), action -> {
            if (currentPage > 1) {
                open(player, claim, currentPage - 1);
            }
        }));

        gui.setItem(nextPage.slot(), new GuiItem(nextPage.item(), action -> {
            if (currentPage < settingPages.size()) {
                open(player, claim, currentPage + 1);
            }
        }));
    }

    private void buildSetting(@NotNull Player player, @NotNull Gui gui, @NotNull Claim claim, @NotNull ClaimSettingsConfig.SettingIcon<?> settingIcon) {
        Runnable action = () -> {
            Setting<?> setting = settingIcon.setting();
            //noinspection unchecked
            SettingHolder<Object> holder = (SettingHolder<Object>) claim.settings()
                    .computeIfAbsent(setting, (k) -> SettingHolder.from(setting));

            boolean shouldUpdate = false;

            Object holderValue = holder.value();
            if (holderValue instanceof Boolean value) {
                holder.value(!value);
                shouldUpdate = true;
            } else if (holderValue != null) {
                holder.value(null);
                shouldUpdate = true;
            }

            if (shouldUpdate) {
                buildSetting(player, gui, claim, settingIcon);
                gui.update();
                return;
            }

            String settingName = setting.displayName();
            ItemStack settingItem = settingIcon.icon().item();
            if (settingItem.hasItemMeta()) {
                ItemMeta meta = settingItem.getItemMeta();
                if (meta.hasCustomName()) {
                    settingName = TextUtil.unparse(meta.customName());
                }
            }

            new SettingDialog()
                    .create(messagesHolder.get().dialogs(), claim.name(), settingName, holder.value() != null ? holder.value() : holder.setting().defaultValue())
                    .onSubmit(view -> {
                        String newValue = view.getText("input");
                        if (newValue == null) {
                            return;
                        }

                        holder.value(setting.parser().apply(newValue));

                        open(player, claim);
                    })
                    .show(player);
        };

        gui.setItem(settingIcon.icon().slot(), new GuiItem(settingIcon.icon().item(), event -> {
            if (!settingIcon.hasModifyIcon()) {
                action.run();
            }
        }));

        if (settingIcon.hasModifyIcon()) {
            Setting<?> setting = settingIcon.setting();

            //noinspection unchecked
            SettingHolder<Object> holder = (SettingHolder<Object>) claim.settings().getOrDefault(setting, SettingHolder.from(setting));
            Object value = holder.value();

            ClaimSettingsConfig.SettingIcon.BooleanSettingIcon modifyIcon = settingIcon.modifyIcon();

            if (value instanceof Boolean ? (Boolean) value : value != null) {
                gui.setItem(modifyIcon.slot(), new GuiItem(buildSettingModifyIcon(holder, modifyIcon.enabled(), modifyIcon.notSet()), event -> action.run()));
            } else {
                gui.setItem(modifyIcon.slot(), new GuiItem(buildSettingModifyIcon(holder, modifyIcon.disabled(), modifyIcon.notSet()), event -> action.run()));
            }
        }
    }

    private ItemStack buildSettingModifyIcon(@NotNull SettingHolder<?> holder, @NotNull GuiTemplate.DynamicIconWithStack icon, String notSetArg) {
        ItemStack stack = icon.item();
        stack.editMeta(meta -> {
            meta.itemName(TextUtil.parse(icon.name()));

            meta.lore(TextUtil.parseItemLore(icon.lore(), Map.of(
                    "setting_value", holder.value() != null ? holder.value().toString() : notSetArg
            )));
        });

        return stack;
    }
}