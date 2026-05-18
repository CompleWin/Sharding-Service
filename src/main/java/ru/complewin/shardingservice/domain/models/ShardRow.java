package ru.complewin.shardingservice.domain.models;

import java.time.Instant;
import java.util.UUID;

public record ShardRow(UUID objectId, short shardIndex, Instant updatedAt) {
}
