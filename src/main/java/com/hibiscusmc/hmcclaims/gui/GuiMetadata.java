package com.hibiscusmc.hmcclaims.gui;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.window.Window;

/**
 * Holds contextual metadata and navigation history for GUI rendering.
 */
@Data
public class GuiMetadata {

    /**
     * The target claim associated with this GUI context.
     */
    private Claim claim;

    /**
     * The target role associated with this GUI context.
     */
    private ClaimRole role;

    /**
     * The target member associated with this GUI context.
     */
    private ClaimMember member;

    /**
     * If the opener of the GUI is able to manage this role
     */
    private boolean canManageRole = false;

    /**
     * If the opener of the GUI is able to manage this role's permissions
     */
    private boolean canManageRolePermissions = false;

    /**
     * Current active page index.
     */
    private int currentPage = 1;

    /**
     * The previous GUI instance for backwards navigation.
     */
    @MonotonicNonNull
    private Window previousPage;

    /**
     * The parent claims overview GUI instance.
     */
    @MonotonicNonNull
    private Gui claimsGui;

    /**
     * Constructs a metadata container for the specified claim.
     *
     * @param claim The claim context for the GUI.
     */
    public GuiMetadata(final Claim claim) {
        this.claim = claim;
    }
}