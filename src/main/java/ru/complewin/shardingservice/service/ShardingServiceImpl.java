package ru.complewin.shardingservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.complewin.shardingservice.api.dto.ShardResponse;
import ru.complewin.shardingservice.domain.ShardingRepository;

import java.util.List;
import java.util.UUID;

@Service
public class ShardingServiceImpl implements ShardingService {

    private final ShardingRepository repo;

    @Autowired
    public ShardingServiceImpl(ShardingRepository repo) {
        this.repo = repo;
    }

    @Override
    public ShardResponse create(UUID objectId, short shardIndex) {
        return null;
    }

    @Override
    public ShardResponse update(UUID objectId, short shardIndex) {
        return null;
    }

    @Override
    public ShardResponse get(UUID objectId) {
        return null;
    }

    @Override
    public List<UUID> sampleIds(int limit) {
        return List.of();
    }
}
