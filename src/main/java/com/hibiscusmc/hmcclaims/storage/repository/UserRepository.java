package com.hibiscusmc.hmcclaims.storage.repository;

import com.hibiscusmc.hmcclaims.user.User;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserRepository {

    CompletableFuture<User> getUser(UUID uuid);

    CompletableFuture<User> getUserByName(String name);

    CompletableFuture<Void> saveUser(User user);

    CompletableFuture<Void> saveUsers(List<User> user);
}