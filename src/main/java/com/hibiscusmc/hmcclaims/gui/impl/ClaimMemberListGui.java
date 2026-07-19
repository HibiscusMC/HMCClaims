package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.gui.ClaimMemberListConfig;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.dialog.type.SearchDialog;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.gui.BaseGui;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.input.Input;
import com.hibiscusmc.hmcclaims.input.InputManager;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.ItemUtil;
import com.hibiscusmc.hmcclaims.util.PlaceholderUtil;
import com.hibiscusmc.hmcclaims.util.RangeUtil;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.StringUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import net.kyori.adventure.text.Component;
import net.minecraft.server.players.NameAndId;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
@SuppressWarnings({"UnstableApiUsage"})
public class ClaimMemberListGui extends ClaimListGui {

    @Inject
    private ConfigHolder<ClaimMemberListConfig> configHolder;
    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private StorageHolder storageHolder;

    @Inject
    private GuiRegistry guis;

    @Inject
    private InputManager inputManager;

    @Inject
    private PlaceholderUtil placeholders;
    @Inject
    private SchedulerUtil scheduler;
    @Inject
    private TextUtil text;

    private final List<Integer> slots = new ArrayList<>();
    private GuiTemplate.GuiTitle title;
    private int rows = 1;

    private GuiTemplate.GuiScreenType screenType;

    private GuiTemplate.DynamicIcon unmanageableMemberIcon;
    private GuiTemplate.DynamicIcon memberIcon;

    private ClaimMemberListConfig.FilterIcon filterIcon;
    private ClaimMemberListConfig.SearchIcon searchIcon;
    private GuiTemplate.SimpleIcon addMemberIcon;

    private GuiTemplate.SimpleIcon previousPage;
    private GuiTemplate.SimpleIcon nextPage;

    private GuiTemplate.SimpleIcon membersTab;
    private GuiTemplate.SimpleIcon rolesTab;
    private GuiTemplate.SimpleIcon settingsTab;
    private GuiTemplate.SimpleIcon manageTab;

    private List<GuiTemplate.Icon> icons;

    @Override
    public void loadConfig() {
        ClaimMemberListConfig config = configHolder.get();

        if (config == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        title = config.title();
        rows = config.rows();

        unmanageableMemberIcon = config.unmanageableMember();
        memberIcon = config.memberIcon();

        searchIcon = config.searchIcon();
        filterIcon = config.filterIcon();
        addMemberIcon = config.addMemberIcon();

        previousPage = config.pages().get("previous-page");
        nextPage = config.pages().get("next-page");

        membersTab = config.tabs().get("members-tab");
        rolesTab = config.tabs().get("roles-tab");
        settingsTab = config.tabs().get("settings-tab");
        manageTab = config.tabs().get("manage-tab");

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
    public void open(@NotNull Player player, Object... args) {
        Claim claim = (Claim) args[0];

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
            structure.set(addMemberIcon.slot(), '+');

            for (int slot : slots) {
                structure.set(slot, '-');
            }

            structure.set(filterIcon.slot(), '%');
            structure.set(searchIcon.slot(), '&');

            Class<? extends BaseGui> currentClass = getClass();
            TriConsumer<Gui.Builder<?, ?>, GuiRegistry, Player> tabsBuilder = buildTabs(
                    structure, currentClass,
                    new TabIcon(ClaimMemberListGui.class, membersTab.item(), membersTab.slot(), claim),
                    new TabIcon(ClaimMemberListGui.class, rolesTab.item(), rolesTab.slot(), claim),
                    new TabIcon(ClaimSettingsGui.class, settingsTab.item(), settingsTab.slot(), claim),
                    new TabIcon(claim.main() == null ? ClaimManageGui.class : SubClaimManageGui.class, manageTab.item(), manageTab.slot(), claim)
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
                        .setItemProvider(icon.item())
                        .addClickHandler(click -> {
                            for (Action iconAction : click.clickType().isLeftClick() ?
                                    icon.leftClickActions() :
                                    icon.rightClickActions()) {
                                iconAction.execute(player);
                            }
                        })
                        .build());
            }

            pagedGui.addIngredient('(', BoundItem.pagedBuilder()
                    .setItemProvider(new ItemBuilder(previousPage.item()))
                    .addClickHandler((item, gui, click) -> gui.setPage(gui.getPage() - 1))
                    .build());
            pagedGui.addIngredient(')', BoundItem.pagedBuilder()
                    .setItemProvider(new ItemBuilder(nextPage.item()))
                    .addClickHandler((item, gui, click) -> gui.setPage(gui.getPage() + 1))
                    .build());

            pagedGui.addIngredient('+', buildAddMemberIcon(player, claim));

            pagedGui.addIngredient('-', Markers.CONTENT_LIST_SLOT_HORIZONTAL);

            Metadata metadata = new Metadata(
                    new AtomicReference<>(null),
                    new AtomicReference<>(null)
            );

            AtomicReference<PagedGui<Item>> guiReference = new AtomicReference<>(null);

            Map<ClaimMember, Item> parsedMembers = buildMembers(guiReference, player, claim);

            Runnable updateMembers = () -> updateMembers(guiReference.get(), player, claim, parsedMembers, metadata);
            pagedGui.addIngredient('%', buildFilter(metadata, claim, updateMembers));
            pagedGui.addIngredient('&', buildSearch(metadata, updateMembers));

            Gui lowerGui = screenType == GuiTemplate.GuiScreenType.FULL ? buildLowerGui(player) : null;

            PagedGui<Item> upperGui = pagedGui.build();
            guiReference.set(upperGui);

            updateMembers.run();

            scheduler.schedule(() -> {
                Window.Builder.Normal.Split window = Window.builder()
                        .setTitle(TextUtil.parse(title.text(), Map.of(
                                "claim_name", parseName(claim.name(), title.maxLength())
                        )))
                        .setUpperGui(upperGui);

                if (lowerGui != null) {
                    window.setLowerGui(lowerGui);
                }

                window.open(player);
            });
        });
    }

    private void updateMembers(@NotNull PagedGui<Item> gui, @NotNull Player player, @NotNull Claim claim, @NotNull Map<ClaimMember, Item> parsedMembers, @NotNull Metadata metadata) {
        List<Item> items = new ArrayList<>();

        List<Map.Entry<ClaimMember, Item>> sortedMembers = parsedMembers.entrySet().stream()
                .sorted(Comparator.<Map.Entry<ClaimMember, Item>, Boolean>comparing(
                                        entry -> entry.getKey().uuid().equals(claim.owner()),
                                        Comparator.reverseOrder()
                                )
                                .thenComparingLong(entry -> entry.getKey().joinedTimestamp().getEpochSecond())
                )
                .toList();

        for (Map.Entry<ClaimMember, Item> entry : sortedMembers) {
            ClaimMember member = entry.getKey();
            Item item = entry.getValue();

            String searchQuery = metadata.searchQuery().get();
            if (searchQuery != null) {
                if (!StringUtil.has(member.lastKnownName(), searchQuery)) {
                    continue;
                }
            }

            String filter = metadata.filter().get();
            if (filter != null) {
                if (!member.role().id().toString().equals(filter)) {
                    continue;
                }
            }

            items.add(item);
        }

        gui.setContent(items);
    }

    private Map<ClaimMember, Item> buildMembers(@NotNull AtomicReference<PagedGui<Item>> guiReference, @NotNull Player player, @NotNull Claim claim) {
        Map<ClaimMember, Item> parsedMembers = new HashMap<>();

        Set<ClaimMember> members = claim.members();
        ClaimMember selfMember = claim.getMember(player.getUniqueId())
                .orElse(null);

        for (ClaimMember member : members) {
            boolean canManage = selfMember != null && selfMember.canManage(member);
            Map<String, String> data = placeholders.memberInfo(member);

            ItemStack head = ItemUtil.buildHeadWithName(member.lastKnownName());
            if (canManage) {
                ItemUtil.applyDisplay(head,
                        TextUtil.parseItem(memberIcon.name(), data),
                        TextUtil.parseItemLore(memberIcon.lore(), data)
                );
            } else {
                ItemUtil.applyDisplay(head,
                        TextUtil.parseItem(unmanageableMemberIcon.name(), data),
                        TextUtil.parseItemLore(unmanageableMemberIcon.lore(), data)
                );
            }

            parsedMembers.put(member, Item.builder()
                    .setItemProvider(head)
                    .addClickHandler(click -> {
                        System.out.println("clicked on " + member.lastKnownName());
                    })
                    .build());
        }

        return parsedMembers;
    }

    private Item buildSearch(@NotNull Metadata metadata, Runnable update) {
        return Item.builder()
                .setItemProvider(player -> new ItemWrapper(buildSearchIcon(metadata.searchQuery())))
                .addClickHandler((it, click) -> new SearchDialog()
                        .create(messagesHolder.get().dialogs())
                        .onSubmit(view -> {
                            String query = view.getText("query");

                            metadata.searchQuery().set(query);

                            it.notifyWindows();
                            update.run();
                        })
                        .onCancel(() -> {
                            metadata.searchQuery().set(null);

                            it.notifyWindows();
                            update.run();
                        })
                        .show(click.player())
                )
                .build();
    }

    private Item buildFilter(@NotNull Metadata metadata, @NotNull Claim claim, Runnable update) {
        List<ClaimRole> roles = claim.roleRegistry()
                .allRoles()
                .stream()
                .filter(role -> !role.id().equals(claim.roleRegistry().everyoneRole().id()))
                .toList();

        return Item.builder()
                .setItemProvider(player -> new ItemWrapper(buildFilterIcon(metadata.filter(), roles)))
                .addClickHandler((it, click) -> {
                    boolean isNext = click.clickType().isLeftClick();

                    String filter = metadata.filter().get();
                    if (filter == null) {
                        metadata.filter().set((isNext ? roles.getFirst() : roles.getLast()).id().toString());
                    } else {
                        ClaimRole currentRole = roles.stream().filter(role -> role.id().toString().equals(filter))
                                .findFirst()
                                .orElse(null);

                        if (currentRole == null) {
                            metadata.filter().set(null);
                        } else {
                            int current = roles.indexOf(currentRole);

                            if (isNext ? (current + 1) >= roles.size() : (current - 1) < 0) {
                                metadata.filter().set(null);
                            } else {
                                metadata.filter().set(roles.get(isNext ? current + 1 : current - 1).id().toString());
                            }
                        }
                    }

                    update.run();
                    it.notifyWindows();
                })
                .build();
    }

    @NotNull
    private ItemStack buildSearchIcon(@NotNull AtomicReference<String> searchQuery) {
        ItemStack item = searchIcon.item();
        item.editMeta(meta -> {
            String query = searchQuery.get();
            if (query == null || query.isEmpty()) {
                query = searchIcon.noQuery();
            }

            meta.lore(TextUtil.parseItemLore(searchIcon.lore(), Map.of(
                    "query", query
            )));
        });

        return item;
    }

    @NotNull
    private ItemStack buildFilterIcon(@NotNull AtomicReference<String> filterQuery, @NotNull List<ClaimRole> roles) {
        ItemStack item = filterIcon.item();
        item.editMeta(meta -> {
            meta.customName(TextUtil.parseItem(filterIcon.name()));

            List<Component> lore = new ArrayList<>();
            for (String line : filterIcon.lore()) {
                if (line.contains("<filter_list>")) {
                    String allName = filterIcon.filterNames().getOrDefault("ALL", "All");
                    String roleName = filterIcon.filterNames().getOrDefault("ROLE", "<role> Role");

                    List<FilterType> list = new ArrayList<>();
                    list.add(new FilterType("ALL", allName));
                    list.addAll(roles.stream().map(role ->
                            new FilterType(role.id().toString(), roleName.replace("<role>", role.name()))
                    ).toList());

                    String filter = filterQuery.get();
                    for (FilterType role : list) {
                        String id = role.id();
                        String name = role.name();

                        boolean selected = (id.equalsIgnoreCase("ALL") && filterQuery.get() == null) || id.equals(filter);

                        String base = line.replace("<filter_list>", selected ? filterIcon.selected() : filterIcon.unselected());
                        lore.add(TextUtil.parseItem(base, Map.of(
                                "name", name
                        )));
                    }

                    continue;
                }

                lore.add(TextUtil.parseItem(line));
            }

            meta.lore(lore);
        });

        return item;
    }

    private Item buildAddMemberIcon(@NotNull Player player, @NotNull Claim claim) {
        return Item.builder()
                .setItemProvider(addMemberIcon.item())
                .addClickHandler(click -> {
                    Input<?> currentInput = inputManager.fetch(player);

                    if (currentInput != null) {
                        return;
                    }

                    AtomicReference<Runnable> inputRunnable = new AtomicReference<>();

                    inputRunnable.set(() -> {
                        Input<NameAndId> input = inputManager.create(player, NameAndId.class);
                        if (input == null) {
                            return;
                        }

                        player.closeInventory();
                        input.onCancel(() -> {
                                    text.send(player, messagesHolder.get().inputs().cancelled());

                                    open(player, claim);
                                })
                                .onSubmit((member) -> {
                                    ClaimMember added = claim.addMember(member);

                                    if (added != null) {
                                        Storage storage = storageHolder.get();
                                        storage.claims()
                                                .saveMembers(claim);

                                        text.send(player, messagesHolder.get().claims().memberAdded(), Map.of(
                                                "name", member.name(),
                                                "player_head", "<head:" + member.name() + ">",
                                                "claim", claim.name()
                                        ));

                                        open(player, claim);
                                    } else {
                                        text.send(player, messagesHolder.get().claims().memberAlreadyAdded());

                                        scheduler.schedule(() -> inputRunnable.get().run());
                                    }
                                });
                    });

                    inputRunnable.get().run();
                })
                .build();
    }

    record Metadata(AtomicReference<String> filter, AtomicReference<String> searchQuery) {
    }

    record FilterType(String id, String name) {
    }
}