CREATE TABLE IF NOT EXISTS `{prefix}users`
(
    uuid            BINARY(16) PRIMARY KEY,
    last_known_name VARCHAR(16) NOT NULL,
    claim_blocks    INT       DEFAULT 0,
    last_online     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_name (last_known_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `{prefix}claims`
(
    uuid                BINARY(16) PRIMARY KEY,
    owner               BINARY(16)              NOT NULL,
    name                VARCHAR(32)             NOT NULL,
    world_name          VARCHAR(64)             NOT NULL,
    region              VARCHAR(64)             NOT NULL,
    parent_uuid         BINARY(16)              NULL,
    inherit_permissions BOOLEAN   DEFAULT TRUE  NOT NULL,
    locked              BOOLEAN   DEFAULT FALSE NOT NULL,
    claimed_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_parent FOREIGN KEY (parent_uuid)
        REFERENCES `{prefix}claims` (uuid) ON DELETE CASCADE,
    CONSTRAINT fk_claim_owner FOREIGN KEY (owner)
        REFERENCES `{prefix}users` (uuid) ON DELETE CASCADE,

    INDEX idx_world (world_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `{prefix}claim_roles`
(
    claim_uuid BINARY(16)  NOT NULL,
    role_id    BINARY(16)  NOT NULL,
    name       VARCHAR(32) NOT NULL,
    position   INT         NOT NULL,

    PRIMARY KEY (claim_uuid, role_id),

    CONSTRAINT fk_role_claim FOREIGN KEY (claim_uuid)
        REFERENCES `{prefix}claims` (uuid) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `{prefix}claim_settings`
(
    claim_uuid    BINARY(16)   NOT NULL,
    setting_key   VARCHAR(32)  NOT NULL,
    setting_value VARCHAR(128) NOT NULL,

    PRIMARY KEY (claim_uuid, setting_key),

    CONSTRAINT fk_settings_claim FOREIGN KEY (claim_uuid)
        REFERENCES `{prefix}claims` (uuid) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `{prefix}members`
(
    claim_uuid  BINARY(16)              NOT NULL,
    player_uuid BINARY(16)              NOT NULL,
    role        VARCHAR(16)             NOT NULL,
    banned      BOOLEAN   DEFAULT FALSE NOT NULL,
    joined_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (claim_uuid, player_uuid),

    CONSTRAINT fk_claim FOREIGN KEY (claim_uuid)
        REFERENCES `{prefix}claims` (uuid) ON DELETE CASCADE,
    CONSTRAINT fk_member_user FOREIGN KEY (player_uuid)
        REFERENCES `{prefix}users` (uuid) ON DELETE CASCADE,

    INDEX idx_player_claims (player_uuid)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `{prefix}permissions`
(
    claim_uuid  BINARY(16)  NOT NULL,
    player_uuid BINARY(16)  NOT NULL,
    permission  VARCHAR(32) NOT NULL,
    value       BOOLEAN     NOT NULL,

    PRIMARY KEY (claim_uuid, player_uuid, permission),

    CONSTRAINT fk_member_perms FOREIGN KEY (claim_uuid, player_uuid)
        REFERENCES `{prefix}members` (claim_uuid, player_uuid)
        ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;