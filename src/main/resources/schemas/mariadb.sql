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
    uuid           BINARY(16) PRIMARY KEY,
    owner          BINARY(16)              NOT NULL,
    name VARCHAR(64) NOT NULL,
    world_name     VARCHAR(64)             NOT NULL,
    min_x          INT                     NOT NULL,
    max_x          INT                     NOT NULL,
    min_z          INT                     NOT NULL,
    max_z          INT                     NOT NULL,
    parent_uuid    BINARY(16)              NULL,
    locked         BOOLEAN   DEFAULT FALSE NOT NULL,
    claimed_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    roles          LONGBLOB                NOT NULL,
    members        LONGBLOB                NOT NULL,
    settings       LONGBLOB                NOT NULL,

    schema_version INT       DEFAULT 1     NOT NULL,

    CONSTRAINT fk_parent FOREIGN KEY (parent_uuid)
        REFERENCES `{prefix}claims` (uuid) ON DELETE CASCADE,
    CONSTRAINT fk_claim_owner FOREIGN KEY (owner)
        REFERENCES `{prefix}users` (uuid) ON DELETE CASCADE,

    INDEX idx_claims_spatial (world_name, min_x, max_x, min_z, max_z)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;