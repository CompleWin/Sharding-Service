package ru.complewin.shardingservice.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateShardRequestDto(@NotNull
                                    @Min(0)
                                    @Max(1023)
                                    Short shardIndex) {
}
