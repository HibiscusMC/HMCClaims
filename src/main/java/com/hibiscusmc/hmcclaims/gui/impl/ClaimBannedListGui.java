package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.gui.ClaimBannedListConfig;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.gui.BaseGui;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.input.Input;
import com.hibiscusmc.hmcclaims.input.InputManager;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.ItemUtil;
import com.hibiscusmc.hmcclaims.util.MapUtil;
import com.hibiscusmc.hmcclaims.util.PlaceholderUtil;
import com.hibiscusmc.hmcclaims.util.RangeUtil;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.StringUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
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
import xyz.xenondevs.invui.util.TriConsumer;
import xyz.xenondevs.invui.window.Window;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class ClaimBannedListGui extends ClaimListGui {

    @Inject
    private ConfigHolder<ClaimBannedListConfig> configHolder;
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

    private GuiTemplate.DynamicIcon memberIcon;

    private GuiTemplate.SimpleIcon banMemberIcon;

    private GuiTemplate.SimpleIcon previousPage;
    private GuiTemplate.SimpleIcon nextPage;

    private GuiTemplate.SimpleIcon membersTab;
    private GuiTemplate.SimpleIcon rolesTab;
    private GuiTemplate.SimpleIcon settingsTab;
    private GuiTemplate.SimpleIcon manageTab;

    private List<GuiTemplate.Icon> icons;

    @Override
    public void loadConfig() {
        ClaimBannedListConfig config = configHolder.get();

        if (config == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        title = config.title();
        rows = config.rows();

        memberIcon = config.memberIcon();

        banMemberIcon = config.banMemberIcon();

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
    public void open(@NotNull Player player, @NotNull GuiMetadata metadata) {
        Claim claim = metadata.claim();

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
            structure.set(banMemberIcon.slot(), '+');

            for (int slot : slots) {
                structure.set(slot, '-');
            }

            Class<? extends BaseGui> currentClass = getClass();
            TriConsumer<Gui.Builder<?, ?>, GuiRegistry, Player> tabsBuilder = buildTabs(
                    structure, currentClass, claim, metadata,
                    membersTab, rolesTab, settingsTab, manageTab
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
                        .addClickHandler(click -> (switch (click.clickType()) {
                            case LEFT -> icon.leftClickActions();
                            case RIGHT -> icon.rightClickActions();
                            default -> List.<Action>of();
                        }).forEach(action -> action.execute(player)))
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

            pagedGui.addIngredient('+', buildBanMemberIcon(player, claim, metadata));

            pagedGui.addIngredient('-', Markers.CONTENT_LIST_SLOT_HORIZONTAL);

            AtomicReference<PagedGui<Item>> guiReference = new AtomicReference<>(null);

            Map<ClaimMember, Item> parsedMembers = buildMembers(claim, metadata);

            Runnable updateMembers = () -> updateMembers(guiReference.get(), parsedMembers);

            Gui lowerGui = metadata.claimsGui() != null ? metadata.claimsGui() : screenType == GuiTemplate.GuiScreenType.FULL ? buildLowerGui(player, metadata) : null;
            if (metadata.claimsGui() == null) {
                metadata.claimsGui(lowerGui);
            }

            PagedGui<Item> upperGui = pagedGui.build();
            guiReference.set(upperGui);

            updateMembers.run();

            scheduler.schedule(() -> {
                Window.Builder.Normal.Split window = Window.builder()
                        .setTitle(TextUtil.parse(title.text(), Map.of(
                                "claim_name", parseName(claim.name(), title.maxLength())
                        )))
                        .setFallbackWindow(metadata.previousPage())
                        .setUpperGui(upperGui);

                if (lowerGui != null) {
                    window.setLowerGui(lowerGui);
                }

                window.open(player);
            });
        });
    }

    private void updateMembers(@NotNull PagedGui<Item> gui, @NotNull Map<ClaimMember, Item> parsedMembers) {
        List<Item> items = parsedMembers.entrySet().stream()
                .sorted(Comparator
                        .comparingLong(entry -> entry.getKey().joinedTimestamp().getEpochSecond())
                )
                .map(Map.Entry::getValue)
                .toList();

        gui.setContent(items);
    }

    private Map<ClaimMember, Item> buildMembers(@NotNull Claim claim, @NotNull GuiMetadata metadata) {
        Map<ClaimMember, Item> parsedMembers = new HashMap<>();

        Set<ClaimMember> members = claim.bannedMembers();

        for (ClaimMember member : members) {
            Map<String, String> data = MapUtil.add(
                    placeholders.memberInfo(member),
                    "banned_date", StringUtil.formatDate(member.joinedTimestamp())
            );

            ItemStack head = ItemUtil.buildHeadWithName(member.lastKnownName());
            ItemUtil.applyDisplay(head,
                    TextUtil.parseItem(memberIcon.name(), data),
                    TextUtil.parseItemLore(memberIcon.lore(), data)
            );

            parsedMembers.put(member, Item.builder()
                    .setItemProvider(head)
                    .addClickHandler(click -> {
                        claim.removeMember(member.uuid());
                        open(click.player(), metadata);
                    })
                    .build());
        }

        return parsedMembers;
    }

    private Item buildBanMemberIcon(@NotNull Player player, @NotNull Claim claim, @NotNull GuiMetadata metadata) {
        return Item.builder()
                .setItemProvider(banMemberIcon.item())
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

                                    open(player, metadata);
                                })
                                .onSubmit((member) -> {
                                    ClaimMember claimMember = claim.getMember(member.id())
                                            .orElse(claim.addMember(member));

                                    ClaimMember selfMember = claim.getMember(player.getUniqueId())
                                            .orElse(null);

                                    if (claimMember.uuid().equals(player.getUniqueId()) ||
                                            (selfMember != null && !selfMember.canManage(claimMember))) {
                                        text.send(player, messagesHolder.get().claims().cantBanMember());

                                        scheduler.schedule(() -> inputRunnable.get().run());
                                        return;
                                    }

                                    if (!claimMember.banned()) {
                                        claimMember.role(claim.roleRegistry().defaultRole());
                                        claimMember.permissions(new Object2BooleanArrayMap<>());
                                        claimMember.joinedTimestamp(Instant.now());
                                        claimMember.banned(true);

                                        Storage storage = storageHolder.get();
                                        storage.claims()
                                                .saveMembers(claim);

                                        text.send(player, messagesHolder.get().claims().memberBanned(), Map.of(
                                                "name", member.name(),
                                                "player_head", "<head:" + member.name() + ">",
                                                "claim", claim.name()
                                        ));

                                        open(player, metadata);
                                    } else {
                                        text.send(player, messagesHolder.get().claims().memberAlreadyBanned());

                                        scheduler.schedule(() -> inputRunnable.get().run());
                                    }
                                });
                    });

                    inputRunnable.get().run();
                })
                .build();
    }
}