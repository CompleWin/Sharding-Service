package ru.complewin.shardingservice.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import ru.complewin.shardingservice.domain.models.ShardRow;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ShardingRepositoryImpl implements ShardingRepository {

    private final JdbcClient jdbc;

    @Autowired
    public ShardingRepositoryImpl(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public ShardRow insert(UUID objectId, short shardIndex) {
        Instant now = Instant.now();
        jdbc.sql("""
                INSERT INTO object_shard (object_id, shard_index, updated_at)
                VALUES(:id, :idx, :now)
                """)
                .param("id", objectId)
                .param("idx", shardIndex)
                .param("now", Timestamp.from(now))
                .update();

        return new ShardRow(objectId, shardIndex, now);
    }

    @Override
    public Optional<ShardRow> update(UUID objectId, short shardIndex) {
        Instant now = Instant.now();

        int rows = jdbc.sql("""
                UPDATE object_shard
                SET shard_index = :idx,
                    updated_at = :now
                WHERE object_id = :id
                """)
                .param("id", objectId)
                .param("idx", shardIndex)
                .param("now", Timestamp.from(now))
                .update();

        if (rows == 0) {
            return Optional.empty();
        }

        return Optional.of(new ShardRow(objectId, shardIndex, now));
    }

    @Override
    public Optional<ShardRow> findById(UUID objectId) {
        return jdbc.sql("""
                SELECT object_id, shard_index, updated_at
                FROM object_shard
                WHERE object_id = :id
                """)
                .param("id", objectId)
                .query((rs, rowNum) -> new ShardRow(
                        (UUID) rs.getObject("object_id"),
                        rs.getShort("shard_index"),
                        rs.getTimestamp("updated_at").toInstant()
                ))
                .optional();
    }

    @Override
    public List<UUID> sampleIds(int limit) {
        return jdbc.sql("""
                SELECT object_id FROM object_shard
                TABLESAMPLE SYSTEM (1)
                LIMIT :limit
                """)
                .param("limit", limit)
                .query((rs, rowNum) -> (UUID) rs.getObject("object_id"))
                .list();
    }

    @Override
    public long count() {
        Long n = jdbc.sql("SELECT COUNT(*) FROM object_shard")
                .query(Long.class)
                .single();
        return n == null ? 0L : n;
    }
}
