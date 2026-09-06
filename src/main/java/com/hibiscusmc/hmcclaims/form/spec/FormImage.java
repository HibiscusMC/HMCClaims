package com.hibiscusmc.hmcclaims.form.spec;

import org.jetbrains.annotations.NotNull;

/**
 * The icon shown next to a {@link SimpleFormSpec} button.
 *
 * @param url  {@code true} if {@link #data()} is an http(s) URL, {@code false} if it is a
 *             Bedrock resource pack texture path such as {@code textures/items/spyglass}.
 * @param data The URL or texture path.
 */
public record FormImage(boolean url, @NotNull String data) {

    /**
     * Creates an image pointing at a texture inside the Bedrock client's resources.
     *
     * @param path The texture path, e.g. {@code textures/items/arrow}.
     */
    @NotNull
    public static FormImage path(@NotNull String path) {
        return new FormImage(false, path);
    }

    /**
     * Creates an image the Bedrock client downloads from the web.
     *
     * @param url The absolute URL of the image.
     */
    @NotNull
    public static FormImage url(@NotNull String url) {
        return new FormImage(true, url);
    }
}
