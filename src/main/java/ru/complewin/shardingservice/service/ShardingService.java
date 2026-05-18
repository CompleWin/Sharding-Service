package ru.complewin.shardingservice.service;

import ru.complewin.shardingservice.api.dto.ShardResponseDto;

import java.util.List;
import java.util.UUID;

public interface ShardingService {
    ShardResponseDto create(UUID objectId, short shardIndex);
    ShardResponseDto update(UUID objectId, short shardIndex);
    ShardResponseDto get(UUID objectId);
    List<UUID> sampleIds(int limit);
}
