package ru.complewin.shardingservice.domain;

import ru.complewin.shardingservice.domain.models.ShardRow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShardingRepository {

    ShardRow insert(UUID objectId, short shardIndex);
    Optional<ShardRow> update(UUID objectId, short shardIndex);
    Optional<ShardRow> findById(UUID objectId);
    List<UUID> sampleIds(int limit);
    long count();

}
