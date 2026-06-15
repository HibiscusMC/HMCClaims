package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.gui.ClaimListConfig;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.dialog.type.RenameDialog;
import com.hibiscusmc.hmcclaims.dialog.type.SearchDialog;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.gui.BaseGui;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.util.PlaceholderUtil;
import com.hibiscusmc.hmcclaims.util.RangeUtil;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.StringUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Singleton
@SuppressWarnings({"UnstableApiUsage"})
public class ClaimListGui implements BaseGui {

    @Inject
    private ConfigHolder<ClaimListConfig> configHolder;
    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private GuiRegistry guis;

    @Inject
    private PlaceholderUtil placeholders;
    @Inject
    private SchedulerUtil scheduler;

    @Inject
    private ClaimManager claimManager;

    private final List<Integer> slots = new ArrayList<>();
    private Component title;
    private int rows = 1;

    private ClaimListConfig.ClaimsIcon claimsIcon;
    private ClaimListConfig.SubClaimsIcon subClaimsIcon;

    private GuiTemplate.SimpleIcon searchIcon;
    private ClaimListConfig.FilterIcon filterIcon;

    private GuiTemplate.SimpleIcon previousPage;
    private GuiTemplate.SimpleIcon nextPage;

    private List<GuiTemplate.Icon> icons;

    @Override
    public void loadConfig() {
        ClaimListConfig config = configHolder.get();

        if (config == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        this.title = TextUtil.parse(config.title());
        this.rows = config.rows();

        this.claimsIcon = config.claimsIcon();
        this.subClaimsIcon = config.subClaimsIcon();

        this.searchIcon = config.searchIcon();
        this.filterIcon = config.filterIcon();

        this.previousPage = config.pages().get("previous-page");
        this.nextPage = config.pages().get("next-page");

        this.icons = config.extraIcons().values().stream().toList();

        slots.clear();
        for (RangeUtil range : config.validSlots()) {
            for (int slot : range.all()) {
                slots.add(slot);
            }
        }
    }

    @Override
    public void open(@NotNull Player player) {
        PaginatedGui gui = Gui.paginated()
                .title(title)
                .rows(rows)
                .pageSize(slots.size())
                .disableAllInteractions()
                .create();

        scheduler.scheduleAsync(() -> {
            buildIcons(player, gui);

            scheduler.schedule(() -> gui.open(player));
        });
    }

    private void buildIcons(@NotNull Player player, @NotNull PaginatedGui gui) {
        buildPageLayout(gui, slots, previousPage, nextPage);

        for (GuiTemplate.Icon icon : icons) {
            gui.setItem(icon.slot(), new GuiItem(icon.item(), action -> {
                for (Action iconAction : action.isLeftClick() ? icon.leftClickActions() : icon.rightClickActions()) {
                    iconAction.execute(player);
                }
            }));
        }

        UUID playerId = player.getUniqueId();

        AtomicReference<Filter> filter = new AtomicReference<>(Filter.ALL);
        AtomicReference<Query> searchQuery = new AtomicReference<>(null);
        List<Claim> claims = claimManager.getPlayerClaims(playerId);

        gui.setItem(searchIcon.slot(), new GuiItem(searchIcon.item(), action -> new SearchDialog(QueryType.all())
                .create(messagesHolder.get().dialogs())
                .onSubmit(view -> {
                    String query = view.getText("query");
                    String rawType = view.getText("option");
                    assert rawType != null;

                    QueryType type = QueryType.fromId(rawType);
                    assert type != null;

                    searchQuery.set(new Query(query, type));

                    updateClaims(gui, claims, filter, searchQuery);
                    gui.update();
                })
                .onCancel(() -> {
                    searchQuery.set(null);

                    updateClaims(gui, claims, filter, searchQuery);
                    gui.update();
                })
                .show(player)));

        updateClaims(gui, claims, filter, searchQuery);
        updateFilter(gui, claims, filter, searchQuery);
    }

    private void updateFilter(@NotNull PaginatedGui gui, List<Claim> claims, @NotNull AtomicReference<Filter> filter, AtomicReference<Query> searchQuery) {
        ItemStack stack = filterIcon.item();
        ItemMeta meta = stack.getItemMeta();

        List<Component> lore = new ArrayList<>();
        Filter currentOption = filter.get();

        for (String line : filterIcon.lore()) {
            if (line.toLowerCase().contains("<filter_list>")) {
                for (Filter option : Filter.values()) {
                    String name = filterIcon.filterNames()
                            .getOrDefault(option.name(), option.name());

                    String parsedName = option.equals(currentOption) ? filterIcon.selected() : filterIcon.unselected();

                    lore.add(TextUtil.parseItem(line.replace("<filter_list>", parsedName), Map.of(
                            "name", name
                    )));
                }

                continue;
            }

            lore.add(TextUtil.parseItem(line));
        }

        meta.lore(lore);
        meta.customName(TextUtil.parseItem(filterIcon.name()));

        stack.setItemMeta(meta);

        gui.setItem(filterIcon.slot(), new GuiItem(stack, action -> {
            if (action.isLeftClick()) {
                filter.updateAndGet(Filter::next);
            } else {
                filter.updateAndGet(Filter::previous);
            }

            updateClaims(gui, claims, filter, searchQuery);
            updateFilter(gui, claims, filter, searchQuery);

            gui.update();
        }));
    }

    private void updateClaims(@NotNull PaginatedGui gui, @NotNull List<Claim> claims, AtomicReference<Filter> filter, AtomicReference<Query> searchQuery) {
        gui.clearPageItems();

        for (Claim claim : claims) {
            switch (filter.get()) {
                case MAIN -> {
                    if (claim.main() != null) {
                        continue;
                    }
                }
                case SUB_CLAIMS -> {
                    if (claim.main() == null) {
                        continue;
                    }
                }
            }

            Query query = searchQuery.get();
            if (query != null) {
                switch (query.type()) {
                    case CLAIM_NAME -> {
                        if (!StringUtil.has(claim.name(), query.query())) {
                            continue;
                        }
                    }

                    case CLAIM_ID -> {
                        if (!StringUtil.has(claim.claimId().toString(), query.query())) {
                            continue;
                        }
                    }

                    case MAIN_CLAIM_NAME -> {
                        if (claim.main() == null || !StringUtil.has(claim.main().name(), query.query())) {
                            continue;
                        }
                    }

                    case MEMBER_NAME -> {
                        if (!StringUtil.listHas(claim.members().stream().map(ClaimMember::lastKnownName).toList(), query.query())) {
                            continue;
                        }
                    }
                }
            }

            gui.addItem(new GuiItem(buildClaimIcon(claim), action -> {
                Player player = (Player) action.getWhoClicked();

                if (action.isRightClick()) {
                    new RenameDialog()
                            .create(messagesHolder.get().dialogs(), claim.name())
                            .onSubmit(view -> {
                                String newName = view.getText("input");
                                if (newName == null) {
                                    return;
                                }

                                claim.rename(newName);

                                updateClaims(gui, claims, filter, searchQuery);
                                gui.update();
                            })
                            .show(player);

                    return;
                }

                guis.get(ClaimMemberListGui.class)
                        .open(player, claim);
            }));
        }
    }

    @NotNull
    private ItemStack buildClaimIcon(@NotNull Claim claim) {
        int totalMembers = claim.members().size();

        List<ClaimMember> sortedList = claim.members()
                .stream()
                .sorted(Comparator.comparing(member -> member.equals(claim.owner()), Comparator.reverseOrder())
                        .thenComparingLong(member -> ((ClaimMember) member).joinedTimestamp().getEpochSecond())
                )
                .limit(4)
                .toList();

        Map<String, String> claimPlaceholders = placeholders.claimInfo(claim);

        ClaimListConfig.ClaimsIcon icon = claim.main() == null ? claimsIcon : subClaimsIcon;
        ItemStack stack = icon.item();
        ItemMeta meta = stack.getItemMeta();

        List<Component> lore = new ArrayList<>();

        for (String line : icon.lore()) {
            if (line.toLowerCase().contains("<member_list>")) {
                ClaimMember owner = claim.owner();
                for (ClaimMember member : sortedList) {
                    lore.add(TextUtil.parseItem(line.replace("<member_list>", buildMemberRow(icon, member, member.uuid().equals(owner.uuid())))));
                }

                if (totalMembers > sortedList.size()) {
                    lore.add(TextUtil.parseItem(line.replace("<member_list>", "<remaining> more..."), Map.of(
                            "remaining", totalMembers - sortedList.size() + ""
                    )));
                }

                continue;
            }

            lore.add(TextUtil.parseItem(line, claimPlaceholders));
        }

        meta.customName(TextUtil.parseItem(claimsIcon.name(), claimPlaceholders));
        meta.lore(lore);

        stack.setItemMeta(meta);

        return stack;
    }

    @NotNull
    private String buildMemberRow(@NotNull ClaimListConfig.ClaimsIcon icon, @NotNull ClaimMember member, boolean isOwner) {
        OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(member.lastKnownName());

        String name;
        if (isOwner) {
            name = icon.owner();
        } else {
            name = icon.member();
        }

        String playerName;
        if (player == null || (playerName = player.getName()) == null || playerName.isEmpty()) {
            return buildHeadComponent(null) + " " + name.replace("<name>", "Unknown Player");
        }

        return buildHeadComponent(member.lastKnownName()) + " " + name.replace("<name>", playerName);
    }

    @NotNull
    @Contract(pure = true)
    private String buildHeadComponent(String name) {
        if (name == null) {
            return "<head:entity/player/wide/steve>";
        }

        return "<head:" + name + ">";
    }

    record Query(String query, QueryType type) {
    }

    enum QueryType {
        CLAIM_NAME("name"),
        CLAIM_ID("id"),
        MAIN_CLAIM_NAME("main"),
        MEMBER_NAME("member");

        private final static QueryType[] VALUES = values();

        private final String id;

        QueryType(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        @Nullable
        public static QueryType fromId(@NotNull String id) {
            return switch (id.toLowerCase()) {
                case "name" -> CLAIM_NAME;
                case "id" -> CLAIM_ID;
                case "main" -> MAIN_CLAIM_NAME;
                case "member" -> MEMBER_NAME;
                default -> null;
            };
        }

        public static List<String> all() {
            return Arrays.stream(VALUES).map(QueryType::id).collect(Collectors.toList());
        }
    }

    enum Filter {
        ALL,
        MAIN,
        SUB_CLAIMS;

        private final static Filter[] VALUES = values();

        public Filter next() {
            return VALUES[(this.ordinal() + 1) % VALUES.length];
        }

        public Filter previous() {
            return VALUES[(this.ordinal() - 1 + VALUES.length) % VALUES.length];
        }
    }
}