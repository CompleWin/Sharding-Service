package ru.complewin.shardingservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import ru.complewin.shardingservice.api.dto.ShardResponseDto;
import ru.complewin.shardingservice.domain.ShardingRepository;
import ru.complewin.shardingservice.domain.exceptions.ShardAlreadyExistsException;
import ru.complewin.shardingservice.domain.exceptions.ShardNotFoundException;
import ru.complewin.shardingservice.domain.models.ShardRow;
import ru.complewin.shardingservice.utils.MyMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ShardingServiceImplTest {

    @Mock
    private ShardingRepository repo;

    @Mock
    private MyMapper mapper;

    @InjectMocks
    private ShardingServiceImpl service;

    private final UUID objectId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final short shardIndex = (short) 42;

    @Test
    void create_happyPath_returnMappedDto() {
        ShardRow row = new ShardRow(objectId, shardIndex, Instant.now());
        ShardResponseDto dto = new ShardResponseDto(objectId, shardIndex, row.updatedAt());

        when(repo.insert(objectId, shardIndex)).thenReturn(row);
        when(mapper.toShardResponseDto(row)).thenReturn(dto);

        assertThat(service.create(objectId, shardIndex)).isEqualTo(dto);
        verify(repo).insert(objectId, shardIndex);
    }

    @Test
    void create_whenRepoThrowsDuplicate_wrapsInShardAlreadyExists() {
        when(repo.insert(any(UUID.class), anyShort()))
                .thenThrow(new DuplicateKeyException("PK violation"));

        assertThatThrownBy(() -> service.create(objectId, shardIndex))
                .isInstanceOf(ShardAlreadyExistsException.class)
                .hasMessageContaining(objectId.toString());
    }

    @Test
    void update_whenExists_returnsMappedDto() {
        ShardRow row = new ShardRow(objectId, shardIndex, Instant.now());
        ShardResponseDto dto = new ShardResponseDto(objectId, shardIndex, row.updatedAt());

        when(repo.update(objectId, shardIndex)).thenReturn(Optional.of(row));
        when(mapper.toShardResponseDto(row)).thenReturn(dto);

        assertThat(service.update(objectId, shardIndex)).isEqualTo(dto);
    }

    @Test
    void update_whenMissing_throwsShardNotFound() {
        when(repo.update(objectId, shardIndex)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(objectId, shardIndex))
                .isInstanceOf(ShardNotFoundException.class)
                .hasMessageContaining(objectId.toString());
    }

    @Test
    void get_whenExists_returnsMappedDto() {
        ShardRow row = new ShardRow(objectId, shardIndex, Instant.now());
        ShardResponseDto dto = new ShardResponseDto(objectId, shardIndex, row.updatedAt());

        when(repo.findById(objectId)).thenReturn(Optional.of(row));
        when(mapper.toShardResponseDto(row)).thenReturn(dto);

        assertThat(service.get(objectId)).isEqualTo(dto);
    }

    @Test
    void get_whenMissing_throwsShardNotFound() {
        when(repo.findById(objectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(objectId))
                .isInstanceOf(ShardNotFoundException.class)
                .hasMessageContaining(objectId.toString());
    }

    @Test
    void sampleIds_delegatesToRepo() {
        List<UUID> excepted = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(repo.sampleIds(100)).thenReturn(excepted);

        assertThat(service.sampleIds(100)).isEqualTo(excepted);
        verify(repo).sampleIds(100);
    }

}
