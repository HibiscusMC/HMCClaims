package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRoleRegistry;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.gui.ClaimRolesConfig;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.dialog.type.SingleInputDialog;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.gui.BaseGui;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.RangeUtil;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.StringUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.BoundItem;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.util.TriConsumer;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
@SuppressWarnings({"UnstableApiUsage"})
public class ClaimRolesGui extends ClaimListGui {

    @Inject
    private ConfigHolder<ClaimRolesConfig> configHolder;
    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private StorageHolder storageHolder;

    @Inject
    private GuiRegistry guis;

    @Inject
    private SchedulerUtil scheduler;

    private final List<Integer> slots = new ArrayList<>();
    private GuiTemplate.GuiTitle title;
    private int rows = 1;

    private GuiTemplate.GuiScreenType screenType;

    private ClaimRolesConfig.RoleIcon roleIcon;

    private GuiTemplate.SimpleIcon createRoleIcon;

    private GuiTemplate.SimpleIcon backIcon;

    private GuiTemplate.SimpleIcon previousPage;
    private GuiTemplate.SimpleIcon nextPage;

    private Map<String, GuiTemplate.SimpleIcon> tabs;

    private List<GuiTemplate.Icon> icons;

    @Override
    public void loadConfig() {
        ClaimRolesConfig config = configHolder.get();

        if (config == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        title = config.title();
        rows = config.rows();

        roleIcon = config.roleIcon();

        createRoleIcon = config.createRoleIcon();

        previousPage = config.pages().get("previous-page");
        nextPage = config.pages().get("next-page");

        tabs = config.tabs();

        backIcon = config.backIcon();

        icons = config.extraIcons().values().stream().toList();

        slots.clear();
        for (RangeUtil range : config.validSlots()) {
            for (int slot : range.all()) {
                slots.add(slot);
            }
        }

        screenType = config.screenType();
        if (screenType == GuiTemplate.GuiScreenType.FULL) {
            super.loadConfig(config.lowerGui());
        }
    }

    @Override
    public void open(@NotNull Player player, @NotNull GuiMetadata metadata) {
        Claim claim = metadata.claim();

        scheduler.scheduleAsync(() -> {
            PagedGui.Builder<Item> pagedGui = PagedGui.itemsBuilder();
            CharList structure = new CharArrayList();
            for (int i = 0; i < rows * 9; i++) {
                structure.add('#');
            }

            Map<Integer, GuiTemplate.Icon> mappedIcons = new HashMap<>();
            for (GuiTemplate.Icon icon : icons) {
                int codePoint = FIRST_SAFE_CHAR + icons.indexOf(icon);

                structure.set(icon.slot(), (char) codePoint);
                mappedIcons.put(codePoint, icon);
            }

            structure.set(previousPage.slot(), '(');
            structure.set(nextPage.slot(), ')');
            structure.set(createRoleIcon.slot(), '+');

            if (isValidIcon(backIcon)) {
                structure.set(backIcon.slot(), '$');
            }

            for (int slot : slots) {
                structure.set(slot, '-');
            }

            Class<? extends BaseGui> currentClass = getClass();
            TriConsumer<Gui.Builder<?, ?>, GuiRegistry, Player> tabsBuilder = buildTabs(
                    structure, currentClass, claim, metadata,
                    tabs
            );

            String[] structureArray = new String[rows];
            for (int r = 0; r < rows; r++) {
                CharList rowList = structure.subList(r * 9, (r + 1) * 9);

                structureArray[r] = new String(rowList.toCharArray());
            }

            pagedGui.setStructure(structureArray);

            tabsBuilder.accept(pagedGui, guis, player);

            for (Map.Entry<Integer, GuiTemplate.Icon> entry : mappedIcons.entrySet()) {
                GuiTemplate.Icon icon = entry.getValue();

                pagedGui.addIngredient((char) entry.getKey().intValue(), Item.builder()
                        .setItemProvider(TextUtil.parseItemPlaceholders(icon.item(), player))
                        .addClickHandler(click -> (switch (click.clickType()) {
                            case LEFT -> icon.leftClickActions();
                            case RIGHT -> icon.rightClickActions();
                            default -> List.<Action>of();
                        }).forEach(action -> action.execute(player)))
                        .build());
            }

            pagedGui.addIngredient('(', BoundItem.pagedBuilder()
                    .setItemProvider(new ItemBuilder(TextUtil.parseItemPlaceholders(previousPage.item(), player)))
                    .addClickHandler((item, gui, click) -> gui.setPage(gui.getPage() - 1))
                    .build());
            pagedGui.addIngredient(')', BoundItem.pagedBuilder()
                    .setItemProvider(new ItemBuilder(TextUtil.parseItemPlaceholders(nextPage.item(), player)))
                    .addClickHandler((item, gui, click) -> gui.setPage(gui.getPage() + 1))
                    .build());

            AtomicReference<PagedGui<Item>> guiReference = new AtomicReference<>(null);

            pagedGui.addIngredient('+', buildCreateRoleIcon(player, claim, guiReference, metadata));

            if (isValidIcon(backIcon)) {
                pagedGui.addIngredient('$', Item.builder()
                        .setItemProvider(TextUtil.parseItemPlaceholders(backIcon.item(), player))
                        .addClickHandler(click -> guis.get(ClaimListGui.class).open(player))
                        .build()
                );
            }

            pagedGui.addIngredient('-', Markers.CONTENT_LIST_SLOT_HORIZONTAL);

            Gui lowerGui = metadata.claimsGui() != null ? metadata.claimsGui() : screenType == GuiTemplate.GuiScreenType.FULL ? buildLowerGui(player, metadata) : null;
            if (metadata.claimsGui() == null) {
                metadata.claimsGui(lowerGui);
            }

            PagedGui<Item> upperGui = pagedGui.build();
            guiReference.set(upperGui);
            updateRoles(guiReference, player, claim, metadata);

            scheduler.schedule(() -> {
                Window.Builder.Normal.Split builder = Window.builder()
                        .setTitle(TextUtil.parse(title.text(), player, Map.of(
                                "claim_name", parseName(claim.name(), title.maxLength())
                        )))
                        .setUpperGui(upperGui)
                        .addCloseHandler(reason -> {
                            Storage storage = storageHolder.get();

                            storage.claims().saveRoles(claim);
                        });

                if (lowerGui != null) {
                    builder.setLowerGui(lowerGui);
                }

                Window window = builder.build(player);
                metadata.previousPage(window);
                window.open();
            });
        });
    }

    private void updateRoles(AtomicReference<PagedGui<Item>> gui, Player player, Claim claim, GuiMetadata metadata) {
        gui.get().setContent(buildRoles(gui, player, claim, metadata));
    }

    private List<Item> buildRoles(AtomicReference<PagedGui<Item>> gui, Player player, Claim claim, GuiMetadata metadata) {
        ClaimRoleRegistry registry = claim.roleRegistry();
        List<ClaimRole> allRoles = registry.allRoles();
        int allRolesQty = allRoles.size();

        ClaimMember playerMember = claim.getMember(player.getUniqueId())
                .orElse(null);
        ClaimRole playerRole = playerMember != null ?
                playerMember.role() :
                registry.everyoneRole();
        int playerRolePosition = allRoles.indexOf(playerRole);

        List<Item> roleList = new ArrayList<>();

        for (ClaimRole role : allRoles) {
            int rolePosition = allRoles.indexOf(role);

            boolean isOwnerRole = role.equals(registry.ownerRole());

            boolean isDefaultRole = isOwnerRole || role.equals(registry.defaultRole()) || role.equals(registry.everyoneRole());

            boolean canManage = playerMember != null && playerMember.hasPermission(Permission.MANAGE_ROLES) &&
                    playerRolePosition < rolePosition && !isOwnerRole;

            boolean canManagePermissions = playerMember != null && playerMember.hasPermission(Permission.MANAGE_ROLE_PERMISSIONS) &&
                    !isOwnerRole;

            boolean canRename = (playerRole.equals(registry.ownerRole()) || canManage) && !role.equals(registry.everyoneRole());

            boolean canSwapPrev = canManage && rolePosition > 1 && playerRolePosition < rolePosition - 1 && rolePosition < allRolesQty - 2;

            boolean canSwapNext = canManage && rolePosition < allRolesQty - 3;

            Item item = Item.builder()
                    .setItemProvider(p -> {
                        ItemStack stack = roleIcon.icon();
                        stack.editMeta(meta -> {
                            meta.itemName(TextUtil.parseItem(roleIcon.name(), player, Map.of(
                                    "name", role.name()
                            )));

                            long roleMembers = claim.members().stream()
                                    .filter(member -> member.role().equals(role))
                                    .count();

                            ClaimRolesConfig.RoleIcon.RoleIconLore lore = roleIcon.lore();
                            meta.lore(TextUtil.parseItemLore(lore.base(), player, Map.of(
                                    "members", roleMembers + "",
                                    "creation_date", StringUtil.formatDate(role.creationTimestamp()),
                                    "manage", (canManage || canManagePermissions) ? lore.manage() : lore.cantManage(),
                                    "rename", canRename ? lore.rename() : lore.cantRename(),
                                    "swap_previous", canSwapPrev ? lore.swapPrevious() : lore.cantSwapPrevious(),
                                    "swap_next", canSwapNext ? lore.swapNext() : lore.cantSwapNext()
                            )));
                        });

                        return new ItemWrapper(TextUtil.parseItemPlaceholders(stack, player));
                    })
                    .addClickHandler((it, click) -> {
                        switch (click.clickType()) {
                            case LEFT -> {
                                if (!canManage && !canManagePermissions) {
                                    return;
                                }

                                guis.get(ClaimRoleManageGui.class)
                                        .open(player, metadata
                                                .role(role)
                                                .canManageRole(canManage && !isDefaultRole)
                                                .canManageRolePermissions(canManagePermissions)
                                        );
                            }
                            case RIGHT -> {
                                if (!canRename) {
                                    return;
                                }

                                new SingleInputDialog()
                                        .create(
                                                messagesHolder.get().dialogs().renameRole(),
                                                Map.of("role_name", role.name()), Map.of(),
                                                role.name(), 40
                                        )
                                        .onSubmit(view -> {
                                            String newName = view.getText("input");
                                            if (newName == null) {
                                                return;
                                            }

                                            role.rename(newName);
                                            it.notifyWindows();
                                        })
                                        .show(player);
                            }
                            case SHIFT_LEFT -> {
                                if (!canSwapPrev) {
                                    return;
                                }

                                registry.swap(allRoles.get(rolePosition - 1), role);
                                updateRoles(gui, player, claim, metadata);
                            }
                            case SHIFT_RIGHT -> {
                                if (!canSwapNext) {
                                    return;
                                }

                                registry.swap(role, allRoles.get(rolePosition + 1));
                                updateRoles(gui, player, claim, metadata);
                            }
                        }
                    })
                    .build();

            roleList.add(item);
        }

        return roleList;
    }

    private Item buildCreateRoleIcon(@NotNull Player player, @NotNull Claim claim, AtomicReference<PagedGui<Item>> gui, GuiMetadata metadata) {
        return Item.builder()
                .setItemProvider(TextUtil.parseItemPlaceholders(createRoleIcon.item(), player))
                .addClickHandler(click ->
                        new SingleInputDialog()
                                .create(
                                        messagesHolder.get().dialogs().createRole(),
                                        Map.of("claim_name", claim.name()), Map.of(),
                                        "", 40
                                )
                                .onSubmit(view -> {
                                    String value = view.getText("input");
                                    if (value == null) {
                                        return;
                                    }

                                    ClaimRoleRegistry registry = claim.roleRegistry();
                                    registry.add(new ClaimRole(UUID.randomUUID(), value, registry.defaultRole().permissions()));

                                    updateRoles(gui, player, claim, metadata);
                                })
                                .show(player))
                .build();
    }
}