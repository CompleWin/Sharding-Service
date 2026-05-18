package ru.complewin.shardingservice.api.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.complewin.shardingservice.api.dto.CreateShardRequestDto;
import ru.complewin.shardingservice.api.dto.ShardResponseDto;
import ru.complewin.shardingservice.api.dto.UpdateShardRequestDto;
import ru.complewin.shardingservice.service.ShardingService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shard")
public class ShardController {

    private final ShardingService service;

    @Autowired
    public ShardController(ShardingService service) {
        this.service = service;
    }

    @GetMapping("/get/{objectId}")
    public ResponseEntity<ShardResponseDto> get(@PathVariable UUID objectId) {
        return ResponseEntity.ok(service.get(objectId));
    }

    @PostMapping("/create/{objectId}")
    public ResponseEntity<ShardResponseDto> create(@PathVariable UUID objectId,
                                                   @Valid @RequestBody CreateShardRequestDto createDto) {
        ShardResponseDto response = service.create(objectId, createDto.shardIndex());
        return ResponseEntity
                .created(URI.create("/api/shard/get/" + response.objectId()))
                .body(response);
    }

    @PutMapping("/update/{objectId}")
    public ResponseEntity<ShardResponseDto> update(@PathVariable UUID objectId,
                                                   @Valid @RequestBody UpdateShardRequestDto updateDto) {
        ShardResponseDto response = service.update(objectId, updateDto.shardIndex());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sample")
    public List<UUID> sample(@RequestParam(defaultValue = "10000") int limit) {
        int safe = Math.clamp(limit, 1, 100_000);
        return service.sampleIds(safe);
    }
}
