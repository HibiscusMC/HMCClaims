package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.gui.ClaimManageConfig;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.config.gui.MainClaimManageConfig;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.dialog.type.SingleInputDialog;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.gui.BaseGui;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.input.Input;
import com.hibiscusmc.hmcclaims.input.InputManager;
import com.hibiscusmc.hmcclaims.marker.BlockMarker;
import com.hibiscusmc.hmcclaims.selection.Selection;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import net.minecraft.server.players.NameAndId;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.util.TriConsumer;
import xyz.xenondevs.invui.window.Window;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Singleton
@SuppressWarnings({"UnstableApiUsage"})
public class ClaimManageGui extends ClaimListGui {

    @Inject
    private ConfigHolder<MainClaimManageConfig> configHolder;
    @Inject
    protected ConfigHolder<Messages> messagesHolder;

    @Inject
    private StorageHolder storageHolder;

    @Inject
    protected GuiRegistry guis;

    @Inject
    protected BlockMarker marker;

    @Inject
    protected ClaimManager claimManager;
    @Inject
    protected InputManager inputManager;
    @Inject
    protected UserManager userManager;

    @Inject
    protected SchedulerUtil scheduler;
    @Inject
    protected TextUtil text;

    protected GuiTemplate.GuiTitle title;
    protected int rows = 1;

    protected GuiTemplate.GuiScreenType screenType;

    protected GuiTemplate.SimpleIcon deleteIcon;

    protected GuiTemplate.SimpleIcon renameIcon;
    protected GuiTemplate.SimpleIcon lockIcon;
    protected ItemStack unlockIcon;
    protected GuiTemplate.SimpleIcon bannedIcon;
    protected GuiTemplate.SimpleIcon resizeIcon;

    protected GuiTemplate.SimpleIcon backIcon;

    private GuiTemplate.SimpleIcon transferIcon;

    private Map<String, GuiTemplate.SimpleIcon> tabs;

    protected List<GuiTemplate.Icon> icons;

    @Override
    public void loadConfig() {
        MainClaimManageConfig config = configHolder.get();
        if (config == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        loadConfig(config);

        transferIcon = config.transferIcon();
    }

    protected <T extends ClaimManageConfig> void loadConfig(T config) {
        title = config.title();
        rows = config.rows();

        deleteIcon = config.deleteIcon();

        tabs = config.tabs();

        renameIcon = config.renameIcon();
        lockIcon = config.lockIcon();
        unlockIcon = config.unlockIcon();
        bannedIcon = config.bannedIcon();
        resizeIcon = config.resizeIcon();

        backIcon = config.backIcon();

        icons = config.extraIcons().values().stream().toList();

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
            InventoryStructure invStructure = build(player, claim, metadata);
            CharList structure = invStructure.structure();
            structure.set(transferIcon.slot(), '!');

            String[] structureArray = new String[rows];
            for (int r = 0; r < rows; r++) {
                CharList rowList = structure.subList(r * 9, (r + 1) * 9);

                structureArray[r] = new String(rowList.toCharArray());
            }

            gui.setStructure(structureArray);

            gui.addIngredient('!', buildTransferIcon(player, claim, metadata));
            invStructure.builder().accept(gui);

            Gui lowerGui = metadata.claimsGui() != null ? metadata.claimsGui() : screenType == GuiTemplate.GuiScreenType.FULL ? buildLowerGui(player, metadata) : null;
            if (metadata.claimsGui() == null) {
                metadata.claimsGui(lowerGui);
            }

            Gui upperGui = gui.build();

            scheduler.schedule(() -> {
                Window.Builder.Normal.Split windowBuilder = Window.builder()
                        .setTitle(TextUtil.parse(title.text(), Map.of(
                                "claim_name", parseName(claim.name(), title.maxLength())
                        )))
                        .setUpperGui(upperGui)
                        .addCloseHandler(reason -> {
                            Storage storage = storageHolder.get();

                            storage.claims().saveClaimMeta(claim);
                        });

                if (lowerGui != null) {
                    windowBuilder.setLowerGui(lowerGui);
                }

                Window window = windowBuilder.build(player);
                metadata.previousPage(window);

                window.open();
            });
        });
    }

    protected InventoryStructure build(@NotNull Player player, @NotNull Claim claim, @NotNull GuiMetadata metadata) {
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

        structure.set(renameIcon.slot(), '(');
        structure.set(bannedIcon.slot(), '%');
        structure.set(lockIcon.slot(), ')');
        structure.set(resizeIcon.slot(), '&');
        structure.set(deleteIcon.slot(), '*');

        if (isValidIcon(backIcon)) {
            structure.set(backIcon.slot(), '$');
        }

        Class<? extends BaseGui> currentClass = getClass();
        TriConsumer<Gui.Builder<?, ?>, GuiRegistry, Player> tabsBuilder = buildTabs(
                structure, currentClass, claim, metadata,
                tabs
        );

        return new InventoryStructure(structure, (gui) -> {
            for (Map.Entry<Integer, GuiTemplate.Icon> entry : mappedIcons.entrySet()) {
                GuiTemplate.Icon icon = entry.getValue();

                gui.addIngredient((char) entry.getKey().intValue(), Item.builder()
                        .setItemProvider(icon.item())
                        .addClickHandler(click -> (switch (click.clickType()) {
                            case LEFT -> icon.leftClickActions();
                            case RIGHT -> icon.rightClickActions();
                            default -> List.<Action>of();
                        }).forEach(action -> action.execute(player)))
                        .build());
            }

            tabsBuilder.accept(gui, guis, player);

            gui.addIngredient('(', buildRenameIcon(player, claim, metadata));
            gui.addIngredient(')', buildLockUnlockIcon(claim));
            gui.addIngredient('%', Item.builder()
                    .setItemProvider(bannedIcon.item())
                    .addClickHandler(click ->
                            guis.get(ClaimBannedListGui.class)
                                    .open(player, metadata)
                    )
                    .build());
            gui.addIngredient('&', buildResizeIcon(player, claim));
            gui.addIngredient('*', Item.builder()
                    .setItemProvider(deleteIcon.item())
                    .addClickHandler(click ->
                            guis.get(ClaimDeleteGui.class)
                                    .open(player, metadata)
                    )
                    .build());

            if (isValidIcon(backIcon)) {
                gui.addIngredient('$', Item.builder()
                        .setItemProvider(backIcon.item())
                        .addClickHandler(click -> guis.get(ClaimListGui.class).open(player))
                        .build()
                );
            }
        });
    }

    private Item buildTransferIcon(@NotNull Player player, @NotNull Claim claim, @NotNull GuiMetadata metadata) {
        return Item.builder()
                .setItemProvider(transferIcon.item())
                .addClickHandler(click -> {
                    Input<?> currentInput = inputManager.fetch(player);

                    if (currentInput != null) {
                        return;
                    }

                    AtomicReference<Runnable> inputRunnable = new AtomicReference<>();
                    UUID oldOwnerId = claim.owner();

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
                                    Messages messages = messagesHolder.get();
                                    boolean transferred = claim.transfer(member);

                                    if (transferred) {
                                        text.send(player, messages.claims().claimTransferred(), Map.of(
                                                "name", member.name(),
                                                "player_head", "<head:" + member.name() + ">",
                                                "claim", claim.name()
                                        ));

                                        claimManager.transferClaim(claim, oldOwnerId, member.id());
                                    } else {
                                        text.send(player, member.id().equals(player.getUniqueId()) ?
                                                messages.claims().selfAlreadyOwner() :
                                                messages.claims().memberAlreadyOwner()
                                        );

                                        scheduler.schedule(() -> inputRunnable.get().run());
                                    }
                                });
                    });

                    inputRunnable.get().run();
                })
                .build();
    }

    private Item buildRenameIcon(@NotNull Player player, @NotNull Claim claim, @NotNull GuiMetadata metadata) {
        return Item.builder()
                .setItemProvider(renameIcon.item())
                .addClickHandler(click -> new SingleInputDialog()
                        .create(
                                messagesHolder.get().dialogs().renameClaim(),
                                Map.of("claim_name", claim.name()), Map.of(),
                                claim.name(), 40
                        )
                        .onSubmit(view -> {
                            String newName = view.getText("input");
                            if (newName == null) {
                                return;
                            }

                            claim.rename(newName);

                            open(player, metadata);
                        })
                        .show(player))
                .build();
    }

    private Item buildLockUnlockIcon(@NotNull Claim claim) {
        return Item.builder()
                .setItemProvider(p -> new ItemWrapper(claim.locked() ? unlockIcon : lockIcon.item()))
                .addClickHandler((it, click) -> {
                    claim.locked(!claim.locked());
                    it.notifyWindows();
                })
                .build();
    }

    private Item buildResizeIcon(@NotNull Player player, @NotNull Claim claim) {
        return Item.builder()
                .setItemProvider(resizeIcon.item())
                .addClickHandler(click -> {
                    User user = userManager.getUser(player.getUniqueId())
                            .orElseThrow(() -> new IllegalStateException("User not loaded!"));

                    Selection selection = new Selection(player, marker, claim.main(), claim.region(), claim);
                    user.currentSelection(selection);
                    selection.refreshVisuals(true);

                    player.closeInventory();
                })
                .build();
    }

    protected record InventoryStructure(CharList structure, Consumer<Gui.Builder<?, ?>> builder) {
    }
}