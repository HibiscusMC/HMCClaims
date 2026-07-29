package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.gui.ClaimDeleteConfig;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.gui.BaseGui;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.util.PlaceholderUtil;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Singleton
public class ClaimDeleteGui extends ClaimListGui {

    @Inject
    private ConfigHolder<ClaimDeleteConfig> configHolder;
    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private ClaimManager claimManager;

    @Inject
    private GuiRegistry guis;

    @Inject
    private TextUtil text;
    @Inject
    private PlaceholderUtil placeholders;
    @Inject
    private SchedulerUtil scheduler;

    private String title;
    private int rows = 1;

    protected GuiTemplate.GuiScreenType screenType;

    private List<GuiTemplate.Icon> icons;

    private ClaimDeleteConfig.ClaimIcon claimIcon;
    private ClaimDeleteConfig.SubClaimIcon subClaimIcon;
    private GuiTemplate.SimpleIcon confirm;
    private GuiTemplate.SimpleIcon cancel;

    @Override
    public void loadConfig() {
        ClaimDeleteConfig config = configHolder.get();
        if (config == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        title = config.title();
        rows = config.rows();

        icons = config.extraIcons().values().stream().toList();

        claimIcon = config.claimIcon();
        subClaimIcon = config.subClaimIcon();
        confirm = config.confirm();
        cancel = config.cancel();

        screenType = config.screenType();
        if (screenType == GuiTemplate.GuiScreenType.FULL) {
            super.loadConfig(config.lowerGui());
        }
    }

    @Override
    public void open(@NotNull Player player, @NotNull GuiMetadata metadata) {
        Claim claim = metadata.claim();

        scheduler.scheduleAsync(() -> {
            Gui.Builder<?, ?> gui = Gui.builder();
            CharList structure = new CharArrayList();
            for (int i = 0; i < rows * 9; i++) {
                structure.add('#');
            }

            Char2ObjectArrayMap<GuiTemplate.Icon> mappedIcons = new Char2ObjectArrayMap<>();
            for (GuiTemplate.Icon icon : icons) {
                char codePoint = (char) (FIRST_SAFE_CHAR + icons.indexOf(icon));

                structure.set(icon.slot(), codePoint);
                mappedIcons.put(codePoint, icon);
            }

            structure.set(confirm.slot(), '(');
            structure.set(claimIcon.slot(), '*');
            structure.set(cancel.slot(), ')');

            String[] structureArray = new String[rows];
            for (int r = 0; r < rows; r++) {
                CharList rowList = structure.subList(r * 9, (r + 1) * 9);

                structureArray[r] = new String(rowList.toCharArray());
            }

            gui.setStructure(structureArray);

            for (Char2ObjectMap.Entry<GuiTemplate.Icon> entry : mappedIcons.char2ObjectEntrySet()) {
                GuiTemplate.Icon icon = entry.getValue();

                gui.addIngredient(entry.getCharKey(), Item.builder()
                        .setItemProvider(icon.item())
                        .addClickHandler(click -> (switch (click.clickType()) {
                            case LEFT -> icon.leftClickActions();
                            case RIGHT -> icon.rightClickActions();
                            default -> List.<Action>of();
                        }).forEach(action -> action.execute(player)))
                        .build());
            }

            Gui lowerGui = metadata.claimsGui() != null ? metadata.claimsGui() : screenType == GuiTemplate.GuiScreenType.FULL ? buildLowerGui(player, metadata) : null;
            if (metadata.claimsGui() == null) {
                metadata.claimsGui(lowerGui);
            }

            gui.addIngredient('(', Item.builder()
                    .setItemProvider(confirm.item())
                    .addClickHandler(click -> {
                        claimManager.deleteClaim(claim);
                        text.send(player, messagesHolder.get().claims().deleted(), Map.of(
                                "claim_name", claim.name()
                        ));

                        if (metadata.previousPage() != null) {
                            guis.get(ClaimListGui.class).open(player);
                            return;
                        }

                        player.closeInventory();
                    })
                    .build());

            gui.addIngredient(')', Item.builder()
                    .setItemProvider(cancel.item())
                    .addClickHandler(click -> {
                        if (metadata.previousPage() != null) {
                            Class<? extends BaseGui> manage = claim.main() == null ? ClaimManageGui.class : SubClaimManageGui.class;

                            guis.get(manage)
                                    .open(player, metadata);
                            return;
                        }

                        player.closeInventory();
                    })
                    .build());

            gui.addIngredient('*', Item.builder()
                    .setItemProvider(buildClaimIcon(claim))
                    .build());

            Gui upperGui = gui.build();

            scheduler.schedule(() -> {
                Window.Builder.Normal.Split window = Window.builder()
                        .setTitle(TextUtil.parse(title))
                        .setUpperGui(upperGui);

                if (metadata.previousPage() != null) {
                    window.setFallbackWindow(metadata.previousPage());
                }

                if (lowerGui != null) {
                    window.setLowerGui(lowerGui);
                }

                window.open(player);
            });
        });
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

        ClaimDeleteConfig.ClaimIcon icon = claim.main() == null ? claimIcon : subClaimIcon;
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

        meta.customName(TextUtil.parseItem(icon.name(), claimPlaceholders));
        meta.lore(lore);

        stack.setItemMeta(meta);

        return stack;
    }

    @NotNull
    private String buildMemberRow(@NotNull ClaimDeleteConfig.ClaimIcon icon, @NotNull ClaimMember member, boolean isOwner) {
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
}