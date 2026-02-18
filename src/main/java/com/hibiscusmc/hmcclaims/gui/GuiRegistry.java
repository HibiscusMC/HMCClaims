package com.hibiscusmc.hmcclaims.gui;

import com.hibiscusmc.hmcclaims.gui.impl.ClaimListGui;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Injector;
import team.unnamed.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class GuiRegistry {

    private final static Map<Class<? extends BaseGui>, BaseGui> GUIS
            = new HashMap<>();

    private final Injector injector;

    @Inject
    public GuiRegistry(Injector injector) {
        this.injector = injector;

        load();
    }

    public void load() {
        registerGui(ClaimListGui.class);
    }

    public void reload() {
        GUIS.clear();

        load();
    }

    public <T extends BaseGui> void registerGui(Class<T> clazz) {
        try {
            BaseGui gui = injector.getInstance(clazz);
            gui.loadConfig();

            GUIS.put(clazz, gui);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T extends BaseGui> T get(Class<T> clazz) {
        // noinspection unchecked
        return (T) GUIS.get(clazz);
    }
}