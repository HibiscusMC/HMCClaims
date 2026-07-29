package com.hibiscusmc.hmcclaims.gui;

import com.hibiscusmc.hmcclaims.gui.impl.ClaimBannedListGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimDeleteGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimListGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimManageGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimMemberListGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimRoleManageGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimRolesGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimSettingsGui;
import com.hibiscusmc.hmcclaims.gui.impl.SubClaimManageGui;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Injector;
import team.unnamed.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

/**
 * A centralized registry for managing the lifecycle and retrieval of GUI instances.
 */
@Singleton
public class GuiRegistry {

    /**
     * Internal mapping of GUI classes to their singleton instances.
     */
    private final Map<Class<? extends BaseGui>, BaseGui> guis
            = new HashMap<>();

    private final Injector injector;

    /**
     * Initializes the registry and performs the initial GUI registration.
     *
     * @param injector The injector used to instantiate GUI classes.
     */
    @Inject
    public GuiRegistry(Injector injector) {
        this.injector = injector;

        setup();
    }

    /**
     * Defines which GUI classes are active and registered within the plugin.
     */
    public void setup() {
        multiRegister(
                ClaimListGui.class,
                ClaimMemberListGui.class,
                ClaimSettingsGui.class,
                ClaimRolesGui.class,
                ClaimManageGui.class,
                SubClaimManageGui.class,
                ClaimRoleManageGui.class,
                ClaimBannedListGui.class,
                ClaimDeleteGui.class
        );
    }

    /**
     * Triggers the {@code loadConfig()} method for every registered GUI.
     */
    public void load() {
        for (BaseGui gui : guis.values()) {
            gui.loadConfig();
        }
    }

    /**
     * Completely resets the registry, re-instantiates all GUIs, and reloads
     * their configurations.
     */
    public void reload() {
        guis.clear();

        setup();
        load();
    }

    /**
     * Registers multiple GUI classes in a single call.
     *
     * @param classes The GUI classes to instantiate and register.
     */
    @SafeVarargs
    public final void multiRegister(Class<? extends BaseGui>... classes) {
        for (Class<? extends BaseGui> clazz : classes) {
            register(clazz);
        }
    }

    /**
     * Instantiates a GUI class via the injector and stores it in the registry.
     *
     * @param clazz The class to register.
     * @param <T>   A type extending {@link BaseGui}.
     * @throws RuntimeException If the injector fails to instantiate the GUI.
     */
    public <T extends BaseGui> void register(Class<T> clazz) {
        try {
            BaseGui gui = injector.getInstance(clazz);

            guis.put(clazz, gui);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Retrieves the singleton instance of a registered GUI.
     *
     * @param clazz The class of the GUI to retrieve.
     * @param <T>   The specific GUI type.
     * @return The registered instance, or {@code null} if not found.
     */
    public <T extends BaseGui> T get(Class<T> clazz) {
        // noinspection unchecked
        return (T) guis.get(clazz);
    }
}