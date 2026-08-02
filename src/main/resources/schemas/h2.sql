CREATE TABLE IF NOT EXISTS `{prefix}users`
(
    uuid            BINARY(16) PRIMARY KEY,
    last_known_name VARCHAR(16) NOT NULL,
    claim_blocks    INT       DEFAULT 0,
    last_online     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_name ON `{prefix}users` (last_known_name);

CREATE TABLE IF NOT EXISTS `{prefix}claims`
(
    uuid           BINARY(16) PRIMARY KEY,
    owner          BINARY(16)              NOT NULL,
    name           VARCHAR(64)             NOT NULL,
    world_name     VARCHAR(64)             NOT NULL,
    min_x          INT                     NOT NULL,
    max_x          INT                     NOT NULL,
    min_z          INT                     NOT NULL,
    max_z          INT                     NOT NULL,
    parent_uuid    BINARY(16)              NULL,
    locked         BOOLEAN   DEFAULT FALSE NOT NULL,
    claimed_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    roles          BLOB                    NOT NULL,
    members        BLOB                    NOT NULL,
    settings       BLOB                    NOT NULL,

    schema_version INT       DEFAULT 1     NOT NULL,

    CONSTRAINT fk_parent FOREIGN KEY (parent_uuid)
        REFERENCES `{prefix}claims` (uuid) ON DELETE CASCADE,
    CONSTRAINT fk_claim_owner FOREIGN KEY (owner)
        REFERENCES `{prefix}users` (uuid) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_claims_spatial ON `{prefix}claims` (world_name, min_x, max_x, min_z, max_z);