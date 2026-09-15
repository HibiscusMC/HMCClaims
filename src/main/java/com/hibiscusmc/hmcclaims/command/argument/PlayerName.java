package com.hibiscusmc.hmcclaims.command.argument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link String} command argument as a player name.
 * <p>
 * The argument is handed over as typed and tab-completes online players; the command
 * resolves it through {@link com.hibiscusmc.hmcclaims.user.PlayerResolver}, since offline
 * players may need a storage lookup that can't happen while parsing.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PlayerName {
}
