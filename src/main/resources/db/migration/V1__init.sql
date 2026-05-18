CREATE TABLE object_shard (
    object_id   UUID        NOT NULL,
    shard_index SMALLINT    NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT object_shard_pk PRIMARY KEY (object_id),
    CONSTRAINT object_shard_index_range CHECK (shard_index BETWEEN 0 AND 1023)
);