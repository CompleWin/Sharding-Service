package ru.complewin.shardingservice.service;

import ru.complewin.shardingservice.api.dto.ShardResponse;

import java.util.List;
import java.util.UUID;

public interface ShardingService {
    ShardResponse create(UUID objectId, short shardIndex);
    ShardResponse update(UUID objectId, short shardIndex);
    ShardResponse get(UUID objectId);
    List<UUID> sampleIds(int limit);
}
