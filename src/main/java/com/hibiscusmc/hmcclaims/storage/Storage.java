package com.hibiscusmc.hmcclaims.storage;

import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.storage.repository.ClaimRepository;
import com.hibiscusmc.hmcclaims.storage.repository.UserRepository;

public interface Storage {
    String name();

    void initialize(Settings.Storage storage);

    void close();

    UserRepository users();

    ClaimRepository claims();
}