package ru.complewin.shardingservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import ru.complewin.shardingservice.api.dto.ShardResponseDto;
import ru.complewin.shardingservice.domain.ShardingRepository;
import ru.complewin.shardingservice.domain.exceptions.ShardAlreadyExistsException;
import ru.complewin.shardingservice.domain.exceptions.ShardNotFoundException;
import ru.complewin.shardingservice.domain.models.ShardRow;
import ru.complewin.shardingservice.utils.MyMapper;

import java.util.List;
import java.util.UUID;

@Service
public class ShardingServiceImpl implements ShardingService {

    private final ShardingRepository repo;
    private final MyMapper mapper;

    @Autowired
    public ShardingServiceImpl(ShardingRepository repo, MyMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public ShardResponseDto create(UUID objectId, short shardIndex) {
        try {
            ShardRow row = repo.insert(objectId, shardIndex);
            return mapper.toShardResponseDto(row);
        } catch (DuplicateKeyException ex) {
            throw new ShardAlreadyExistsException(objectId);
        }
    }

    @Override
    public ShardResponseDto update(UUID objectId, short shardIndex) {
        return repo.update(objectId, shardIndex)
                .map(mapper::toShardResponseDto)
                .orElseThrow(() -> new ShardNotFoundException(objectId));
    }

    @Override
    public ShardResponseDto get(UUID objectId) {
        return repo.findById(objectId)
                .map(mapper::toShardResponseDto)
                .orElseThrow(() -> new ShardAlreadyExistsException(objectId));
    }

    @Override
    public List<UUID> sampleIds(int limit) {
        return repo.sampleIds(limit);
    }
}
