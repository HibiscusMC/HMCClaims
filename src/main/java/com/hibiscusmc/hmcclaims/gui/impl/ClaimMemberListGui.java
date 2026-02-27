package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.gui.BaseGui;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.util.ItemUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import team.unnamed.inject.Inject;

public class ClaimMemberListGui implements BaseGui {

    @Inject
    private GuiRegistry guis;

    @Override
    public void loadConfig() {

    }

    @Override
    public void open(Player player, Object... args) {
        Claim claim = (Claim) args[0];

        PaginatedGui gui = Gui.paginated()
                .title(TextUtil.parse("hi, claim name: " + claim.name()))
                .rows(2)
                .disableAllInteractions()
                .create();

        gui.setItem(1, new GuiItem(ItemUtil.build(Material.PAPER, "go back!"), action -> {
            guis.get(ClaimListGui.class)
                    .open(player);
        }));
        gui.open(player);
    }
}