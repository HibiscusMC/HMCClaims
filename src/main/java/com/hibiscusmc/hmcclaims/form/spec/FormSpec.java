package com.hibiscusmc.hmcclaims.form.spec;

/**
 * A renderer-independent description of a Bedrock form.
 * <p>
 * Nothing in this package references Geyser, Floodgate or Cumulus; the
 * {@link com.hibiscusmc.hmcclaims.form.FloodgateBridge} is the only class that
 * translates these descriptions into actual Bedrock forms. That keeps the plugin
 * loadable on servers where Floodgate isn't installed.
 */
public sealed interface FormSpec permits SimpleFormSpec, ModalFormSpec, CustomFormSpec {

    /**
     * @return The title displayed at the top of the form.
     */
    String title();

    /**
     * Callback fired when the player dismisses the form without picking anything.
     *
     * @return The callback, or {@code null} if closing should do nothing.
     */
    Runnable onClose();
}
