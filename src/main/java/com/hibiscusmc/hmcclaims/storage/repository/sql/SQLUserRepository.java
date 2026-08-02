package com.hibiscusmc.hmcclaims.storage.repository.sql;

import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.storage.impl.remote.HikariStorage;
import com.hibiscusmc.hmcclaims.storage.repository.UserRepository;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.util.ByteUtil;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * A SQL-based implementation of the {@link UserRepository} using JDBC.
 */
public class SQLUserRepository implements UserRepository {

    private final HikariStorage storage;
    private final ExecutorService executor;

    private final String getUserQuery;
    private final String getUserByNameQuery;
    private final String saveUserQuery;

    /**
     * Constructs the repository and prepares the SQL query strings.
     *
     * @param settings The storage settings used to retrieve the table prefix.
     * @param storage  The HikariStorage provider for database connections.
     * @param executor The executor service for off-thread processing.
     */
    public SQLUserRepository(Settings.Storage settings, HikariStorage storage, ExecutorService executor) {
        this.storage = storage;
        this.executor = executor;

        String prefix = settings.prefix();

        this.getUserQuery = "SELECT * FROM " + prefix + "users WHERE uuid = ?;";
        this.getUserByNameQuery = "SELECT * FROM " + prefix + "users WHERE last_known_name = ?;";

        this.saveUserQuery = "INSERT INTO " + prefix + "users (uuid, last_known_name, claim_blocks, last_online) " +
                "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "last_known_name = VALUES(last_known_name), " +
                "claim_blocks = VALUES(claim_blocks), " +
                "last_online = VALUES(last_online);";
    }

    @Override
    public @NotNull CompletableFuture<User> getUser(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.getUserQuery)) {
                ps.setBytes(1, ByteUtil.UUIDtoBytes(uuid));

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return buildUser(rs);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load user " + uuid, e);
            }

            return null;
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<User> getUserByName(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.getUserByNameQuery)) {
                ps.setString(1, name);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return buildUser(rs);
                    }
                }

                return null;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to lookup user by name: " + name, e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveUser(@NotNull User user) {
        return CompletableFuture.runAsync(() -> {
            try (Connection con = storage.getConnection();
                 PreparedStatement ps = con.prepareStatement(this.saveUserQuery)) {
                ps.setBytes(1, ByteUtil.UUIDtoBytes(user.uuid()));
                ps.setString(2, user.lastKnownName());
                ps.setLong(3, user.claimBlocks());
                ps.setTimestamp(4, Timestamp.from(user.lastOnline()));

                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save user " + user.uuid(), e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveUsers(@NotNull List<User> users) {
        return CompletableFuture.runAsync(() -> {
            if (users.isEmpty()) return;

            try (Connection con = storage.getConnection()) {
                con.setAutoCommit(false);

                try (PreparedStatement ps = con.prepareStatement(this.saveUserQuery)) {
                    for (User user : users) {
                        ps.setBytes(1, ByteUtil.UUIDtoBytes(user.uuid()));
                        ps.setString(2, user.lastKnownName());
                        ps.setLong(3, user.claimBlocks());
                        ps.setTimestamp(4, Timestamp.from(user.lastOnline()));

                        ps.addBatch();
                    }

                    ps.executeBatch();
                }

                con.commit();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to batch save users", e);
            }
        }, executor);
    }

    /**
     * Maps a single row from a {@link ResultSet} into a {@link User} object.
     *
     * @param rs The result set positioned at the desired row.
     * @return A fully populated User instance.
     * @throws SQLException If a required column is missing or data conversion fails.
     */
    private User buildUser(ResultSet rs) throws SQLException {
        UUID uuid = ByteUtil.bytesToUUID(rs.getBytes("uuid"));
        User user = new User(
                uuid,
                rs.getString("last_known_name"),
                rs.getLong("claim_blocks")
        );

        user.persistent(true);
        user.lastOnline(rs.getTimestamp("last_online").toInstant());

        return user;
    }
}