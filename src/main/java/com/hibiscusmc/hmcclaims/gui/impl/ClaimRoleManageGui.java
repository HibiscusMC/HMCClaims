package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRoleRegistry;
import com.hibiscusmc.hmcclaims.config.gui.ClaimRoleManageConfig;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
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
public class ClaimRoleManageGui extends ClaimListGui {

    @Inject
    private ConfigHolder<ClaimRoleManageConfig> configHolder;

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

    private GuiTemplate.SimpleIcon deleteIcon;
    private ItemStack cantDeleteIcon;

    private List<GuiTemplate.Icon> icons;

    private GuiTemplate.SimpleIcon previousPage;
    private GuiTemplate.SimpleIcon nextPage;

    private GuiTemplate.SimpleIcon backIcon;

    private String enabledState;
    private String disabledState;

    private Map<Integer, Map<String, ClaimRoleManageConfig.TogglePermissionIcon<Permission>>> permissionPages;

    @Override
    public void loadConfig() {
        ClaimRoleManageConfig config = configHolder.get();
        if (config == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        title = config.title();
        rows = config.rows();

        tabs = config.tabs();

        deleteIcon = config.deleteIcon();
        cantDeleteIcon = config.cantDeleteIcon();

        icons = config.extraIcons().values().stream().toList();

        previousPage = config.pages().get("previous-page");
        nextPage = config.pages().get("next-page");

        permissionPages = config.permissionPages();

        backIcon = config.backIcon();

        enabledState = config.states().get("enabled");
        disabledState = config.states().get("disabled");

        screenType = config.screenType();
        if (screenType == GuiTemplate.GuiScreenType.FULL) {
            super.loadConfig(config.lowerGui());
        }
    }

    @Override
    public void open(@NotNull Player player, @NotNull GuiMetadata metadata) {
        Claim claim = metadata.claim();
        ClaimRole role = metadata.role();
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

            structure.set(deleteIcon.slot(), '*');
            structure.set(backIcon.slot(), '$');

            Char2ObjectArrayMap<Item> permissionItems = new Char2ObjectArrayMap<>();
            int currentSafeCode = FIRST_SAFE_CHAR + icons.size() + 1;
            for (ClaimRoleManageConfig.TogglePermissionIcon<Permission> toggleIcon : permissionPages.getOrDefault(currentPage, Map.of()).values()) {
                PermissionItem item = buildPermission(role, toggleIcon, metadata);

                permissionItems.put((char) currentSafeCode, item.item());
                structure.set(toggleIcon.slot(), (char) currentSafeCode++);

                if (toggleIcon.hasModifyIcon()) {
                    permissionItems.put((char) currentSafeCode, item.modifyItem());
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
                        .setItemProvider(icon.item())
                        .addClickHandler(click -> (switch (click.clickType()) {
                            case LEFT -> icon.leftClickActions();
                            case RIGHT -> icon.rightClickActions();
                            default -> List.<Action>of();
                        }).forEach(action -> action.execute(player)))
                        .build());
            }

            tabsBuilder.accept(gui, guis, player);

            for (Char2ObjectMap.Entry<Item> entry : permissionItems.char2ObjectEntrySet()) {
                gui.addIngredient(entry.getCharKey(), entry.getValue());
            }

            gui.addIngredient('*', Item.builder()
                    .setItemProvider(metadata.canManageRole() ? deleteIcon.item() : cantDeleteIcon)
                    .addClickHandler(click -> {
                        ClaimRoleRegistry registry = claim.roleRegistry();

                        if (!metadata.canManageRole()) {
                            return;
                        }

                        registry.remove(role);
                        guis.get(ClaimRolesGui.class)
                                .open(player, metadata);
                    })
                    .build());

            gui.addIngredient('(', Item.builder()
                    .setItemProvider(previousPage.item())
                    .addClickHandler(click -> {
                        int page = currentPage - 1;
                        if (!permissionPages.containsKey(page)) {
                            return;
                        }

                        open(player, metadata.currentPage(page));
                    })
                    .build());

            gui.addIngredient(')', Item.builder()
                    .setItemProvider(nextPage.item())
                    .addClickHandler(click -> {
                        int page = currentPage + 1;
                        if (!permissionPages.containsKey(page)) {
                            return;
                        }

                        open(player, metadata.currentPage(page));
                    })
                    .build());

            gui.addIngredient('$', Item.builder()
                    .setItemProvider(backIcon.item())
                    .addClickHandler(click -> guis.get(ClaimRolesGui.class).open(player, metadata))
                    .build());

            Gui lowerGui = metadata.claimsGui() != null ? metadata.claimsGui() : screenType == GuiTemplate.GuiScreenType.FULL ? buildLowerGui(player, metadata) : null;
            if (metadata.claimsGui() == null) {
                metadata.claimsGui(lowerGui);
            }

            Gui upperGui = gui.build();

            scheduler.schedule(() -> {
                Window.Builder.Normal.Split window = Window.builder()
                        .setTitle(TextUtil.parse(title.text(), Map.of(
                                "role_name", parseName(role.name(), title.maxLength())
                        )))
                        .setUpperGui(upperGui)
                        .setFallbackWindow(metadata.previousPage())
                        .addCloseHandler(reason -> {
                            Storage storage = storageHolder.get();

                            storage.claims().saveRoles(claim);
                        });

                if (lowerGui != null) {
                    window.setLowerGui(lowerGui);
                }

                window.open(player);
            });
        });
    }

    private PermissionItem buildPermission(@NotNull ClaimRole role, @NotNull ClaimRoleManageConfig.TogglePermissionIcon<Permission> toggleIcon, @NotNull GuiMetadata metadata) {
        Permission permission = toggleIcon.key();

        BiConsumer<Item, Click> action = (it, click) -> {
            if (click.clickType() == ClickType.DOUBLE_CLICK) {
                return;
            }

            if (!metadata.canManageRolePermissions()) {
                return;
            }

            if (role.hasPermission(permission)) {
                role.removePermission(permission);
            } else {
                role.addPermission(permission);
            }

            it.notifyWindows();
        };

        return new PermissionItem(
                Item.builder()
                        .setItemProvider(p -> new ItemWrapper(buildPermissionIcon(metadata.canManageRolePermissions() ? toggleIcon.icon() : toggleIcon.noPermsIcon(), role.hasPermission(permission))))
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
                                    ClaimRoleManageConfig.TogglePermissionIcon.BiStateToggleIcon modifyIcon = toggleIcon.modifyIcon();
                                    boolean hasPermission = role.hasPermission(permission);

                                    ClaimRoleManageConfig.PermissionIcon icon;
                                    if (hasPermission) {
                                        icon = modifyIcon.enabled();
                                    } else {
                                        icon = modifyIcon.disabled();
                                    }

                                    return new ItemWrapper(buildPermissionIcon(icon, metadata, hasPermission));
                                })
                                .addClickHandler(action)
                                .build()
                        : null
        );
    }

    private ItemStack buildPermissionIcon(@NotNull GuiTemplate.DynamicIconWithStack icon, boolean hasPermission) {
        ItemStack stack = icon.item();
        stack.editMeta(meta -> {
            meta.itemName(TextUtil.parse(icon.name()));

            meta.lore(TextUtil.parseItemLore(icon.lore(), Map.of(
                    "permission_value", hasPermission ? enabledState : disabledState
            )));
        });

        return stack;
    }

    private ItemStack buildPermissionIcon(@NotNull ClaimRoleManageConfig.PermissionIcon icon, @NotNull GuiMetadata metadata, boolean hasPermission) {
        ItemStack stack = icon.item();
        stack.editMeta(meta -> {
            meta.itemName(TextUtil.parse(icon.name()));

            meta.lore(TextUtil.parseItemLore(metadata.canManageRolePermissions() ? icon.lore() : icon.cantChangeLore(), Map.of(
                    "permission_value", hasPermission ? enabledState : disabledState
            )));
        });

        return stack;
    }

    record PermissionItem(Item item, Item modifyItem) {
    }
}