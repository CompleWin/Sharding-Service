package ru.complewin.shardingservice.domain.exceptions;

import java.util.UUID;

public class ShardAlreadyExistsException extends RuntimeException {
    public ShardAlreadyExistsException(UUID objectId) {
        super("Шард уже есть у объекта = " + objectId);
    }
}
