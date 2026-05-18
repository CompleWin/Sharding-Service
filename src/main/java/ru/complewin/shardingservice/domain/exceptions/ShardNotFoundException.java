package ru.complewin.shardingservice.domain.exceptions;

import java.util.UUID;

public class ShardNotFoundException extends RuntimeException {
    public ShardNotFoundException(UUID objectId) {
        super("Для объекта = " + objectId + " не найдено шарда");
    }
}
