package com.hibiscusmc.hmcclaims.command.argument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link com.hibiscusmc.hmcclaims.claim.ClaimMember} command argument, matched by
 * name against the members of the claim the sender is standing in.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Member {
}
