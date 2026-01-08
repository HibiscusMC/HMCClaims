CREATE TABLE `{prefix}claims`
(
    uuid                UUID PRIMARY KEY,
    owner               UUID                    NOT NULL,
    name                VARCHAR(32)             NOT NULL,
    world_name          VARCHAR(64)             NOT NULL,
    region              VARCHAR(64)             NOT NULL,
    parent_uuid         UUID                    NULL,
    inherit_permissions BOOLEAN   DEFAULT TRUE  NOT NULL,
    locked              BOOLEAN   DEFAULT FALSE NOT NULL,
    claimed_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_parent FOREIGN KEY (parent_uuid)
        REFERENCES `{prefix}claims` (uuid) ON DELETE CASCADE,

    INDEX idx_world (world_name)
) DEFAULT CHARSET = utf8mb4;

CREATE TABLE `{prefix}members`
(
    claim_uuid  UUID NOT NULL,
    player_uuid
) DEFAULT CHARSET = utf8mb4;