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
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import net.minecraft.server.players.NameAndId;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
@SuppressWarnings({"UnstableApiUsage"})
public class ClaimMemberListGui implements BaseGui {

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
    private String title;
    private int rows = 1;

    private GuiTemplate.DynamicIcon unmanageableMemberIcon;
    private GuiTemplate.DynamicIcon memberIcon;

    private ClaimMemberListConfig.FilterIcon filterIcon;
    private ClaimMemberListConfig.SearchIcon searchIcon;
    private GuiTemplate.SimpleIcon addMemberIcon;

    private GuiTemplate.SimpleIcon backIcon;
    private GuiTemplate.SimpleIcon previousPage;
    private GuiTemplate.SimpleIcon nextPage;
    private GuiTemplate.SimpleIcon deleteIcon;

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

        backIcon = config.pages().get("back");
        previousPage = config.pages().get("previous-page");
        nextPage = config.pages().get("next-page");

        deleteIcon = config.deleteIcon();

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
    }

    @Override
    public void open(@NotNull Player player, Object... args) {
        Claim claim = (Claim) args[0];

        PaginatedGui gui = Gui.paginated()
                .title(TextUtil.parse(title, Map.of(
                        "claim_name", claim.name()
                )))
                .pageSize(slots.size())
                .rows(rows)
                .disableAllInteractions()
                .create();

        scheduler.scheduleAsync(() -> {
            buildIcons(player, gui, claim);

            scheduler.schedule(() -> gui.open(player));
        });
    }

    private void buildIcons(@NotNull Player player, @NotNull PaginatedGui gui, @NotNull Claim claim) {
        buildPageLayout(gui, slots, previousPage, nextPage);

        gui.setItem(backIcon.slot(), new GuiItem(backIcon.item(), action ->
                guis.get(ClaimListGui.class).open(player)
        ));

        for (GuiTemplate.Icon icon : icons) {
            gui.setItem(icon.slot(), new GuiItem(icon.item(), action -> {
                for (Action iconAction : action.isLeftClick() ? icon.leftClickActions() : icon.rightClickActions()) {
                    iconAction.execute(player);
                }
            }));
        }

        gui.setItem(addMemberIcon.slot(), buildAddMemberIcon(player, claim));

        AtomicReference<String> searchQuery = new AtomicReference<>(null);
        AtomicReference<String> filterQuery = new AtomicReference<>(null);

        handleSearch(gui, player, claim, searchQuery, filterQuery);
        handleFilter(gui, player, claim, searchQuery, filterQuery);

        gui.setItem(membersTab.slot(), new GuiItem(membersTab.item()));
        gui.setItem(rolesTab.slot(), new GuiItem(rolesTab.item(), action -> player.sendRichMessage("<green>viewing roles")));
        gui.setItem(settingsTab.slot(), new GuiItem(settingsTab.item(), action -> player.sendRichMessage("<green>viewing settings")));
        gui.setItem(manageTab.slot(), new GuiItem(manageTab.item(), action -> {
            BaseGui tab = claim.main() == null ? guis.get(ClaimManageGui.class) : guis.get(SubClaimManageGui.class);

            tab.open(player, claim);
        }));

        gui.setItem(deleteIcon.slot(), new GuiItem(deleteIcon.item(), action -> player.sendRichMessage("<green>viewing delete")));

        updateMembers(gui, player, claim, searchQuery, filterQuery);
    }

    private void updateMembers(@NotNull PaginatedGui gui, @NotNull Player player, @NotNull Claim claim, @NotNull AtomicReference<String> searchQuery, @NotNull AtomicReference<String> filterQuery) {
        gui.clearPageItems();

        List<ClaimMember> sortedList = claim.members()
                .stream()
                .sorted(Comparator.comparing(member -> member.equals(claim.owner()), Comparator.reverseOrder())
                        .thenComparingLong(member -> ((ClaimMember) member).joinedTimestamp().getEpochSecond())
                )
                .toList();

        ClaimMember selfMember = claim.getMember(player.getUniqueId())
                .orElse(null);

        for (ClaimMember member : sortedList) {
            if (searchQuery.get() != null) {
                if (!StringUtil.has(member.lastKnownName(), searchQuery.get())) {
                    continue;
                }
            }

            if (filterQuery.get() != null) {
                if (!member.role().id().toString().equals(filterQuery.get())) {
                    continue;
                }
            }

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

            gui.addItem(new GuiItem(head, action -> {
                if (!canManage) {
                    return;
                }

                player.sendRichMessage("managing");
            }));
        }
    }

    private void handleSearch(@NotNull PaginatedGui gui, @NotNull Player player, @NotNull Claim claim, @NotNull AtomicReference<String> searchQuery, @NotNull AtomicReference<String> filterQuery) {
        Runnable updateGui = () -> {
            handleSearch(gui, player, claim, searchQuery, filterQuery);
            updateMembers(gui, player, claim, searchQuery, filterQuery);

            gui.update();
        };

        gui.setItem(searchIcon.slot(), new GuiItem(buildSearchIcon(searchQuery), action -> new SearchDialog()
                .create(messagesHolder.get().dialogs())
                .onSubmit(view -> {
                    String query = view.getText("query");

                    searchQuery.set(query);

                    updateGui.run();
                })
                .onCancel(() -> {
                    searchQuery.set(null);

                    updateGui.run();
                })
                .show(player)));
    }

    private void handleFilter(@NotNull PaginatedGui gui, @NotNull Player player, @NotNull Claim claim, @NotNull AtomicReference<String> searchQuery, @NotNull AtomicReference<String> filterQuery) {
        List<ClaimRole> roles = claim.roleRegistry()
                .allRoles()
                .stream()
                .filter(role -> role.id().equals(claim.roleRegistry().everyoneRole().id()))
                .toList();

        ItemStack item = buildFilterIcon(filterQuery, roles);

        gui.setItem(filterIcon.slot(), new GuiItem(item, action -> {
            boolean isNext = action.isLeftClick();

            String filter = filterQuery.get();
            if (filter == null) {
                filterQuery.set((isNext ? roles.getFirst() : roles.getLast()).id().toString());
            } else {
                ClaimRole currentRole = roles.stream().filter(role -> role.id().toString().equals(filter))
                        .findFirst()
                        .orElse(null);

                if (currentRole == null) {
                    filterQuery.set(null);
                } else {
                    int current = roles.indexOf(currentRole);

                    if (isNext ? (current + 1) >= roles.size() : (current - 1) < 0) {
                        filterQuery.set(null);
                    } else {
                        filterQuery.set(roles.get(isNext ? current + 1 : current - 1).id().toString());
                    }
                }
            }

            handleFilter(gui, player, claim, searchQuery, filterQuery);
            updateMembers(gui, player, claim, searchQuery, filterQuery);

            gui.update();
        }));
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

    private GuiItem buildAddMemberIcon(@NotNull Player player, @NotNull Claim claim) {
        return new GuiItem(addMemberIcon.item(), action -> {
            Input<?> currentInput = inputManager.fetch(player);

            if (currentInput != null) {
                return;
            }

            AtomicReference<Runnable> inputRunnable = new AtomicReference<>();
            InventoryView inv = player.getOpenInventory();

            inputRunnable.set(() -> {
                Input<NameAndId> input = inputManager.create(player, NameAndId.class);
                if (input == null) {
                    return;
                }

                player.closeInventory();
                input.onCancel(() -> {
                            text.send(player, messagesHolder.get().inputs().cancelled());

                            player.openInventory(inv);
                        })
                        .onSubmit((member) -> {
                            ClaimMember added = claim.addMember(member);

                            if (added != null) {
                                Storage storage = storageHolder.get();
                                storage.claims()
                                        .saveMember(claim.claimId(), added);

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
        });
    }

    private record FilterType(String id, String name) {
    }
}