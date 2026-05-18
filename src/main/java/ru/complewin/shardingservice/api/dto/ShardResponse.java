package ru.complewin.shardingservice.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ShardResponse(UUID objectId, short shardIndex, Instant updatedAt) {
}
