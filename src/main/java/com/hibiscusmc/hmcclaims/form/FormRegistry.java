package com.hibiscusmc.hmcclaims.form;

import com.hibiscusmc.hmcclaims.form.impl.ClaimBannedListForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimDeleteForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimListForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimManageForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimMemberListForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimMemberPermissionsForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimMemberRoleForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimRoleManageForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimRolesForm;
import com.hibiscusmc.hmcclaims.form.impl.ClaimSettingsForm;
import com.hibiscusmc.hmcclaims.form.impl.SubClaimManageForm;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Injector;
import team.unnamed.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

/**
 * A centralized registry for managing the lifecycle and retrieval of Bedrock form
 * instances.
 *
 * @see com.hibiscusmc.hmcclaims.gui.GuiRegistry
 */
@Singleton
public class FormRegistry {

    /**
     * Internal mapping of form classes to their singleton instances.
     */
    private final Map<Class<? extends BaseForm>, BaseForm> forms
            = new HashMap<>();

    private final Injector injector;

    /**
     * Initializes the registry and performs the initial form registration.
     *
     * @param injector The injector used to instantiate form classes.
     */
    @Inject
    public FormRegistry(Injector injector) {
        this.injector = injector;

        setup();
    }

    /**
     * Defines which form classes are active and registered within the plugin.
     */
    public void setup() {
        multiRegister(
                ClaimListForm.class,
                ClaimMemberListForm.class,
                ClaimSettingsForm.class,
                ClaimRolesForm.class,
                ClaimManageForm.class,
                SubClaimManageForm.class,
                ClaimRoleManageForm.class,
                ClaimMemberRoleForm.class,
                ClaimMemberPermissionsForm.class,
                ClaimBannedListForm.class,
                ClaimDeleteForm.class
        );
    }

    /**
     * Triggers the {@code loadConfig()} method for every registered form.
     */
    public void load() {
        for (BaseForm form : forms.values()) {
            form.loadConfig();
        }
    }

    /**
     * Completely resets the registry, re-instantiates all forms, and reloads their
     * configurations.
     */
    public void reload() {
        forms.clear();

        setup();
        load();
    }

    /**
     * Registers multiple form classes in a single call.
     *
     * @param classes The form classes to instantiate and register.
     */
    @SafeVarargs
    public final void multiRegister(Class<? extends BaseForm>... classes) {
        for (Class<? extends BaseForm> clazz : classes) {
            register(clazz);
        }
    }

    /**
     * Instantiates a form class via the injector and stores it in the registry.
     *
     * @param clazz The class to register.
     * @param <T>   A type extending {@link BaseForm}.
     * @throws RuntimeException If the injector fails to instantiate the form.
     */
    public <T extends BaseForm> void register(Class<T> clazz) {
        try {
            BaseForm form = injector.getInstance(clazz);

            forms.put(clazz, form);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Retrieves the singleton instance of a registered form.
     *
     * @param clazz The class of the form to retrieve.
     * @param <T>   The specific form type.
     * @return The registered instance, or {@code null} if not found.
     */
    public <T extends BaseForm> T get(Class<T> clazz) {
        // noinspection unchecked
        return (T) forms.get(clazz);
    }
}