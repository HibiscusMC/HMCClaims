package com.hibiscusmc.hmcclaims.input;

import com.hibiscusmc.hmcclaims.input.type.PlayerInput;
import com.hibiscusmc.hmcclaims.input.type.StringInput;
import net.minecraft.server.players.NameAndId;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the lifecycle and retrieval of {@link Input} instances.
 * <p>
 * This registry acts as a central repository to map specific data types
 * (represented by {@code Class} objects) to their corresponding input handlers.
 */
public class InputRegistry {

    private final static Map<Class<?>, Input<?>> INPUT_MAP
            = new HashMap<>();

    protected InputRegistry() {
        register(String.class, new StringInput());
        register(NameAndId.class, new PlayerInput());
    }

    /**
     * Associates a specific class type with an input handler.
     *
     * @param <T>   The type of data handled by the input.
     * @param clazz The class object representing the data type.
     * @param input The input implementation to register.
     */
    private <T> void register(Class<T> clazz, Input<T> input) {
        INPUT_MAP.put(clazz, input);
    }

    /**
     * Retrieves the registered input handler for a given class type.
     *
     * @param <T>   The type of data expected.
     * @param clazz The class object to look up.
     * @return The {@link Input} instance associated with the type,
     * or {@code null} if no handler is registered.
     */
    @SuppressWarnings("unchecked")
    protected <T> Input<T> get(Class<T> clazz) {
        return (Input<T>) INPUT_MAP.get(clazz);
    }
}