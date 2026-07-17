package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.gui.ClaimManageConfig;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.config.gui.MainClaimManageConfig;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.dialog.type.RenameDialog;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.input.Input;
import com.hibiscusmc.hmcclaims.input.InputManager;
import com.hibiscusmc.hmcclaims.marker.BlockMarker;
import com.hibiscusmc.hmcclaims.selection.Selection;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import net.minecraft.server.players.NameAndId;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Singleton
@SuppressWarnings({"UnstableApiUsage"})
public class ClaimManageGui extends ClaimListLowerGui {

    @Inject
    private ConfigHolder<MainClaimManageConfig> configHolder;
    @Inject
    protected ConfigHolder<Messages> messagesHolder;

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

    protected String title;
    protected int rows = 1;

    protected GuiTemplate.GuiScreenType screenType;

    protected GuiTemplate.SimpleIcon backIcon;
    protected GuiTemplate.SimpleIcon deleteIcon;

    protected GuiTemplate.SimpleIcon renameIcon;
    protected GuiTemplate.SimpleIcon lockIcon;
    protected ItemStack unlockIcon;
    protected GuiTemplate.SimpleIcon bannedIcon;
    protected GuiTemplate.SimpleIcon resizeIcon;

    private GuiTemplate.SimpleIcon transferIcon;

    protected GuiTemplate.SimpleIcon membersTab;
    protected GuiTemplate.SimpleIcon rolesTab;
    protected GuiTemplate.SimpleIcon settingsTab;
    protected GuiTemplate.SimpleIcon manageTab;

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

        backIcon = config.backIcon();
        deleteIcon = config.deleteIcon();

        membersTab = config.tabs().get("members-tab");
        rolesTab = config.tabs().get("roles-tab");
        settingsTab = config.tabs().get("settings-tab");
        manageTab = config.tabs().get("manage-tab");

        renameIcon = config.renameIcon();
        lockIcon = config.lockIcon();
        unlockIcon = config.unlockIcon();
        bannedIcon = config.bannedIcon();
        resizeIcon = config.resizeIcon();

        icons = config.extraIcons().values().stream().toList();

        screenType = config.screenType();
        if (screenType == GuiTemplate.GuiScreenType.FULL) {
            super.loadConfig(config.lowerGui());
        }
    }

    @Override
    public void open(@NotNull Player player, Object... args) {
        Claim claim = (Claim) args[0];

        scheduler.scheduleAsync(() -> {
            Gui.Builder<?, ?> gui = Gui.builder();
            InventoryStructure invStructure = build(player, claim);
            List<String> structure = invStructure.structure();
            structure.set(transferIcon.slot(), "!");

            String[] structureArray = new String[rows];
            for (int r = 0; r < rows; r++) {
                List<String> rowList = structure.subList(r * 9, (r + 1) * 9);

                structureArray[r] = String.join("", rowList);
            }

            gui.setStructure(structureArray);

            gui.addIngredient('!', buildTransferIcon(player, claim));
            invStructure.builder().accept(gui);

            Gui lowerGui = screenType == GuiTemplate.GuiScreenType.FULL ? buildLowerGui(player) : null;
            Gui upperGui = gui.build();

            scheduler.schedule(() -> {
                Window.Builder.Normal.Split window = Window.builder()
                        .setTitle(TextUtil.parse(title, Map.of(
                                "claim_name", claim.name()
                        )))
                        .setUpperGui(upperGui);

                if (lowerGui != null) {
                    window.setLowerGui(lowerGui);
                }

                window.open(player);
            });
        });
    }

    protected InventoryStructure build(@NotNull Player player, @NotNull Claim claim) {
        List<String> structure = new ArrayList<>(Collections.nCopies(rows * 9, "#"));

        structure.set(membersTab.slot(), Character.toString(1));
        structure.set(rolesTab.slot(), Character.toString(2));
        structure.set(settingsTab.slot(), Character.toString(3));
        structure.set(manageTab.slot(), Character.toString(4));

        structure.set(renameIcon.slot(), "(");
        structure.set(bannedIcon.slot(), "%");
        structure.set(lockIcon.slot(), ")");
        structure.set(resizeIcon.slot(), "&");

        Map<Integer, GuiTemplate.Icon> mappedIcons = new HashMap<>();
        for (GuiTemplate.Icon icon : icons) {
            int codePoint = FIRST_SAFE_CHAR + icons.indexOf(icon);

            structure.set(icon.slot(), Character.toString(codePoint));
            mappedIcons.put(codePoint, icon);
        }

        return new InventoryStructure(structure, (gui) -> {
            for (Map.Entry<Integer, GuiTemplate.Icon> entry : mappedIcons.entrySet()) {
                GuiTemplate.Icon icon = entry.getValue();

                gui.addIngredient((char) entry.getKey().intValue(), Item.builder()
                        .setItemProvider(icon.item())
                        .addClickHandler(click -> {
                            for (Action iconAction : click.clickType().isLeftClick() ?
                                    icon.leftClickActions() :
                                    icon.rightClickActions()) {
                                iconAction.execute(player);
                            }
                        })
                        .build());
            }

            gui.addIngredient((char) 1, Item.builder()
                    .setItemProvider(membersTab.item())
                    .addClickHandler(click -> guis.get(ClaimMemberListGui.class).open(player, claim))
                    .build());

            gui.addIngredient((char) 2, Item.builder()
                    .setItemProvider(rolesTab.item())
                    .addClickHandler(click -> {
                    })
                    .build());

            gui.addIngredient((char) 3, Item.builder()
                    .setItemProvider(settingsTab.item())
                    .addClickHandler(click -> guis.get(ClaimSettingsGui.class).open(player, claim))
                    .build());

            gui.addIngredient((char) 4, Item.builder()
                    .setItemProvider(manageTab.item())
                    .build());

            gui.addIngredient('(', buildRenameIcon(player, claim));
            gui.addIngredient(')', buildLockUnlockIcon(claim));
            gui.addIngredient('%', Item.builder().setItemProvider(bannedIcon.item()).build());
            gui.addIngredient('&', buildResizeIcon(player, claim));
        });
    }

    private Item buildTransferIcon(@NotNull Player player, @NotNull Claim claim) {
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

                                    open(player, claim);
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

    private Item buildRenameIcon(@NotNull Player player, @NotNull Claim claim) {
        return Item.builder()
                .setItemProvider(renameIcon.item())
                .addClickHandler(click -> new RenameDialog()
                        .create(messagesHolder.get().dialogs(), claim.name())
                        .onSubmit(view -> {
                            String newName = view.getText("input");
                            if (newName == null) {
                                return;
                            }

                            claim.rename(newName);

                            open(player, claim);
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

    protected Item buildBannedIcon(@NotNull Player player, @NotNull Gui gui, @NotNull Claim claim) {
        // TODO: Move this to banned members GUI
        /*gui.setItem(bannedIcon.slot(), new GuiItem(bannedIcon.item(), action -> {
            Input<?> currentInput = inputManager.fetch(player);

            if (currentInput != null) {
                return;
            }

            AtomicReference<Runnable> inputRunnable = new AtomicReference<>();
            InventoryView inv = player.getOpenInventory();


            inputRunnable.set(() -> {
                Input<NameAndId> input = inputManager.create(player, NameAndId.class);
                if (input == null) {
                    return;
                }

                player.closeInventory();
                input.onCancel(() -> {
                            text.send(player, messagesHolder.get().inputs().cancelled());

                            player.openInventory(inv);
                        })
                        .onSubmit((member) -> {
                            Messages messages = messagesHolder.get();
                            ClaimMember claimMember = claim.getMember(member.id())
                                    .orElse(null);

                            if (claimMember != null && !claimMember.uuid().equals(claim.owner()) && !claimMember.banned()) {
                                text.send(player, messages.claims().memberBanned(), Map.of(
                                        "name", member.name(),
                                        "player_head", "<head:" + member.name() + ">",
                                        "claim", claim.name()
                                ));

                                claimMember.banned(true);

                                guis.get(ClaimMemberListGui.class)
                                        .open(player, claim);
                            } else {
                                text.send(player, claimMember == null ?
                                        messages.claims().playerNotMember() :
                                        claimMember.banned() ?
                                                messages.claims().memberAlreadyBanned() :
                                                messages.claims().cantBanMember()
                                );


                                scheduler.schedule(() -> inputRunnable.get().run());
                            }
                        });
            });

            inputRunnable.get().run();
        }));*/
        return null;
    }

    protected record InventoryStructure(List<String> structure, Consumer<Gui.Builder<?, ?>> builder) {
    }
}