package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingHolder;
import com.hibiscusmc.hmcclaims.config.DefaultSettings;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.gui.ClaimSettingsConfig;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.dialog.type.SingleInputDialog;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.gui.BaseGui;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
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
    private ConfigHolder<DefaultSettings> defaultSettingsHolder;

    @Inject
    private StorageHolder storageHolder;

    @Inject
    private GuiRegistry guis;

    @Inject
    private SchedulerUtil scheduler;

    private GuiTemplate.GuiTitle title;
    private int rows = 1;

    protected GuiTemplate.GuiScreenType screenType;

    private Map<String, GuiTemplate.SimpleIcon> tabs;

    private List<GuiTemplate.Icon> icons;

    private GuiTemplate.SimpleIcon previousPage;
    private GuiTemplate.SimpleIcon nextPage;

    private GuiTemplate.SimpleIcon backIcon;

    private String enabledState;
    private String disabledState;

    private Map<Integer, List<ClaimSettingsConfig.ToggleSettingIcon<Setting<?>>>> settingPages;

    @Override
    public void loadConfig() {
        ClaimSettingsConfig config = configHolder.get();
        if (config == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        title = config.title();
        rows = config.rows();

        tabs = config.tabs();

        icons = config.extraIcons().values().stream().toList();

        previousPage = config.pages().get("previous-page");
        nextPage = config.pages().get("next-page");

        backIcon = config.backIcon();

        enabledState = config.states().get(true);
        disabledState = config.states().get(false);

        settingPages = config.settingPages();

        screenType = config.screenType();
        if (screenType == GuiTemplate.GuiScreenType.FULL) {
            super.loadConfig(config.lowerGui());
        }
    }

    @Override
    public void open(@NotNull Player player, @NotNull GuiMetadata metadata) {
        Claim claim = metadata.claim();
        int currentPage = metadata.currentPage();

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
                    structure, currentClass, claim, metadata,
                    tabs
            );

            structure.set(previousPage.slot(), '(');
            structure.set(nextPage.slot(), ')');

            if (isValidIcon(backIcon)) {
                structure.set(backIcon.slot(), '$');
            }

            Char2ObjectArrayMap<Item> settingItems = new Char2ObjectArrayMap<>();
            int currentSafeCode = FIRST_SAFE_CHAR + icons.size() + 1;
            for (ClaimSettingsConfig.ToggleSettingIcon<Setting<?>> toggleIcon : settingPages.getOrDefault(currentPage, List.of())) {
                SettingItem item = buildSetting(player, claim, toggleIcon);

                settingItems.put((char) currentSafeCode, item.item());
                structure.set(toggleIcon.slot(), (char) currentSafeCode++);

                if (toggleIcon.hasModifyIcon()) {
                    settingItems.put((char) currentSafeCode, item.modifyItem());
                    structure.set(toggleIcon.modifyIcon().slot(), (char) currentSafeCode++);
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
                        .setItemProvider(TextUtil.parseItemPlaceholders(icon.item(), player))
                        .addClickHandler(click -> (switch (click.clickType()) {
                            case LEFT -> icon.leftClickActions();
                            case RIGHT -> icon.rightClickActions();
                            default -> List.<Action>of();
                        }).forEach(action -> action.execute(player)))
                        .build());
            }

            tabsBuilder.accept(gui, guis, player);

            for (Char2ObjectMap.Entry<Item> entry : settingItems.char2ObjectEntrySet()) {
                gui.addIngredient(entry.getCharKey(), entry.getValue());
            }

            Gui lowerGui = metadata.claimsGui() != null ? metadata.claimsGui() : screenType == GuiTemplate.GuiScreenType.FULL ? buildLowerGui(player, metadata) : null;
            if (metadata.claimsGui() == null) {
                metadata.claimsGui(lowerGui);
            }

            gui.addIngredient('(', Item.builder()
                    .setItemProvider(TextUtil.parseItemPlaceholders(previousPage.item(), player))
                    .addClickHandler(click -> {
                        int page = currentPage - 1;
                        if (!settingPages.containsKey(page)) {
                            return;
                        }

                        open(player, metadata.currentPage(page));
                    })
                    .build());

            gui.addIngredient(')', Item.builder()
                    .setItemProvider(TextUtil.parseItemPlaceholders(nextPage.item(), player))
                    .addClickHandler(click -> {
                        int page = currentPage + 1;
                        if (!settingPages.containsKey(page)) {
                            return;
                        }

                        open(player, metadata.currentPage(page));
                    })
                    .build());

            if (isValidIcon(backIcon)) {
                gui.addIngredient('$', Item.builder()
                        .setItemProvider(TextUtil.parseItemPlaceholders(backIcon.item(), player))
                        .addClickHandler(click -> guis.get(ClaimListGui.class).open(player))
                        .build()
                );
            }

            Gui upperGui = gui.build();

            scheduler.schedule(() -> {
                Window.Builder.Normal.Split window = Window.builder()
                        .setTitle(TextUtil.parse(title.text(), player, Map.of(
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

    private SettingItem buildSetting(@NotNull Player player, @NotNull Claim claim, @NotNull ClaimSettingsConfig.ToggleSettingIcon<Setting<?>> toggleIcon) {
        Setting<?> setting = toggleIcon.key();
        //noinspection unchecked
        SettingHolder<Object> holder = (SettingHolder<Object>) claim.settings()
                .computeIfAbsent(setting, (k) -> {
                    //noinspection unchecked
                    SettingHolder<Object> h = (SettingHolder<Object>) SettingHolder.from(setting);
                    h.value(defaultSettingsHolder.get().defaultSettings().getOrDefault(setting, null));
                    return h;
                });

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
            ItemStack settingItem = toggleIcon.icon().item();
            if (settingItem.hasItemMeta()) {
                ItemMeta meta = settingItem.getItemMeta();
                if (meta.hasItemName()) {
                    settingName = TextUtil.unparse(meta.itemName());
                }
            }

            new SingleInputDialog()
                    .create(
                            messagesHolder.get().dialogs().setting(),
                            Map.of("claim_name", claim.name()), Map.of("setting_name", settingName),
                            holder.value() != null ? holder.value().toString() : holder.setting().defaultValue().toString()
                    )
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
                        .setItemProvider(p -> new ItemWrapper(TextUtil.parseItemPlaceholders(buildSettingIcon(holder, toggleIcon.icon(), toggleIcon.notSet()), player)))
                        .addClickHandler((it, click) -> {
                            if (toggleIcon.hasModifyIcon()) {
                                return;
                            }

                            action.accept(it, click);
                        })
                        .build(),
                toggleIcon.hasModifyIcon() ?
                        Item.builder()
                                .setItemProvider(p -> {
                                    Object value = holder.value();

                                    GuiTemplate.ToggleIcon.BiStateToggleIcon modifyIcon = toggleIcon.modifyIcon();

                                    GuiTemplate.DynamicIconWithStack icon;
                                    if (value instanceof Boolean ? (Boolean) value : value != null) {
                                        icon = modifyIcon.enabled();
                                    } else {
                                        icon = modifyIcon.disabled();
                                    }

                                    return new ItemWrapper(TextUtil.parseItemPlaceholders(buildSettingIcon(holder, icon, toggleIcon.notSet()), player));
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
                    "setting_value", holder.value() != null ? holder.value() instanceof Boolean ?
                            ((Boolean) holder.value() ? enabledState : disabledState)
                            : holder.value().toString() : notSetArg
            )));
        });

        return stack;
    }

    record SettingItem(Item item, Item modifyItem) {
    }
}