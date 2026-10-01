package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingHolder;
import com.hibiscusmc.hmcclaims.config.ClaimSettings;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.gui.ClaimSettingsConfig;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.dialog.type.SingleInputDialog;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.gui.BaseGui;
import com.hibiscusmc.hmcclaims.gui.EntryIcon;
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
import org.jetbrains.annotations.NotNull;
import team.hypox.config.core.ConfigHolder;
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
    private ConfigHolder<ClaimSettings> claimSettingsHolder;

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

    private ClaimSettings claimSettings;
    private ClaimSettingsConfig.SettingList settingList;
    private int[] settingSlots;
    private List<List<Setting<?>>> settingPages;

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

        claimSettings = claimSettingsHolder.get();
        settingList = config.settingList();
        settingSlots = settingList.allSlots();
        settingPages = settingList.pages(claimSettings.visible());

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
            List<Setting<?>> currentSettings = currentPage >= 1 && currentPage <= settingPages.size()
                    ? settingPages.get(currentPage - 1)
                    : List.of();

            for (int i = 0; i < currentSettings.size(); i++) {
                int slot = settingSlots[i];
                SettingItem item = buildSetting(player, claim, currentSettings.get(i));

                settingItems.put((char) currentSafeCode, item.item());
                structure.set(slot, (char) currentSafeCode++);

                int toggleSlot = slot + settingList.toggleOffset();
                if (settingList.hasToggle() && toggleSlot < structure.size()) {
                    settingItems.put((char) currentSafeCode, item.modifyItem());
                    structure.set(toggleSlot, (char) currentSafeCode++);
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
                        if (page < 1 || page > settingPages.size()) {
                            return;
                        }

                        open(player, metadata.currentPage(page));
                    })
                    .build());

            gui.addIngredient(')', Item.builder()
                    .setItemProvider(TextUtil.parseItemPlaceholders(nextPage.item(), player))
                    .addClickHandler(click -> {
                        int page = currentPage + 1;
                        if (page < 1 || page > settingPages.size()) {
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

    private SettingItem buildSetting(@NotNull Player player, @NotNull Claim claim, @NotNull Setting<?> setting) {
        ClaimSettings.Entry entry = claimSettings.entry(setting);

        //noinspection unchecked
        SettingHolder<Object> holder = (SettingHolder<Object>) claim.settings()
                .computeIfAbsent(setting, (k) -> {
                    //noinspection unchecked
                    SettingHolder<Object> h = (SettingHolder<Object>) SettingHolder.from(setting);
                    h.value(claimSettings.defaultValue(setting));

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

            new SingleInputDialog()
                    .create(
                            messagesHolder.get().dialogs().setting(),
                            Map.of("claim_name", claim.name()), Map.of("setting_name", entry.name()),
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
                        .setItemProvider(p -> new ItemWrapper(TextUtil.parseItemPlaceholders(EntryIcon.build(
                                entry.icon(), settingList.icon().name(), settingList.icon().lore(),
                                "setting", entry.name(), entry.description(),
                                Map.of("setting_value", valueOf(holder))
                        ), player)))
                        .addClickHandler((it, click) -> {
                            if (settingList.hasToggle()) {
                                return;
                            }

                            action.accept(it, click);
                        })
                        .build(),
                settingList.hasToggle() ?
                        Item.builder()
                                .setItemProvider(p -> {
                                    Object value = holder.value();

                                    GuiTemplate.DynamicIconWithStack icon;
                                    if (value instanceof Boolean ? (Boolean) value : value != null) {
                                        icon = settingList.toggle().enabled();
                                    } else {
                                        icon = settingList.toggle().disabled();
                                    }

                                    return new ItemWrapper(TextUtil.parseItemPlaceholders(EntryIcon.build(
                                            icon.item(), icon.name(), icon.lore(),
                                            "setting", entry.name(), entry.description(),
                                            Map.of("setting_value", valueOf(holder))
                                    ), player));
                                })
                                .addClickHandler(action)
                                .build()
                        : null
        );
    }

    @NotNull
    private String valueOf(@NotNull SettingHolder<?> holder) {
        Object value = holder.value();

        if (value == null) {
            return settingList.notSet();
        }

        if (value instanceof Boolean enabled) {
            return enabled ? enabledState : disabledState;
        }

        return value.toString();
    }

    record SettingItem(Item item, Item modifyItem) {
    }
}