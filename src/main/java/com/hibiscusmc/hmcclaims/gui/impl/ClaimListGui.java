package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.ClaimRegion;
import com.hibiscusmc.hmcclaims.config.Guis;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.gui.BaseGui;
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
import team.unnamed.inject.Inject;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClaimListGui implements BaseGui {

    private final static DateTimeFormatter FORMATTER
            = DateTimeFormatter.ofPattern("MM/dd/yyyy")
            .withZone(ZoneId.systemDefault());

    @Inject
    private ConfigHolder<Guis.ClaimList> configHolder;

    @Inject
    private Scheduler scheduler;
    @Inject
    private Text text;

    @Inject
    private ClaimManager claimManager;

    private Component title;

    @Override
    public void loadConfig() {
        Guis.ClaimList config = configHolder.get();

        title = text.parse(config.title());
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

    private void buildIcons(Player player, PaginatedGui gui) {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setHideTooltip(true);
        filler.setItemMeta(fillerMeta);

        gui.getFiller().fillBetweenPoints(1, 1, 1, 9, new GuiItem(ItemStack.of(Material.AIR)));
        gui.getFiller().fillBetweenPoints(2, 1, 2, 9, new GuiItem(filler));
        gui.getFiller().fillBetweenPoints(5, 2, 5, 8, new GuiItem(filler));

        gui.setItem(36, new GuiItem(ItemStack.of(Material.ARROW), action -> gui.previous()));
        gui.setItem(44, new GuiItem(ItemStack.of(Material.ARROW), action -> gui.next()));

        fillClaims(player, gui);
    }

    private void fillClaims(Player player, PaginatedGui gui) {
        List<Claim> claims = claimManager.getPlayerClaims(player.getUniqueId());

        for (Claim claim : claims) {
            gui.addItem(new GuiItem(buildClaimIcon(claim)));
        }
    }

    private ItemStack buildClaimIcon(Claim claim) {
        ItemStack stack = ItemStack.of(claim.parent() == null ? Material.GRASS_BLOCK : Material.DIRT);
        ItemMeta meta = stack.getItemMeta();

        List<Component> lore = new ArrayList<>();
        String shortId = claim.claimId().toString().split("-")[0];
        lore.add(text.parseItem("<dark_gray>ID: " + shortId));
        lore.add(text.parseItem(""));

        lore.add(text.parseItem("<gray>Details:"));
        lore.add(text.parseItem(" <dark_gray>- <gray>Private: <white>" + (claim.locked() ? "Yes" : "No")));

        if (claim.parent() != null) {
            lore.add(text.parseItem(" <dark_gray>- <gray>Parent Claim: <white>" + claim.parent().name()));
            lore.add(text.parseItem(" <dark_gray>- <gray>Inherits Permissions: <white>" + (claim.inheritPermissions() ? "Yes" : "No")));
        } else {
            int totalChildren = claim.children().size();

            lore.add(text.parseItem(" <dark_gray>- <gray>Children <dark_gray>(<total_children>)</dark_gray>:", Map.of(
                    "total_children", totalChildren + ""
            )));

            List<Claim> trimmedList = claim.children()
                    .stream()
                    .toList()
                    .subList(0, Math.min(4, totalChildren));
            for (Claim child : trimmedList) {
                lore.add(text.parseItem(" <gray> - <white>" + child.name()));
            }

            if (totalChildren > trimmedList.size()) {
                lore.add(text.parseItem(" <gray> - <white><remaining> more...", Map.of(
                        "remaining", totalChildren - trimmedList.size() + ""
                )));
            }
        }

        lore.add(text.parseItem(""));

        ClaimRegion region = claim.region();

        lore.add(text.parseItem("<gray>Region:"));
        lore.add(text.parseItem(" <dark_gray>- <gray>World: <white>" + region.worldName()));
        lore.add(text.parseItem(" <dark_gray>- <gray>Location: <white>X: <x>, Z: <z>", Map.of(
                "x", region.maxX() + "",
                "z", region.maxZ() + ""
        )));
        lore.add(text.parseItem(" <dark_gray>- <gray>Area: <white><surface_area> <dark_gray>(<total_x>x<total_z>)", Map.of(
                "surface_area", region.getSurfaceArea() + "",
                "total_x", ((region.maxX() - region.minX()) + 1) + "",
                "total_z", ((region.maxZ() - region.minZ()) + 1) + ""
        )));
        lore.add(text.parseItem(""));

        int totalMembers = claim.members().size();
        lore.add(text.parseItem("<gray>Members <dark_gray>(<member_count>)</dark_gray>:", Map.of(
                "member_count", totalMembers + ""
        )));

        ClaimMember owner = claim.owner();
        lore.add(text.parseItem(buildMemberRow(owner, true)));

        List<ClaimMember> sortedList = claim.members().values()
                .stream()
                .toList()
                .reversed()
                .subList(0, Math.min(4, totalMembers));

        for (ClaimMember member : sortedList) {
            if (member.equals(owner)) {
                continue;
            }

            lore.add(text.parseItem(buildMemberRow(member)));
        }

        if (totalMembers > sortedList.size()) {
            lore.add(text.parseItem(" <gray> - <white><remaining> more...", Map.of(
                    "remaining", totalMembers - sortedList.size() + ""
            )));
        }

        lore.add(text.parseItem(""));
        lore.add(text.parseItem("<gray>Creation date: <white>" + FORMATTER.format(claim.claimedTimestamp())));

        meta.displayName(text.parseItem("<gray>Name: <white>" + claim.name()));
        meta.lore(lore);

        stack.setItemMeta(meta);
        return stack;
    }

    private String buildMemberRow(ClaimMember member) {
        return buildMemberRow(member, false);
    }

    private String buildMemberRow(ClaimMember member, boolean isOwner) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(member.uuid());

        String playerName = player.getName();
        if (playerName == null || playerName.isEmpty()) {
            return " <gray>- <white>" + buildHeadComponent(null) + " Unknown Player";
        }

        String name;
        if (isOwner) {
            name = "<b>" + playerName + "</b> <sprite:blocks:item/nether_star>";
        } else {
            name = playerName;
        }

        return " <gray>- <white>" + buildHeadComponent(member.uuid()) + " " + name;
    }

    private String buildHeadComponent(UUID uuid) {
        if (uuid == null) {
            return "<head:entity/player/wide/steve>";
        }

        return "<head:" + uuid + ">";
    }
}