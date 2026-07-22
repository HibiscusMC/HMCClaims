package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.gui.BaseListGuiConfig;
import com.hibiscusmc.hmcclaims.config.gui.ClaimListConfig;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.dialog.type.RenameDialog;
import com.hibiscusmc.hmcclaims.dialog.type.SearchDialog;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.gui.BaseGui;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.PlaceholderUtil;
import com.hibiscusmc.hmcclaims.util.RangeUtil;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.StringUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.inject.Inject;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.BoundItem;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@SuppressWarnings({"UnstableApiUsage"})
public class ClaimListGui implements BaseGui {

    protected final static int FIRST_SAFE_CHAR = 46; // "."

    @Inject
    private ConfigHolder<ClaimListConfig> configHolder;
    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private StorageHolder storageHolder;

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
    private int rows = 4;

    private BaseListGuiConfig.ClaimsIcon claimsIcon;
    private BaseListGuiConfig.SubClaimsIcon subClaimsIcon;

    private GuiTemplate.SearchIcon searchIcon;
    private GuiTemplate.FilterIcon filterIcon;

    private GuiTemplate.SimpleIcon previousPage;
    private GuiTemplate.SimpleIcon nextPage;

    private List<GuiTemplate.Icon> icons;

    @Override
    public void loadConfig() {
        ClaimListConfig config = configHolder.get();

        if (config == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        title = TextUtil.parse(config.title());
        rows = config.rows();

        loadConfig(config);
    }

    protected void loadConfig(BaseListGuiConfig lowerConfig) {
        claimsIcon = lowerConfig.claimsIcon();
        subClaimsIcon = lowerConfig.subClaimsIcon();

        searchIcon = lowerConfig.searchIcon();
        filterIcon = lowerConfig.filterIcon();

        previousPage = lowerConfig.pages().get("previous-page");
        nextPage = lowerConfig.pages().get("next-page");

        icons = lowerConfig.extraIcons().values().stream().toList();

        slots.clear();
        for (RangeUtil range : lowerConfig.validSlots()) {
            for (int slot : range.all()) {
                slots.add(slot);
            }
        }
    }

    @Override
    public void open(@NotNull Player player) {
        scheduler.scheduleAsync(() -> {
            Window.Builder.Normal.Split window = Window.builder()
                    .setTitle(title)
                    .setUpperGui(buildLowerGui(player, null));

            scheduler.schedule(() -> window.open(player));
        });
    }

    protected Gui buildLowerGui(@NotNull Player player, @Nullable GuiMetadata guiMetadata) {
        if (guiMetadata != null && guiMetadata.claimsGui() != null) {
            return guiMetadata.claimsGui(); // return the cached gui instead of re-creating a new one.
        }

        PagedGui.Builder<Item> pagedGui = PagedGui.itemsBuilder();

        List<String> structure = new ArrayList<>(Collections.nCopies(rows * 9, "#"));
        structure.set(previousPage.slot(), "(");
        structure.set(nextPage.slot(), ")");

        for (int slot : slots) {
            structure.set(slot, "-");
        }

        structure.set(filterIcon.slot(), "%");
        structure.set(searchIcon.slot(), "&");

        Map<Integer, GuiTemplate.Icon> mappedIcons = new HashMap<>();
        for (GuiTemplate.Icon icon : icons) {
            int codePoint = FIRST_SAFE_CHAR + icons.indexOf(icon);

            structure.set(icon.slot(), Character.toString(codePoint));
            mappedIcons.put(codePoint, icon);
        }

        String[] structureArray = new String[rows];
        for (int r = 0; r < rows; r++) {
            List<String> rowList = structure.subList(r * 9, (r + 1) * 9);

            structureArray[r] = String.join("", rowList);
        }

        pagedGui.setStructure(structureArray);

        for (Map.Entry<Integer, GuiTemplate.Icon> entry : mappedIcons.entrySet()) {
            GuiTemplate.Icon icon = entry.getValue();

            pagedGui.addIngredient((char) entry.getKey().intValue(), Item.builder()
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

        pagedGui.addIngredient('(', BoundItem.pagedBuilder()
                .setItemProvider(new ItemBuilder(previousPage.item()))
                .addClickHandler((item, gui, click) -> gui.setPage(gui.getPage() - 1))
                .build());
        pagedGui.addIngredient(')', BoundItem.pagedBuilder()
                .setItemProvider(new ItemBuilder(nextPage.item()))
                .addClickHandler((item, gui, click) -> gui.setPage(gui.getPage() + 1))
                .build());

        Metadata metadata = new Metadata(
                new AtomicReference<>(Filter.ALL),
                new AtomicReference<>(null)
        );

        AtomicReference<PagedGui<Item>> guiReference = new AtomicReference<>(null);

        Set<Claim> claims = claimManager.getPlayerClaims(player.getUniqueId());
        Map<Claim, Item> parsedClaims = buildClaims(guiReference, claims, metadata, guiMetadata);

        Runnable updateClaims = () -> updateClaims(guiReference.get(), parsedClaims, metadata);
        pagedGui.addIngredient('%', buildFilter(metadata, updateClaims));
        pagedGui.addIngredient('&', buildSearch(metadata, updateClaims));

        pagedGui.addIngredient('-', Markers.CONTENT_LIST_SLOT_HORIZONTAL);

        PagedGui<Item> gui = pagedGui.build();
        guiReference.set(gui);

        updateClaims.run();

        return gui;
    }

    private Item buildSearch(@NotNull Metadata metadata, Runnable update) {
        return Item.builder()
                .setItemProvider(player -> {
                    ItemStack item = searchIcon.item();
                    item.editMeta(meta -> {
                        Query searchQuery = metadata.searchQuery().get();
                        String query = searchIcon.noQuery();

                        if (searchQuery != null && !searchQuery.query().isEmpty()) {
                            query = searchQuery.query();
                        }

                        meta.lore(TextUtil.parseItemLore(searchIcon.lore(), Map.of(
                                "query", query
                        )));
                    });

                    return new ItemWrapper(item);
                })
                .addClickHandler((it, click) -> new SearchDialog(QueryType.all())
                        .create(messagesHolder.get().dialogs())
                        .onSubmit(view -> {
                            String query = view.getText("query");
                            String rawType = view.getText("option");
                            assert rawType != null;

                            QueryType type = QueryType.fromId(rawType);
                            assert type != null;

                            metadata.searchQuery().set(new Query(query, type));

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

    private Item buildFilter(@NotNull Metadata metadata, Runnable update) {
        return Item.builder()
                .setItemProvider(player -> {
                    ItemStack stack = filterIcon.item();
                    ItemMeta meta = stack.getItemMeta();

                    List<Component> lore = new ArrayList<>();
                    Filter currentOption = metadata.filter().get();

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

                    return new ItemWrapper(stack);
                })
                .addClickHandler((it, click) -> {
                    if (click.clickType() == ClickType.LEFT) {
                        metadata.filter().updateAndGet(Filter::next);
                    } else if (click.clickType() == ClickType.RIGHT) {
                        metadata.filter().updateAndGet(Filter::previous);
                    }

                    it.notifyWindows();
                    update.run();
                })
                .build();
    }

    private void updateClaims(@NotNull PagedGui<Item> gui, @NotNull Map<Claim, Item> claims, @NotNull Metadata metadata) {
        List<Item> items = new ArrayList<>();

        for (Map.Entry<Claim, Item> entry : claims.entrySet()) {
            Claim claim = entry.getKey();
            Item item = entry.getValue();

            switch (metadata.filter().get()) {
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

            Query query = metadata.searchQuery().get();
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

            items.add(item);
        }

        gui.setContent(items);
    }

    @NotNull
    private Map<Claim, Item> buildClaims(@NotNull AtomicReference<PagedGui<Item>> guiReference, @NotNull Set<Claim> claims, @NotNull Metadata metadata, @Nullable GuiMetadata guiMetadata) {
        Map<Claim, Item> parsedClaims = new HashMap<>();

        for (Claim claim : claims) {
            parsedClaims.put(claim, Item.builder()
                    .setItemProvider(stack -> new ItemWrapper(buildClaimIcon(claim)))
                    .addClickHandler((it, click) -> {
                        Player player = click.player();

                        if (click.clickType() == ClickType.RIGHT) {
                            new RenameDialog()
                                    .create(messagesHolder.get().dialogs(), claim.name())
                                    .onSubmit(view -> {
                                        String newName = view.getText("input");
                                        if (newName == null) {
                                            return;
                                        }

                                        claim.rename(newName);
                                        storageHolder.get().claims()
                                                .saveClaimName(claim);

                                        it.notifyWindows();
                                        updateClaims(guiReference.get(), parsedClaims, metadata);
                                    })
                                    .show(player);

                            return;
                        }

                        guis.get(ClaimMemberListGui.class)
                                .open(player, guiMetadata == null ? new GuiMetadata(claim) : guiMetadata.claim(claim));
                    })
                    .build());
        }

        return parsedClaims;
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
                for (ClaimMember member : sortedList) {
                    lore.add(TextUtil.parseItem(line.replace("<member_list>", buildMemberRow(icon, member, member.uuid().equals(claim.owner())))));
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
    private String buildMemberRow(@NotNull BaseListGuiConfig.ClaimsIcon icon, @NotNull ClaimMember member, boolean isOwner) {
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

    record Metadata(AtomicReference<Filter> filter, AtomicReference<Query> searchQuery) {
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