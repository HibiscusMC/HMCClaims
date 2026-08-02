package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRoleRegistry;
import com.hibiscusmc.hmcclaims.config.gui.ClaimMemberRoleConfig;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.RangeUtil;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.StringUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
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
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class ClaimMemberRoleGui extends ClaimMemberManageGui {

    @Inject
    private ConfigHolder<ClaimMemberRoleConfig> configHolder;

    @Inject
    private StorageHolder storageHolder;

    @Inject
    private GuiRegistry guis;

    @Inject
    private SchedulerUtil scheduler;

    private final IntSet slots = new IntArraySet();
    private GuiTemplate.GuiTitle title;
    private int rows = 1;

    private GuiTemplate.DynamicIconWithStack roleIcon;
    private GuiTemplate.DynamicIconWithStack roleIconSelected;
    private GuiTemplate.DynamicIconWithStack roleIconUnable;

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
        ClaimMemberRoleConfig config = configHolder.get();
        if (config == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        title = config.title();
        rows = config.rows();

        roleIcon = config.roleIcon();
        roleIconSelected = config.roleIconSelected();
        roleIconUnable = config.roleIconUnable();

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

        slots.clear();
        for (RangeUtil range : config.validSlots()) {
            for (int slot : range.all()) {
                slots.add(slot);
            }
        }

        screenType = config.screenType();
        if (screenType == GuiTemplate.GuiScreenType.FULL) {
            loadConfig(config.lowerGui());
        }
    }

    @Override
    public void open(@NotNull Player player, @NotNull GuiMetadata metadata) {
        Claim claim = metadata.claim();
        ClaimMember member = metadata.member();

        scheduler.scheduleAsync(() -> {
            PagedGui.Builder<Item> builder = PagedGui.itemsBuilder();

            CharList structure = new CharArrayList();
            for (int i = 0; i < rows * 9; i++) {
                structure.add((char) FIRST_SAFE_CHAR);
            }

            for (int slot : slots) {
                structure.set(slot, (char) (FIRST_SAFE_CHAR + 1));
            }

            structure.set(previousPage.slot(), (char) (FIRST_SAFE_CHAR + 4));
            structure.set(nextPage.slot(), (char) (FIRST_SAFE_CHAR + 5));

            structure.set(kickIcon.slot(), (char) (FIRST_SAFE_CHAR + 6));
            structure.set(banIcon.slot(), (char) (FIRST_SAFE_CHAR + 7));

            if (isValidIcon(backIcon)) {
                structure.set(backIcon.slot(), (char) (FIRST_SAFE_CHAR + 8));
            }

            int currentPoint = FIRST_SAFE_CHAR + 9;
            Char2ObjectMap<Item> tabsMap = new Char2ObjectOpenHashMap<>();
            for (int slot : rolesTab.slots()) {
                structure.set(slot, (char) currentPoint);

                tabsMap.put((char) currentPoint, Item.builder()
                        .setItemProvider(rolesTab.item())
                        .build());
                currentPoint++;
            }
            for (int slot : permissionsTab.slots()) {
                structure.set(slot, (char) currentPoint);

                tabsMap.put((char) currentPoint, Item.builder()
                        .setItemProvider(permissionsTab.item())
                        .addClickHandler(click -> guis.get(ClaimMemberPermissionsGui.class).open(player, metadata))
                        .build());
                currentPoint++;
            }

            Char2ObjectMap<Item> itemMap = new Char2ObjectOpenHashMap<>();
            for (Int2ObjectMap.Entry<Item> extraItem : extraItems.int2ObjectEntrySet()) {
                structure.set(extraItem.getIntKey(), (char) currentPoint);

                itemMap.put((char) currentPoint, extraItem.getValue());
                currentPoint++;
            }

            builder.setStructure(parseStructure(structure, rows));

            tabsMap.char2ObjectEntrySet().forEach(entry -> builder.addIngredient(
                    entry.getCharKey(), entry.getValue()
            ));

            itemMap.char2ObjectEntrySet().forEach(entry -> builder.addIngredient(
                    entry.getCharKey(), entry.getValue()
            ));

            builder.addIngredient((char) (FIRST_SAFE_CHAR + 2), Item.builder()
                    .setItemProvider(rolesTab.item())
                    .build());
            builder.addIngredient((char) (FIRST_SAFE_CHAR + 3), Item.builder()
                    .setItemProvider(permissionsTab.item())
                    .addClickHandler(click -> guis.get(ClaimMemberPermissionsGui.class).open(player, metadata))
                    .build());

            builder.addIngredient((char) (FIRST_SAFE_CHAR + 4), BoundItem.pagedBuilder()
                    .setItemProvider(new ItemBuilder(previousPage.item()))
                    .addClickHandler((item, gui, click) -> gui.setPage(gui.getPage() - 1))
                    .build());
            builder.addIngredient((char) (FIRST_SAFE_CHAR + 5), BoundItem.pagedBuilder()
                    .setItemProvider(new ItemBuilder(nextPage.item()))
                    .addClickHandler((item, gui, click) -> gui.setPage(gui.getPage() + 1))
                    .build());

            builder.addIngredient((char) (FIRST_SAFE_CHAR + 6), buildKickItem(player, claim, member, kickIcon, cantKickIcon));
            builder.addIngredient((char) (FIRST_SAFE_CHAR + 7), buildBanItem(player, claim, member, banIcon, cantBanIcon));

            if (isValidIcon(backIcon)) {
                builder.addIngredient((char) (FIRST_SAFE_CHAR + 8), Item.builder()
                        .setItemProvider(backIcon.item())
                        .addClickHandler(click -> guis.get(ClaimMemberListGui.class).open(player, metadata))
                        .build());
            }

            AtomicReference<PagedGui<Item>> guiReference = new AtomicReference<>();
            builder.addIngredient((char) (FIRST_SAFE_CHAR + 1), Markers.CONTENT_LIST_SLOT_HORIZONTAL);
            builder.setContent(buildRoleItems(player, claim, member, metadata, guiReference));

            PagedGui<Item> gui = builder.build();
            guiReference.set(gui);

            Gui lowerGui = screenType == GuiTemplate.GuiScreenType.FULL ? build(player, metadata) : null;

            scheduler.schedule(() -> {
                Window.Builder.Normal.Split window = Window.builder()
                        .setTitle(TextUtil.parse(title.text(), Map.of(
                                "member_name", parseName(member.lastKnownName(), title.maxLength())
                        )))
                        .setUpperGui(gui)
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

    private List<Item> buildRoleItems(Player player, Claim claim, ClaimMember targetMember, GuiMetadata metadata, AtomicReference<PagedGui<Item>> guiReference) {
        ClaimRoleRegistry registry = claim.roleRegistry();
        List<ClaimRole> allRoles = registry.allRoles();
        List<Item> roleItems = new ArrayList<>();

        Optional<ClaimMember> executor = claim.getMember(player.getUniqueId());
        boolean canManage = executor.map(m ->
                        m.hasPermission(Permission.MANAGE_MEMBER_ROLES) &&
                                m.canManage(targetMember)
                )
                .orElse(false);

        ClaimRole executorRole = executor
                .map(ClaimMember::role)
                .orElse(claim.roleRegistry().everyoneRole());
        int rolePosition = allRoles.indexOf(executorRole);

        for (ClaimRole role : allRoles) {
            boolean validPosition = rolePosition < allRoles.indexOf(role);
            boolean validRole = !role.equals(claim.roleRegistry().everyoneRole()) &&
                    !role.equals(claim.roleRegistry().ownerRole());

            Item item = Item.builder()
                    .setItemProvider(p -> {
                        GuiTemplate.DynamicIconWithStack icon = targetMember.role().equals(role) ? roleIconSelected : canManage && validPosition && validRole ? roleIcon : roleIconUnable;
                        ItemStack stack = icon.item();

                        stack.editMeta(meta -> {
                            meta.itemName(TextUtil.parseItem(icon.name(), Map.of("name", role.name())));

                            long roleMembers = claim.members().stream()
                                    .filter(m -> m.role().equals(role))
                                    .count();

                            meta.lore(TextUtil.parseItemLore(icon.lore(), Map.of(
                                    "members", String.valueOf(roleMembers),
                                    "creation_date", StringUtil.formatDate(role.creationTimestamp()),
                                    "player_name", targetMember.lastKnownName()
                            )));
                        });

                        return new ItemWrapper(stack);
                    })
                    .addClickHandler((it, click) -> {
                        if (click.clickType() != ClickType.LEFT || !canManage || !validPosition || !validRole) {
                            return;
                        }

                        targetMember.role(role);
                        guiReference.get()
                                .setContent(buildRoleItems(player, claim, targetMember, metadata, guiReference));
                    })
                    .build();

            roleItems.add(item);
        }

        return roleItems;
    }
}