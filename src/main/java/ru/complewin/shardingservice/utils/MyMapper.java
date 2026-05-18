package ru.complewin.shardingservice.utils;

import org.springframework.stereotype.Component;
import ru.complewin.shardingservice.api.dto.ShardResponse;
import ru.complewin.shardingservice.domain.models.ShardRow;

@Component
public class MyMapper {
    public ShardResponse toShardResponseDto(ShardRow row) {
        return new ShardResponse(row.objectId(), row.shardIndex(), row.updatedAt());
    }

}
