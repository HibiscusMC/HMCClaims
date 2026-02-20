package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.ClaimRegion;
import com.hibiscusmc.hmcclaims.config.Guis;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.gui.BaseGui;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.Scheduler;
import com.hibiscusmc.hmcclaims.util.Text;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ClaimListGui implements BaseGui {

    private final static DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("MM/dd/yyyy")
            .withZone(ZoneId.systemDefault());

    @Inject
    private ConfigHolder<Guis.ClaimList> configHolder;
    @Inject
    private ConfigHolder<Settings> settingsHolder;

    @Inject
    private Scheduler scheduler;
    @Inject
    private Text text;

    @Inject
    private ClaimManager claimManager;
    @Inject
    private UserManager userManager;

    private Component title;

    private Guis.ClaimList.ClaimsIcon claimsIcon;
    private Guis.ClaimList.ChildClaimsIcon childClaimsIcon;

    private Guis.ClaimList.ClaimBlocksIcon claimBlocksIcon;
    private Guis.ClaimList.FilterIcon filterIcon;

    private Guis.SimpleIcon previousPage;
    private Guis.SimpleIcon nextPage;

    private List<Guis.Icon> icons;

    @Override
    public void loadConfig() {
        Guis.ClaimList config = configHolder.get();

        title = text.parse(config.title());

        claimsIcon = config.claimsIcon();
        childClaimsIcon = config.childClaimsIcon();

        claimBlocksIcon = config.claimBlocksIcon();
        filterIcon = config.filterIcon();

        previousPage = config.pages().get("previous-page");
        nextPage = config.pages().get("next-page");

        icons = config.extraIcons().values().stream().toList();
    }

    @Override
    public void open(Player player) {
        PaginatedGui gui = Gui.paginated()
                .title(title)
                .rows(5)
                .disableAllInteractions()
                .create();

        scheduler.scheduleAsync(() -> {
            buildIcons(player, gui);

            scheduler.schedule(() -> gui.open(player));
        });
    }

    private void buildIcons(Player player, @NotNull PaginatedGui gui) {
        gui.getFiller().fillBetweenPoints(5, 2, 5, 8, new GuiItem(ItemStack.of(Material.AIR)));

        if (previousPage != null) {
            gui.setItem(previousPage.slot(), new GuiItem(previousPage.item(), action -> gui.previous()));
        }

        if (nextPage != null) {
            gui.setItem(nextPage.slot(), new GuiItem(nextPage.item(), action -> gui.next()));
        }

        for (Guis.Icon icon : icons) {
            gui.setItem(icon.slot(), new GuiItem(icon.item(), action -> {
                if (action.isLeftClick()) {
                    for (Action iconAction : icon.leftClickActions()) {
                        iconAction.execute(player);
                    }
                } else {
                    for (Action iconAction : icon.rightClickActions()) {
                        iconAction.execute(player);
                    }
                }
            }));
        }

        UUID playerId = player.getUniqueId();
        User user = userManager.getUser(playerId)
                .orElseThrow(() -> new IllegalStateException("Data for player " + player.getName() + " not loaded yet"));

        gui.setItem(claimBlocksIcon.slot(), new GuiItem(buildClaimBlocksIcon(user)));

        AtomicReference<Filter> filter = new AtomicReference<>(Filter.ALL);
        List<Claim> claims = claimManager.getPlayerClaims(playerId);

        updateClaims(gui, claims, filter);
        updateFilter(gui, claims, filter);
    }

    private void updateFilter(PaginatedGui gui, List<Claim> claims, @NotNull AtomicReference<Filter> filter) {
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

                    lore.add(Text.parseItem(line.replace("<filter_list>", parsedName), Map.of(
                            "name", name
                    )));
                }

                continue;
            }

            lore.add(Text.parseItem(line));
        }

        meta.lore(lore);
        meta.customName(Text.parseItem(filterIcon.name()));

        stack.setItemMeta(meta);

        gui.setItem(filterIcon.slot(), new GuiItem(stack, action -> {
            if (action.isLeftClick()) {
                filter.updateAndGet(Filter::next);
            } else {
                filter.updateAndGet(Filter::previous);
            }

            updateClaims(gui, claims, filter);
            updateFilter(gui, claims, filter);

            gui.update();
        }));
    }

    private void updateClaims(@NotNull PaginatedGui gui, @NotNull List<Claim> claims, AtomicReference<Filter> filter) {
        gui.clearPageItems();

        for (Claim claim : claims.stream().filter(claim -> switch (filter.get()) {
            case ALL -> true;
            case PARENT -> claim.parent() == null;
            case CHILDREN -> claim.parent() != null;
        }).toList()) {
            gui.addItem(new GuiItem(buildClaimIcon(claim)));
        }
    }

    @NotNull
    private ItemStack buildClaimIcon(@NotNull Claim claim) {
        Settings settings = settingsHolder.get();

        String shortId = claim.claimId().toString().split("-")[0];
        int totalChildren = claim.children().size();

        ClaimRegion region = claim.region();
        String worldName = region.worldName();

        int totalMembers = claim.members().size();

        List<ClaimMember> sortedList = claim.members().values()
                .stream()
                .sorted(Comparator.comparingLong(member -> member.joinedTimestamp().getEpochSecond()))
                .toList()
                .subList(0, Math.min(4, totalMembers));

        Map<String, String> placeholders = new HashMap<>();
        placeholders.putAll(Map.of(
                "name", claim.name(),
                "short_id", shortId,
                "locked", claim.locked() ? "Yes" : "No",
                "total_children", totalChildren + "",
                "parent_claim", claim.parent() != null ? claim.parent().name() : "",
                "inherits_permissions", claim.inheritPermissions() ? "Yes" : "No",
                "world", settings.worldAliases().getOrDefault(worldName, worldName),
                "x", region.maxX() + "",
                "z", region.maxZ() + "",
                "surface_area", region.getSurfaceArea() + ""
        ));
        placeholders.putAll(Map.of(
                "total_x", ((region.maxX() - region.minX()) + 1) + "",
                "total_z", ((region.maxZ() - region.minZ()) + 1) + "",
                "member_count", totalMembers + "",
                "creation_date", FORMATTER.format(claim.claimedTimestamp())
        ));

        Guis.ClaimList.ClaimsIcon icon = claim.parent() == null ? claimsIcon : childClaimsIcon;
        ItemStack stack = icon.item();
        ItemMeta meta = stack.getItemMeta();

        List<Component> lore = new ArrayList<>();

        for (String line : icon.lore()) {
            if (line.toLowerCase().contains("<member_list>")) {
                ClaimMember owner = claim.owner();
                for (ClaimMember member : sortedList) {
                    lore.add(Text.parseItem(line.replace("<member_list>", buildMemberRow(icon, member, member.equals(owner)))));
                }

                if (totalMembers > sortedList.size()) {
                    lore.add(Text.parseItem(line.replace("<member_list>", "<remaining> more..."), Map.of(
                            "remaining", totalMembers - sortedList.size() + ""
                    )));
                }

                continue;
            }

            lore.add(Text.parseItem(line, placeholders));
        }

        meta.customName(Text.parseItem(claimsIcon.name(), placeholders));
        meta.lore(lore);

        stack.setItemMeta(meta);

        return stack;
    }

    @NotNull
    private ItemStack buildClaimBlocksIcon(@NotNull User user) {
        long startingBlocks = settingsHolder.get().claimBlocks().startingAmount();
        long obtainedBlocks = user.claimBlocks();
        long totalBlocks = startingBlocks + obtainedBlocks;

        long availableBlocks = userManager.getRemainingBlocks(user);
        long usedBlocks = totalBlocks - availableBlocks;

        Map<String, String> placeholders = Map.of(
                "starting_blocks", startingBlocks + "",
                "obtained_blocks", obtainedBlocks + "",
                "total_blocks", totalBlocks + "",
                "used_blocks", usedBlocks + "",
                "available_blocks", availableBlocks + ""
        );

        ItemStack stack = claimBlocksIcon.item();
        ItemMeta meta = stack.getItemMeta();

        meta.lore(claimBlocksIcon.lore().stream().map(lore -> Text.parseItem(lore, placeholders)).toList());
        meta.customName(Text.parseItem(claimBlocksIcon.name(), placeholders));

        stack.setItemMeta(meta);
        return stack;
    }

    @NotNull
    private String buildMemberRow(Guis.ClaimList.ClaimsIcon icon, @NotNull ClaimMember member, boolean isOwner) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(member.uuid());

        String name;
        if (isOwner) {
            name = icon.owner();
        } else {
            name = icon.member();
        }

        String playerName = player.getName();
        if (playerName == null || playerName.isEmpty()) {
            return buildHeadComponent(null) + " " + name.replace("<name>", "Unknown Player");
        }

        return buildHeadComponent(member.uuid()) + " " + name.replace("<name>", playerName);
    }

    @NotNull
    @Contract(pure = true)
    private String buildHeadComponent(UUID uuid) {
        if (uuid == null) {
            return "<head:entity/player/wide/steve>";
        }

        return "<head:" + uuid + ">";
    }

    enum Filter {
        ALL,
        PARENT,
        CHILDREN;

        private static final Filter[] VALUES = values();

        public Filter next() {
            return VALUES[(this.ordinal() + 1) % VALUES.length];
        }

        public Filter previous() {
            return VALUES[(this.ordinal() - 1 + VALUES.length) % VALUES.length];
        }
    }
}