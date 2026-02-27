package com.hibiscusmc.hmcclaims.permission;

/**
 * An immutable record that pairs a permission node with its toggled state.
 *
 * @param permission The specific action or right (e.g., BREAK, PLACE, INTERACT).
 * @param status     The state of the permission: {@code true} for granted,
 *                   {@code false} for explicitly revoked.
 */
public record PermissionHolder(Permission permission, boolean status) {
}