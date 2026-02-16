package com.hibiscusmc.hmcclaims.util;

import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;

public class RegistryUtil {

    public static Key withKey(@Subst("registry_id") String namespace) {
        return Key.key("hmcclaims", namespace);
    }

}