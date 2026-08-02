package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.config.gui.ClaimMemberManageConfig;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.gui.BaseGui;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.inject.Inject;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemWrapper;

import java.time.Instant;

public abstract class ClaimMemberManageGui implements BaseGui {

    protected final static int FIRST_SAFE_CHAR = 33; // "!"

    @Inject
    private GuiRegistry guis;

    private GuiTemplate.SimpleIcon kickIcon;
    private ItemStack cantKickIcon;

    private GuiTemplate.SimpleIcon banIcon;
    private ItemStack cantBanIcon;

    private GuiTemplate.SimpleIcon backIcon;

    private Int2ObjectMap<Item> extraItems;

    protected void loadConfig(@Nullable ClaimMemberManageConfig.LowerGui lowerGui) {
        if (lowerGui == null) {
            return;
        }

        kickIcon = lowerGui.kickIcon();
        cantKickIcon = lowerGui.cantKickIcon();

        banIcon = lowerGui.banIcon();
        cantBanIcon = lowerGui.cantBanIcon();

        backIcon = lowerGui.backIcon();

        extraItems = parseExtraItems(lowerGui.extraIcons());
    }

    protected Gui build(@NotNull Player player, @NotNull GuiMetadata metadata) {
        Claim claim = metadata.claim();
        ClaimMember target = metadata.member();

        Gui.Builder<?, ?> guiBuilder = Gui
                .builder();

        CharList structure = new CharArrayList();
        int rows = 4;

        for (int i = 0; i < rows * 9; i++) {
            structure.add('#');
        }

        structure.set(kickIcon.slot(), (char) 0);
        structure.set(banIcon.slot(), (char) 1);
        structure.set(backIcon.slot(), (char) 2);

        int currentPoint = 3;
        Char2ObjectMap<Item> itemMap = new Char2ObjectOpenHashMap<>();
        for (Int2ObjectMap.Entry<Item> extraItem : extraItems.int2ObjectEntrySet()) {
            structure.set(extraItem.getIntKey(), (char) currentPoint);

            itemMap.put((char) currentPoint, extraItem.getValue());
            currentPoint++;
        }

        guiBuilder.setStructure(parseStructure(structure, rows));

        itemMap.char2ObjectEntrySet().forEach(entry -> guiBuilder.addIngredient(
                entry.getCharKey(), entry.getValue()
        ));

        guiBuilder.addIngredient((char) 0, buildKickItem(player, claim, target, kickIcon, cantKickIcon));
        guiBuilder.addIngredient((char) 1, buildBanItem(player, claim, target, banIcon, cantBanIcon));
        guiBuilder.addIngredient((char) 2, Item.builder()
                .setItemProvider(backIcon.item())
                .addClickHandler(click -> guis.get(ClaimMemberListGui.class).open(player, metadata))
                .build());

        return guiBuilder.build();
    }

    @NotNull
    @Contract(pure = true)
    protected Item buildKickItem(
            @NotNull Player player, @NotNull Claim claim, @NotNull ClaimMember target,
            @NotNull GuiTemplate.SimpleIcon kickIcon, @NotNull ItemStack cantKickIcon
    ) {
        boolean canKick = claim.getMember(player.getUniqueId())
                .map(member -> member.canManage(target) && member.hasPermission(Permission.MANAGE_MEMBERS))
                .orElse(false);

        return Item.builder()
                .setItemProvider(p -> new ItemWrapper(
                        canKick ?
                                kickIcon.item() :
                                cantKickIcon
                ))
                .addClickHandler(click -> {
                    if (!canKick) {
                        return;
                    }

                    claim.removeMember(target.uuid());

                    guis.get(ClaimMemberListGui.class)
                            .open(player, new GuiMetadata(claim));
                })
                .build();
    }

    @NotNull
    @Contract(pure = true)
    protected Item buildBanItem(
            @NotNull Player player, @NotNull Claim claim, @NotNull ClaimMember target,
            @NotNull GuiTemplate.SimpleIcon banIcon, @NotNull ItemStack cantBanIcon
    ) {
        boolean canBan = claim.getMember(player.getUniqueId())
                .map(member -> member.canManage(target) && member.hasPermission(Permission.BAN_MEMBERS))
                .orElse(false);

        return Item.builder()
                .setItemProvider(p -> new ItemWrapper(
                        canBan ?
                                banIcon.item() :
                                cantBanIcon
                ))
                .addClickHandler(click -> {
                    if (!canBan) {
                        return;
                    }

                    target.role(claim.roleRegistry().defaultRole());
                    target.permissions(new Object2BooleanArrayMap<>());
                    target.joinedTimestamp(Instant.now());
                    target.banned(true);

                    guis.get(ClaimMemberListGui.class)
                            .open(player, new GuiMetadata(claim));
                })
                .build();
    }
}