package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.config.gui.ClaimMemberListConfig;
import com.hibiscusmc.hmcclaims.config.gui.GuisTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.gui.BaseGui;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.util.ItemUtil;
import com.hibiscusmc.hmcclaims.util.RangeUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class ClaimMemberListGui implements BaseGui {

    @Inject
    private ConfigHolder<ClaimMemberListConfig> configHolder;

    @Inject
    private GuiRegistry guis;

    private String title;
    private int rows = 1;
    private List<Integer> slots = new ArrayList<>();

    private GuisTemplate.DynamicIcon memberIcon;

    private ClaimMemberListConfig.FilterIcon filterIcon;
    private GuisTemplate.SimpleIcon searchIcon;
    private GuisTemplate.SimpleIcon addMemberIcon;

    private GuisTemplate.SimpleIcon backIcon;
    private GuisTemplate.SimpleIcon previousPage;
    private GuisTemplate.SimpleIcon nextPage;
    private GuisTemplate.SimpleIcon deleteIcon;

    private GuisTemplate.SimpleIcon membersTab;
    private GuisTemplate.SimpleIcon rolesTab;
    private GuisTemplate.SimpleIcon settingsTab;
    private GuisTemplate.SimpleIcon manageTab;

    private List<GuisTemplate.Icon> icons;

    @Override
    public void loadConfig() {
        ClaimMemberListConfig config = configHolder.get();

        title = config.title();
        rows = config.rows();

        searchIcon = config.searchIcon();
        filterIcon = config.filterIcon();
        memberIcon = config.memberIcon();
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
    public void open(Player player, Object... args) {
        Claim claim = (Claim) args[0];

        PaginatedGui gui = Gui.paginated()
                .title(TextUtil.parse(title, Map.of(
                        "claim_name", claim.name()
                )))
                .pageSize(slots.size())
                .rows(rows)
                .disableAllInteractions()
                .create();

        GuiItem air = new GuiItem(ItemStack.of(Material.AIR));
        for (int i = 0; i < rows * 9; i++) {
            if (!slots.contains(i)) {
                gui.setItem(i, air);
            }
        }

        gui.getFiller().fillTop(air);

        gui.setItem(backIcon.slot(), new GuiItem(backIcon.item(), action -> {
            guis.get(ClaimListGui.class)
                    .open(player);
        }));

        for (GuisTemplate.Icon icon : icons) {
            gui.setItem(icon.slot(), new GuiItem(icon.item(), action -> {
                for (Action iconAction : action.isLeftClick() ? icon.leftClickActions() : icon.rightClickActions()) {
                    iconAction.execute(player);
                }
            }));
        }

        gui.setItem(searchIcon.slot(), new GuiItem(searchIcon.item()));
        gui.setItem(addMemberIcon.slot(), new GuiItem(addMemberIcon.item()));
        gui.setItem(filterIcon.slot(), new GuiItem(filterIcon.item()));

        gui.setItem(membersTab.slot(), new GuiItem(membersTab.item()));
        gui.setItem(rolesTab.slot(), new GuiItem(rolesTab.item()));
        gui.setItem(settingsTab.slot(), new GuiItem(settingsTab.item()));
        gui.setItem(manageTab.slot(), new GuiItem(manageTab.item()));

        gui.setItem(backIcon.slot(), new GuiItem(backIcon.item()));
        gui.setItem(deleteIcon.slot(), new GuiItem(deleteIcon.item()));

        for (ClaimMember member : claim.members().values()) {
            gui.addItem(new GuiItem(ItemUtil.buildHeadWithName(member.lastKnownName())));
        }

        gui.setItem(previousPage.slot(), new GuiItem(previousPage.item()));
        gui.setItem(nextPage.slot(), new GuiItem(nextPage.item()));

        gui.open(player);
    }
}