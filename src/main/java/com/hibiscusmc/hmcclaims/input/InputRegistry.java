package com.hibiscusmc.hmcclaims.input;

import com.hibiscusmc.hmcclaims.input.type.PlayerInput;
import com.hibiscusmc.hmcclaims.input.type.StringInput;
import com.hibiscusmc.hmcclaims.user.PlayerResolver;
import net.minecraft.server.players.NameAndId;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Creates {@link Input} instances.
 * <p>
 * Maps data types (represented by {@code Class} objects) to factories, so every
 * prompt gets its own fresh input with its own callbacks.
 */
public class InputRegistry {

    private final Map<Class<?>, Supplier<? extends Input<?>>> factories
            = new HashMap<>();

    protected InputRegistry(PlayerResolver resolver) {
        register(String.class, StringInput::new);
        register(NameAndId.class, () -> new PlayerInput(resolver));
    }

    private <T> void register(Class<T> type, Supplier<? extends Input<T>> factory) {
        factories.put(type, factory);
    }

    /**
     * Creates a new input for the type, or null if none is registered.
     */
    @SuppressWarnings("unchecked")
    protected <T> @Nullable Input<T> create(Class<T> type) {
        Supplier<? extends Input<?>> factory = factories.get(type);

        if (factory == null) {
            return null;
        }

        return (Input<T>) factory.get();
    }
}
