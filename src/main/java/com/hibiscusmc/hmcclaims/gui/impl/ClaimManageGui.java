package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.gui.ClaimManageConfig;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.config.gui.MainClaimManageConfig;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.dialog.type.RenameDialog;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.gui.BaseGui;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.input.Input;
import com.hibiscusmc.hmcclaims.input.InputManager;
import com.hibiscusmc.hmcclaims.marker.BlockMarker;
import com.hibiscusmc.hmcclaims.selection.Selection;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.PlaceholderUtil;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.minecraft.server.players.NameAndId;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
@SuppressWarnings({"UnstableApiUsage"})
public class ClaimManageGui implements BaseGui {

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
    protected PlaceholderUtil placeholders;
    @Inject
    protected SchedulerUtil scheduler;
    @Inject
    protected TextUtil text;

    protected String title;
    protected int rows = 1;

    protected GuiTemplate.SimpleIcon backIcon;
    protected GuiTemplate.SimpleIcon deleteIcon;

    protected GuiTemplate.SimpleIcon renameIcon;
    protected GuiTemplate.SimpleIcon lockIcon;
    protected GuiTemplate.SimpleIcon unlockIcon;
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
    }

    @Override
    public void open(@NotNull Player player, Object... args) {
        Claim claim = (Claim) args[0];

        Gui gui = Gui.gui()
                .title(TextUtil.parse(title, Map.of(
                        "claim_name", claim.name()
                )))
                .rows(rows)
                .disableAllInteractions()
                .create();

        scheduler.scheduleAsync(() -> {
            buildMainIcons(player, gui, claim);

            scheduler.schedule(() -> gui.open(player));
        });
    }

    private void buildMainIcons(@NotNull Player player, @NotNull Gui gui, @NotNull Claim claim) {
        buildIcons(player, gui, claim);

        gui.setItem(transferIcon.slot(), new GuiItem(transferIcon.item(), action -> {
            Input<?> currentInput = inputManager.fetch(player);

            if (currentInput != null) {
                return;
            }

            AtomicReference<Runnable> inputRunnable = new AtomicReference<>();
            InventoryView inv = player.getOpenInventory();
            UUID oldOwnerId = claim.owner();

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
                            boolean transferred = claim.transfer(member);

                            if (transferred) {
                                text.send(player, messages.claims().claimTransferred(), Map.of(
                                        "name", member.name(),
                                        "player_head", "<head:" + member.name() + ">",
                                        "claim", claim.name()
                                ));

                                claimManager.transferClaim(claim, oldOwnerId, member.id());

                                guis.get(ClaimMemberListGui.class)
                                        .open(player, claim);
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
        }));
    }

    protected void buildIcons(@NotNull Player player, @NotNull Gui gui, @NotNull Claim claim) {
        for (GuiTemplate.Icon icon : icons) {
            gui.setItem(icon.slot(), new GuiItem(icon.item(), action -> {
                for (Action iconAction : action.isLeftClick() ? icon.leftClickActions() : icon.rightClickActions()) {
                    iconAction.execute(player);
                }
            }));
        }

        gui.setItem(membersTab.slot(), new GuiItem(membersTab.item(), action -> guis.get(ClaimMemberListGui.class)
                .open(player, claim)));
        gui.setItem(rolesTab.slot(), new GuiItem(rolesTab.item(), action -> player.sendRichMessage("<green>viewing roles")));
        gui.setItem(settingsTab.slot(), new GuiItem(settingsTab.item(), action -> guis.get(ClaimSettingsGui.class)
                .open(player, claim)));
        gui.setItem(manageTab.slot(), new GuiItem(manageTab.item()));

        gui.setItem(deleteIcon.slot(), new GuiItem(deleteIcon.item(), action -> player.sendRichMessage("<green>viewing delete")));

        gui.setItem(backIcon.slot(), new GuiItem(backIcon.item(), action ->
                guis.get(ClaimListGui.class).open(player)
        ));

        gui.setItem(renameIcon.slot(), new GuiItem(renameIcon.item(), action ->
                new RenameDialog()
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
        );

        if (claim.locked()) {
            gui.setItem(unlockIcon.slot(), new GuiItem(unlockIcon.item(), action -> {
                claim.locked(false);
                open(player, claim);
            }));
        } else {
            gui.setItem(lockIcon.slot(), new GuiItem(lockIcon.item(), action -> {
                claim.locked(true);
                open(player, claim);
            }));
        }

        gui.setItem(bannedIcon.slot(), new GuiItem(bannedIcon.item(), action -> {
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
        }));

        gui.setItem(resizeIcon.slot(), new GuiItem(resizeIcon.item(), action -> {
            User user = userManager.getUser(player.getUniqueId())
                    .orElseThrow(() -> new IllegalStateException("User not loaded!"));

            Selection selection = new Selection(player, marker, claim.main(), claim.region(), claim);
            user.currentSelection(selection);
            selection.refreshVisuals(true);

            player.closeInventory();
        }));
    }
}