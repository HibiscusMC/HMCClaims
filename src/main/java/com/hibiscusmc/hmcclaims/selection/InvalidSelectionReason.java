package com.hibiscusmc.hmcclaims.selection;

/**
 * Represents the various reasons why a player's current selection
 * cannot be converted into a valid claim.
 */
public enum InvalidSelectionReason {

    /**
     * The selection boundaries intersect with an existing claim
     * or a protected administrative region.
     */
    OVERLAPPING,

    /**
     * The selection area does not meet the minimum size requirements
     * defined in the plugin configuration.
     */
    TOO_SMALL,

    /**
     * The selection is valid, or no selection has been made yet.
     */
    NONE

}