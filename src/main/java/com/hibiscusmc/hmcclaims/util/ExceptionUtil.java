package com.hibiscusmc.hmcclaims.util;

import org.jetbrains.annotations.NotNull;

public final class ExceptionUtil {

    private ExceptionUtil() {
    }

    public static @NotNull String rootMessage(@NotNull Throwable throwable) {
        Throwable root = throwable;

        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }

        String message = root.getMessage();

        return message != null ? message : root.getClass().getSimpleName();
    }
}
