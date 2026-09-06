package com.hibiscusmc.hmcclaims.menu;

import com.hibiscusmc.hmcclaims.form.BaseForm;
import com.hibiscusmc.hmcclaims.form.FormRegistry;
import com.hibiscusmc.hmcclaims.form.FormService;
import com.hibiscusmc.hmcclaims.form.impl.ClaimBannedListForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimDeleteForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimListForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimManageForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimMemberListForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimMemberPermissionsForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimMemberRoleForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimRoleManageForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimRolesForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimSettingsForm;
import com.hibiscusmc.hmcclaims.form.impl.SubClaimManageForm;
import com.hibiscusmc.hmcclaims.gui.BaseGui;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimBannedListGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimDeleteGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimListGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimManageGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimMemberListGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimMemberPermissionsGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimMemberRoleGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimRoleManageGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimRolesGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimSettingsGui;
import com.hibiscusmc.hmcclaims.gui.impl.SubClaimManageGui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.Map;

/**
 * Decides whether a player gets the inventory GUI or the Bedrock form.
 */
@Singleton
public class MenuService {

    /**
     * Which form stands in for which GUI.
     */
    private final static Map<Class<? extends BaseGui>, Class<? extends BaseForm>> EQUIVALENTS = Map.ofEntries(
            Map.entry(ClaimListGui.class, ClaimListForm.class),
            Map.entry(ClaimMemberListGui.class, ClaimMemberListForm.class),
            Map.entry(ClaimSettingsGui.class, ClaimSettingsForm.class),
            Map.entry(ClaimRolesGui.class, ClaimRolesForm.class),
            Map.entry(ClaimManageGui.class, ClaimManageForm.class),
            Map.entry(SubClaimManageGui.class, SubClaimManageForm.class),
            Map.entry(ClaimRoleManageGui.class, ClaimRoleManageForm.class),
            Map.entry(ClaimMemberRoleGui.class, ClaimMemberRoleForm.class),
            Map.entry(ClaimMemberPermissionsGui.class, ClaimMemberPermissionsForm.class),
            Map.entry(ClaimBannedListGui.class, ClaimBannedListForm.class),
            Map.entry(ClaimDeleteGui.class, ClaimDeleteForm.class)
    );

    @Inject
    private GuiRegistry guis;

    @Inject
    private FormRegistry forms;

    @Inject
    private FormService formService;

    /**
     * Opens a menu without any claim context.
     *
     * @param gui    The inventory GUI to open, or its Bedrock equivalent.
     * @param player The viewer.
     */
    public void open(@NotNull Class<? extends BaseGui> gui, @NotNull Player player) {
        open(gui, player, null);
    }

    /**
     * Opens a menu.
     *
     * @param gui      The inventory GUI to open, or its Bedrock equivalent.
     * @param player   The viewer.
     * @param metadata The claim context, or {@code null} when there is none.
     */
    public void open(@NotNull Class<? extends BaseGui> gui, @NotNull Player player, @Nullable GuiMetadata metadata) {
        if (sendForm(gui, player, metadata)) {
            return;
        }

        openGui(gui, player, metadata);
    }

    /**
     * Attempts the Bedrock path.
     *
     * @return {@code true} if the player was handed a form.
     */
    private boolean sendForm(@NotNull Class<? extends BaseGui> gui, @NotNull Player player, @Nullable GuiMetadata metadata) {
        if (!formService.isBedrock(player)) {
            return false;
        }

        Class<? extends BaseForm> equivalent = EQUIVALENTS.get(gui);
        if (equivalent == null) {
            return false;
        }

        BaseForm form = forms.get(equivalent);
        if (form == null) {
            return false;
        }

        // Commands are entry points, so anything the player had open before is stale.
        formService.clear(player);

        if (metadata == null) {
            form.send(player);
        } else {
            form.send(player, metadata);
        }

        return true;
    }

    /**
     * Falls back to the inventory GUI. Bedrock players end up here when forms are turned
     * off in the config or when Floodgate isn't installed, in which case Geyser translates
     * the inventory for them.
     */
    private void openGui(@NotNull Class<? extends BaseGui> gui, @NotNull Player player, @Nullable GuiMetadata metadata) {
        BaseGui instance = guis.get(gui);
        if (instance == null) {
            return;
        }

        if (metadata == null) {
            instance.open(player);
        } else {
            instance.open(player, metadata);
        }
    }
}