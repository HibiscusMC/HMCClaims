package com.hibiscusmc.hmcclaims.gui;

import com.hibiscusmc.hmcclaims.claim.Claim;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import xyz.xenondevs.invui.gui.Gui;

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
     * Current active settings page index.
     */
    private int settingsPage = 1;

    /**
     * The previous GUI instance for backwards navigation.
     */
    @MonotonicNonNull
    private BaseGui previousPage;

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