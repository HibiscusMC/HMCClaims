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
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.util.TriConsumer;
import xyz.xenondevs.invui.window.Window;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Singleton
@SuppressWarnings({"UnstableApiUsage"})
public class ClaimSettingsGui extends ClaimListGui {

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

    protected GuiTemplate.GuiScreenType screenType;

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

        membersTab = config.tabs().get("members-tab");
        rolesTab = config.tabs().get("roles-tab");
        settingsTab = config.tabs().get("settings-tab");
        manageTab = config.tabs().get("manage-tab");

        icons = config.extraIcons().values().stream().toList();

        previousPage = config.pages().get("previous-page");
        nextPage = config.pages().get("next-page");

        settingPages = config.settingPages();

        screenType = config.screenType();
        if (screenType == GuiTemplate.GuiScreenType.FULL) {
            super.loadConfig(config.lowerGui());
        }
    }

    @Override
    public void open(@NotNull Player player, Object... args) {
        Claim claim = (Claim) args[0];
        int currentPage = args.length > 1 ? (int) args[1] : 1;

        scheduler.scheduleAsync(() -> {
            Gui.Builder<?, ?> gui = Gui.builder();
            CharList structure = new CharArrayList();
            for (int i = 0; i < rows * 9; i++) {
                structure.add('#');
            }

            Char2ObjectArrayMap<GuiTemplate.Icon> mappedIcons = new Char2ObjectArrayMap<>();
            for (GuiTemplate.Icon icon : icons) {
                char codePoint = (char) (FIRST_SAFE_CHAR + icons.indexOf(icon));

                structure.set(icon.slot(), codePoint);
                mappedIcons.put(codePoint, icon);
            }

            Class<? extends BaseGui> currentClass = getClass();
            TriConsumer<Gui.Builder<?, ?>, GuiRegistry, Player> tabsBuilder = buildTabs(
                    structure, currentClass,
                    new TabIcon(ClaimMemberListGui.class, membersTab.item(), membersTab.slot(), claim),
                    new TabIcon(ClaimMemberListGui.class, rolesTab.item(), rolesTab.slot(), claim),
                    new TabIcon(ClaimSettingsGui.class, settingsTab.item(), settingsTab.slot(), claim),
                    new TabIcon(claim.main() == null ? ClaimManageGui.class : SubClaimManageGui.class, manageTab.item(), manageTab.slot(), claim)
            );

            structure.set(previousPage.slot(), '<');
            structure.set(nextPage.slot(), '>');

            Char2ObjectArrayMap<Item> settingItems = new Char2ObjectArrayMap<>();
            int currentSafeCode = FIRST_SAFE_CHAR + icons.size() + 1;
            for (ClaimSettingsConfig.SettingIcon<?> settingIcon : settingPages.getOrDefault(currentPage, List.of())) {
                SettingItem item = buildSetting(player, claim, settingIcon);

                settingItems.put((char) currentSafeCode, item.item());
                structure.set(settingIcon.slot(), (char) currentSafeCode++);

                if (settingIcon.hasModifyIcon()) {
                    settingItems.put((char) currentSafeCode, item.modifyItem());
                    structure.set(settingIcon.modifyIcon().slot(), (char) currentSafeCode++);
                }
            }

            String[] structureArray = new String[rows];
            for (int r = 0; r < rows; r++) {
                CharList rowList = structure.subList(r * 9, (r + 1) * 9);

                structureArray[r] = new String(rowList.toCharArray());
            }

            gui.setStructure(structureArray);

            for (Char2ObjectMap.Entry<GuiTemplate.Icon> entry : mappedIcons.char2ObjectEntrySet()) {
                GuiTemplate.Icon icon = entry.getValue();

                gui.addIngredient(entry.getCharKey(), Item.builder()
                        .setItemProvider(icon.item())
                        .addClickHandler(click -> {
                            for (Action iconAction : click.clickType() == ClickType.LEFT ?
                                    icon.leftClickActions() :
                                    icon.rightClickActions()) {
                                iconAction.execute(player);
                            }
                        })
                        .build());
            }

            tabsBuilder.accept(gui, guis, player);

            for (Char2ObjectMap.Entry<Item> entry : settingItems.char2ObjectEntrySet()) {
                gui.addIngredient(entry.getCharKey(), entry.getValue());
            }

            Gui lowerGui = screenType == GuiTemplate.GuiScreenType.FULL ? buildLowerGui(player) : null;
            Gui upperGui = gui.build();

            scheduler.schedule(() -> {
                Window.Builder.Normal.Split window = Window.builder()
                        .setTitle(TextUtil.parse(title.text(), Map.of(
                                "claim_name", parseName(claim.name(), title.maxLength())
                        )))
                        .setUpperGui(upperGui)
                        .addCloseHandler(reason -> {
                            Storage storage = storageHolder.get();

                            storage.claims().saveSettings(claim);
                        });

                if (lowerGui != null) {
                    window.setLowerGui(lowerGui);
                }

                window.open(player);
            });
        });
    }

    private SettingItem buildSetting(@NotNull Player player, @NotNull Claim claim, @NotNull ClaimSettingsConfig.SettingIcon<?> settingIcon) {
        Setting<?> setting = settingIcon.setting();
        //noinspection unchecked
        SettingHolder<Object> holder = (SettingHolder<Object>) claim.settings()
                .computeIfAbsent(setting, (k) -> SettingHolder.from(setting));

        BiConsumer<Item, Click> action = (it, click) -> {
            if (click.clickType() == ClickType.DOUBLE_CLICK) {
                return;
            }

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
                it.notifyWindows();
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

                        it.notifyWindows();
                    })
                    .show(player);
        };

        return new SettingItem(
                Item.builder()
                        .setItemProvider(p -> new ItemWrapper(buildSettingIcon(holder, settingIcon.icon(), settingIcon.notSet())))
                        .addClickHandler((it, click) -> {
                            if (settingIcon.hasModifyIcon()) {
                                return;
                            }

                            action.accept(it, click);
                        })
                        .build(),
                settingIcon.hasModifyIcon() ?
                        Item.builder()
                                .setItemProvider(p -> {
                                    Object value = holder.value();

                                    ClaimSettingsConfig.SettingIcon.BooleanSettingIcon modifyIcon = settingIcon.modifyIcon();

                                    GuiTemplate.DynamicIconWithStack icon;
                                    if (value instanceof Boolean ? (Boolean) value : value != null) {
                                        icon = modifyIcon.enabled();
                                    } else {
                                        icon = modifyIcon.disabled();
                                    }

                                    return new ItemWrapper(buildSettingIcon(holder, icon, settingIcon.notSet()));
                                })
                                .addClickHandler(action)
                                .build()
                        : null
        );
    }

    private ItemStack buildSettingIcon(@NotNull SettingHolder<?> holder, @NotNull GuiTemplate.DynamicIconWithStack icon, String notSetArg) {
        ItemStack stack = icon.item();
        stack.editMeta(meta -> {
            meta.itemName(TextUtil.parse(icon.name()));

            meta.lore(TextUtil.parseItemLore(icon.lore(), Map.of(
                    "setting_value", holder.value() != null ? holder.value().toString() : notSetArg
            )));
        });

        return stack;
    }

    record SettingItem(Item item, Item modifyItem) {
    }
}