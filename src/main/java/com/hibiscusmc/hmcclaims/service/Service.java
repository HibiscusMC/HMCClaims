package com.hibiscusmc.hmcclaims.service;

/**
 * Defines the standard lifecycle operations for a plugin service or manager.
 */
public interface Service {

    /**
     * Initializes the service and prepares it for operation.
     */
    void start();

    /**
     * Reloads the service's configuration and internal state.
     */
    void reload();

    /**
     * Gracefully shuts down the service.
     */
    void stop();
}