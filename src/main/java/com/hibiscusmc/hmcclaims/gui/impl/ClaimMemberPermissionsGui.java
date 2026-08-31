package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.config.gui.ClaimMemberPermissionsConfig;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.window.Window;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

@Singleton
public class ClaimMemberPermissionsGui extends ClaimMemberManageGui {

    @Inject
    private ConfigHolder<ClaimMemberPermissionsConfig> configHolder;

    @Inject
    private StorageHolder storageHolder;

    @Inject
    private GuiRegistry guis;

    @Inject
    private SchedulerUtil scheduler;

    private GuiTemplate.GuiTitle title;
    private int rows = 1;

    private String enabledState;
    private String unsetState;
    private String disabledState;

    private Int2ObjectMap<Set<ClaimMemberPermissionsConfig.TogglePermissionIcon<Permission>>> permissionPages;

    private GuiTemplate.SimpleIcon kickIcon;
    private ItemStack cantKickIcon;

    private GuiTemplate.SimpleIcon banIcon;
    private ItemStack cantBanIcon;

    private GuiTemplate.SimpleMultiIcon rolesTab;
    private GuiTemplate.SimpleMultiIcon permissionsTab;

    private GuiTemplate.SimpleIcon previousPage;
    private GuiTemplate.SimpleIcon nextPage;

    private GuiTemplate.SimpleIcon backIcon;

    private Int2ObjectMap<Item> extraItems;

    private GuiTemplate.GuiScreenType screenType;

    @Override
    public void loadConfig() {
        ClaimMemberPermissionsConfig config = configHolder.get();
        if (config == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        title = config.title();
        rows = config.rows();

        enabledState = config.states().get("enabled");
        unsetState = config.states().get("unset");
        disabledState = config.states().get("disabled");

        permissionPages = new Int2ObjectArrayMap<>();
        for (Map.Entry<Integer, Map<String, ClaimMemberPermissionsConfig.TogglePermissionIcon<Permission>>> page : config.permissionPages().entrySet()) {
            permissionPages.put(page.getKey().intValue(), new HashSet<>(page.getValue().values()));
        }

        kickIcon = config.kickIcon();
        cantKickIcon = config.cantKickIcon();

        banIcon = config.banIcon();
        cantBanIcon = config.cantBanIcon();

        rolesTab = config.tabs().get("roles-tab");
        permissionsTab = config.tabs().get("permissions-tab");

        previousPage = config.pages().get("previous-page");
        nextPage = config.pages().get("next-page");

        backIcon = config.backIcon();

        extraItems = parseExtraItems(config.extraIcons());

        screenType = config.screenType();
        if (screenType == GuiTemplate.GuiScreenType.FULL) {
            loadConfig(config.lowerGui());
        }
    }

    @Override
    public void open(@NotNull Player player, @NotNull GuiMetadata metadata) {
        Claim claim = metadata.claim();
        ClaimMember member = metadata.member();
        int currentPage = metadata.currentPage();

        scheduler.scheduleAsync(() -> {
            Gui.Builder<?, ?> builder = Gui.builder();

            CharList structure = new CharArrayList();
            for (int i = 0; i < rows * 9; i++) {
                structure.add((char) FIRST_SAFE_CHAR);
            }

            structure.set(previousPage.slot(), (char) (FIRST_SAFE_CHAR + 4));
            structure.set(nextPage.slot(), (char) (FIRST_SAFE_CHAR + 5));

            if (isValidIcon(kickIcon)) {
                structure.set(kickIcon.slot(), (char) (FIRST_SAFE_CHAR + 6));
            }

            if (isValidIcon(banIcon)) {
                structure.set(banIcon.slot(), (char) (FIRST_SAFE_CHAR + 7));
            }

            if (isValidIcon(backIcon)) {
                structure.set(backIcon.slot(), (char) (FIRST_SAFE_CHAR + 8));
            }

            int currentPoint = FIRST_SAFE_CHAR + 9;
            Char2ObjectMap<Item> tabsMap = new Char2ObjectOpenHashMap<>();
            for (int slot : rolesTab.slots()) {
                structure.set(slot, (char) currentPoint);

                tabsMap.put((char) currentPoint, Item.builder()
                        .setItemProvider(TextUtil.parseItemPlaceholders(rolesTab.item(), player))
                        .addClickHandler(click -> guis.get(ClaimMemberRoleGui.class).open(player, metadata))
                        .build());
                currentPoint++;
            }
            for (int slot : permissionsTab.slots()) {
                structure.set(slot, (char) currentPoint);

                tabsMap.put((char) currentPoint, Item.builder()
                        .setItemProvider(TextUtil.parseItemPlaceholders(permissionsTab.item(), player))
                        .build());
                currentPoint++;
            }

            Char2ObjectMap<Item> itemMap = new Char2ObjectOpenHashMap<>();
            for (Int2ObjectMap.Entry<Item> extraItem : extraItems.int2ObjectEntrySet()) {
                structure.set(extraItem.getIntKey(), (char) currentPoint);

                itemMap.put((char) currentPoint, extraItem.getValue());
                currentPoint++;
            }

            Set<ClaimMemberPermissionsConfig.TogglePermissionIcon<Permission>> currentPermissions =
                    permissionPages.getOrDefault(currentPage, Set.of());
            Char2ObjectArrayMap<Item> permissionItems = new Char2ObjectArrayMap<>();

            for (ClaimMemberPermissionsConfig.TogglePermissionIcon<Permission> toggleIcon : currentPermissions) {
                PermissionItem item = buildPermissionItem(player, member, toggleIcon, metadata);

                permissionItems.put((char) currentPoint, item.item());
                structure.set(toggleIcon.slot(), (char) currentPoint++);

                if (toggleIcon.hasModifyIcon()) {
                    permissionItems.put((char) currentPoint, item.modifyItem());
                    structure.set(toggleIcon.modifyIcon().slot(), (char) currentPoint++);
                }
            }

            builder.setStructure(parseStructure(structure, rows));

            tabsMap.char2ObjectEntrySet().forEach(entry -> builder.addIngredient(
                    entry.getCharKey(), entry.getValue()
            ));

            itemMap.char2ObjectEntrySet().forEach(entry -> builder.addIngredient(
                    entry.getCharKey(), entry.getValue()
            ));

            permissionItems.char2ObjectEntrySet().forEach(entry -> builder.addIngredient(
                    entry.getCharKey(), entry.getValue()
            ));

            builder.addIngredient((char) (FIRST_SAFE_CHAR + 4), Item.builder()
                    .setItemProvider(new ItemBuilder(TextUtil.parseItemPlaceholders(previousPage.item(), player)))
                    .addClickHandler((item, click) -> {
                        int page = currentPage - 1;
                        if (!permissionPages.containsKey(page)) {
                            return;
                        }

                        open(player, metadata.currentPage(page));
                    })
                    .build());
            builder.addIngredient((char) (FIRST_SAFE_CHAR + 5), Item.builder()
                    .setItemProvider(new ItemBuilder(TextUtil.parseItemPlaceholders(nextPage.item(), player)))
                    .addClickHandler((item, click) -> {
                        int page = currentPage + 1;
                        if (!permissionPages.containsKey(page)) {
                            return;
                        }

                        open(player, metadata.currentPage(page));
                    })
                    .build());

            if (isValidIcon(kickIcon)) {
                builder.addIngredient((char) (FIRST_SAFE_CHAR + 6), buildKickItem(player, claim, member, kickIcon, cantKickIcon));
            }

            if (isValidIcon(banIcon)) {
                builder.addIngredient((char) (FIRST_SAFE_CHAR + 7), buildBanItem(player, claim, member, banIcon, cantBanIcon));
            }

            if (isValidIcon(backIcon)) {
                builder.addIngredient((char) (FIRST_SAFE_CHAR + 8), Item.builder()
                        .setItemProvider(TextUtil.parseItemPlaceholders(backIcon.item(), player))
                        .addClickHandler(click -> guis.get(ClaimMemberListGui.class).open(player, metadata))
                        .build());
            }

            Gui lowerGui = screenType == GuiTemplate.GuiScreenType.FULL ? build(player, metadata) : null;

            scheduler.schedule(() -> {
                Window.Builder.Normal.Split window = Window.builder()
                        .setTitle(TextUtil.parse(title.text(), player, Map.of(
                                "member_name", parseName(member.lastKnownName(), title.maxLength())
                        )))
                        .setUpperGui(builder)
                        .setFallbackWindow(metadata.previousPage())
                        .addCloseHandler(reason -> {
                            Storage storage = storageHolder.get();

                            storage.claims().saveMembers(claim);
                        });

                if (lowerGui != null) {
                    window.setLowerGui(lowerGui);
                }

                window.open(player);
            });
        });
    }

    @NotNull
    @Contract(pure = true)
    private PermissionItem buildPermissionItem(@NotNull Player player, @NotNull ClaimMember target, @NotNull ClaimMemberPermissionsConfig.TogglePermissionIcon<Permission> toggleIcon, @NotNull GuiMetadata metadata) {
        Claim claim = metadata.claim();
        Permission permission = toggleIcon.key();

        boolean hasPermission = metadata.claim().hasPermission(player.getUniqueId(), Permission.MANAGE_MEMBER_PERMISSIONS) &&
                claim.getMember(player.getUniqueId())
                        .map(member -> member.canManage(target))
                        .orElse(false);

        BiConsumer<Item, Click> action = (it, click) -> {
            if (!hasPermission) {
                return;
            }

            if (click.clickType() == ClickType.DOUBLE_CLICK) {
                return;
            }

            Object2BooleanMap<Permission> permissions = target.permissions();

            if (!permissions.containsKey(permission)) {
                permissions.put(permission, true);
            } else {
                boolean enabled = permissions.getBoolean(permission);

                if (enabled) {
                    permissions.put(permission, false);
                } else {
                    permissions.removeBoolean(permission);
                }
            }

            it.notifyWindows();
        };

        return new PermissionItem(
                Item.builder()
                        .setItemProvider(p -> {
                            Object2BooleanMap<Permission> permissions = target.permissions();
                            String state;

                            if (!permissions.containsKey(permission)) {
                                state = unsetState;
                            } else {
                                state = permissions.getBoolean(permission) ? enabledState : disabledState;
                            }

                            return new ItemWrapper(TextUtil.parseItemPlaceholders(buildPermissionIcon(
                                    hasPermission ? toggleIcon.icon() : toggleIcon.noPermsIcon(),
                                    state
                            ), player));
                        })
                        .addClickHandler((it, click) -> {
                            if (!toggleIcon.hasModifyIcon()) {
                                action.accept(it, click);
                            }
                        })
                        .build(),
                toggleIcon.hasModifyIcon() ?
                        Item.builder()
                                .setItemProvider(p -> {
                                    ClaimMemberPermissionsConfig.TogglePermissionIcon.TriStateToggleIcon modifyIcon = toggleIcon.modifyIcon();
                                    Object2BooleanMap<Permission> permissions = target.permissions();
                                    ClaimMemberPermissionsConfig.PermissionIcon icon;
                                    String state;

                                    if (!permissions.containsKey(permission)) {
                                        icon = modifyIcon.unset();
                                        state = unsetState;
                                    } else {
                                        icon = permissions.getBoolean(permission) ? modifyIcon.enabled() : modifyIcon.disabled();
                                        state = permissions.getBoolean(permission) ? enabledState : disabledState;
                                    }

                                    return new ItemWrapper(TextUtil.parseItemPlaceholders(buildPermissionIcon(icon, hasPermission, state), player));
                                })
                                .addClickHandler(action)
                                .build()
                        : null
        );
    }

    @NotNull
    @Contract(pure = true)
    private ItemStack buildPermissionIcon(@NotNull GuiTemplate.DynamicIconWithStack icon, @NotNull String state) {
        ItemStack stack = icon.item();
        stack.editMeta(meta -> {
            meta.itemName(TextUtil.parse(icon.name()));
            meta.lore(TextUtil.parseItemLore(icon.lore(), Map.of("permission_value", state)));
        });

        return stack;
    }

    @NotNull
    @Contract(pure = true)
    private ItemStack buildPermissionIcon(@NotNull ClaimMemberPermissionsConfig.PermissionIcon icon, boolean hasPermission, @NotNull String state) {
        ItemStack stack = icon.item();
        stack.editMeta(meta -> {
            meta.itemName(TextUtil.parse(icon.name()));

            meta.lore(TextUtil.parseItemLore(
                    hasPermission ? icon.lore() : icon.cantChangeLore(),
                    Map.of("permission_value", state)
            ));
        });

        return stack;
    }

    record PermissionItem(@NotNull Item item, @Nullable Item modifyItem) {
    }
}