CREATE TABLE IF NOT EXISTS object_shard (
    object_id UUID PRIMARY KEY,
    shard_index SMALLINT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);