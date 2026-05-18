package ru.complewin.shardingservice.api.controllers;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.complewin.shardingservice.api.dto.CreateShardRequestDto;
import ru.complewin.shardingservice.api.dto.ShardResponseDto;
import ru.complewin.shardingservice.api.dto.UpdateShardRequestDto;
import ru.complewin.shardingservice.domain.exceptions.ShardAlreadyExistsException;
import ru.complewin.shardingservice.domain.exceptions.ShardNotFoundException;
import ru.complewin.shardingservice.service.ShardingService;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ShardController.class)

public class ShardControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @MockitoBean
    private ShardingService service;

    private final UUID objectId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void get_existing_returns200() throws Exception {
        when(service.get(objectId)).thenReturn(
                new ShardResponseDto(objectId, (short) 69, Instant.parse("2026-01-01T00:00:00Z"))
        );

        mvc.perform(get("/api/shard/get/{id}", objectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objectId").value(objectId.toString()))
                .andExpect(jsonPath("$.shardIndex").value(69));
    }

    @Test
    void get_missing_returns404() throws Exception {
        when(service.get(objectId)).thenThrow(new ShardNotFoundException(objectId));

        mvc.perform(get("/api/shard/get/{id}", objectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(containsString(objectId.toString())));
    }

    @Test
    void create_valid_returns201WithLocationHeader() throws Exception {
        when(service.create(any(UUID.class), anyShort()))
                .thenReturn(new ShardResponseDto(objectId, (short) 69, Instant.now()));

        mvc.perform(post("/api/shard/create/{id}", objectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new CreateShardRequestDto((short) 69))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/shard/get/" + objectId))
                .andExpect(jsonPath("$.shardIndex").value(69));
    }

    @Test
    void create_duplicate_returns409() throws Exception {
        when(service.create(any(UUID.class), anyShort()))
                .thenThrow(new ShardAlreadyExistsException(objectId));

        mvc.perform(post("/api/shard/create/{id}", objectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new CreateShardRequestDto((short) 69))))
                .andExpect(status().isConflict());
    }

    @Test
    void create_negativeIndex_returns400() throws Exception {
        mvc.perform(post("/api/shard/create/{id}", objectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new CreateShardRequestDto((short) -69))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_indexAboveMax_returns400() throws Exception {
        mvc.perform(post("/api/shard/create/{id}", objectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new CreateShardRequestDto((short) 1069))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_nullIndex_returns400() throws Exception {
        mvc.perform(post("/api/shard/create/{id}", objectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_valid_returns200() throws Exception {
        when(service.update(any(UUID.class), anyShort()))
                .thenReturn(new ShardResponseDto(objectId, (short) 67, Instant.now()));

        mvc.perform(put("/api/shard/update/{id}", objectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new UpdateShardRequestDto((short) 67))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shardIndex").value(67));
    }

    @Test
    void update_missing_returns404() throws Exception {
        when(service.update(any(UUID.class), anyShort()))
                .thenThrow(new ShardNotFoundException(objectId));

        mvc.perform(put("/api/shard/update/{id}", objectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new UpdateShardRequestDto((short) 96))))
                .andExpect(status().isNotFound());
    }

    @Test
    void sample_returnsList() throws Exception {
        when(service.sampleIds(10)).thenReturn(List.of(objectId));

        mvc.perform(get("/api/shard/sample")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(objectId.toString()));
    }
}
