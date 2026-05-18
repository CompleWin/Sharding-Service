package ru.complewin.shardingservice.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ShardResponseDto(UUID objectId, short shardIndex, Instant updatedAt) {
}
