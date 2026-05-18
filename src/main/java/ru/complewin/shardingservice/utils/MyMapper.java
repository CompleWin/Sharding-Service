package ru.complewin.shardingservice.utils;

import org.springframework.stereotype.Component;
import ru.complewin.shardingservice.api.dto.ShardResponseDto;
import ru.complewin.shardingservice.domain.models.ShardRow;

@Component
public class MyMapper {
    public ShardResponseDto toShardResponseDto(ShardRow row) {
        return new ShardResponseDto(row.objectId(), row.shardIndex(), row.updatedAt());
    }

}
