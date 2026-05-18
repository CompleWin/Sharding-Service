package ru.complewin.shardingservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.complewin.shardingservice.api.dto.CreateShardRequestDto;
import ru.complewin.shardingservice.api.dto.ShardResponseDto;
import ru.complewin.shardingservice.api.dto.UpdateShardRequestDto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShardingServiceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @LocalServerPort
    int port;

    @Autowired
    private JdbcClient jdbc;

    private RestClient rest;

    private String baseUrl = "/api/shard/";
    private String postUrl = baseUrl + "create/{id}";
    private String getUrl = baseUrl + "get/{id}";
    private String putUrl = baseUrl + "update/{id}";
    private short shardIndex = 69;
    private short updateShardIndex = 67;
    private short shardIndexOutOfRange = 6969;

    @BeforeEach
    void setUp() {

        rest = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (req, resp) -> {
                })
                .build();

        jdbc.sql("TRUNCATE TABLE object_shard").update();
    }

    @Test
    void contextLoadsAndContainerRuns() {
        assertThat(POSTGRES.isRunning()).isTrue();
    }

    @Test
    void createThenGet_returnsSameShard() {
        UUID id = UUID.randomUUID();

        var created = rest.post()
                .uri(postUrl, id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateShardRequestDto(shardIndex))
                .retrieve()
                .toEntity(ShardResponseDto.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().objectId()).isEqualTo(id);
        assertThat(created.getBody().shardIndex()).isEqualTo(shardIndex);
        assertThat(created.getHeaders().getLocation())
                .isNotNull()
                .hasToString("/api/shard/get/" + id);

        var got = rest.get()
                .uri(getUrl, id)
                .retrieve()
                .toEntity(ShardResponseDto.class);

        assertThat(got.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(got.getBody()).isNotNull();
        assertThat(got.getBody().shardIndex()).isEqualTo(shardIndex);
    }

    @Test
    void create_twice_returnsConflict() {
        UUID id = UUID.randomUUID();

        rest.post()
                .uri(postUrl, id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateShardRequestDto((short) 67))
                .retrieve()
                .toBodilessEntity();

        var second = rest.post()
                .uri(postUrl, id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateShardRequestDto((short) 76))
                .retrieve()
                .toEntity(ProblemDetail.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void create_withShardIndexOutOfRange_returnsBadRequest() {
        var response = rest.post()
                .uri(postUrl, UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateShardRequestDto(shardIndexOutOfRange))
                .retrieve()
                .toEntity(ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void get_unknownId_returnsNotFound() {
        var response = rest.get()
                .uri(getUrl, UUID.randomUUID())
                .retrieve()
                .toEntity(ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void update_existing_changesShardIndex(){
        UUID id = UUID.randomUUID();

        rest.post()
                .uri(postUrl, id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateShardRequestDto(shardIndex))
                .retrieve()
                .toBodilessEntity();

        var updated = rest.put()
                .uri(putUrl, id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateShardRequestDto(updateShardIndex))
                .retrieve()
                .toEntity(ShardResponseDto.class);

        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody()).isNotNull();
        assertThat(updated.getBody().shardIndex()).isEqualTo(updateShardIndex);

        var got = rest.get()
                .uri(getUrl, id)
                .retrieve()
                .toEntity(ShardResponseDto.class);

        assertThat(got.getBody()).isNotNull();
        assertThat(got.getBody().shardIndex()).isEqualTo(updateShardIndex);
    }

    @Test
    void update_unknownId_returnsNotFound() {
        var response = rest.put()
                .uri(putUrl, UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateShardRequestDto(updateShardIndex))
                .retrieve()
                .toEntity(ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void sample_returnsAtMostLimitIds() {
        for (int i = 0; i < 50; i++) {
            rest.post()
                    .uri(postUrl, UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CreateShardRequestDto((short) (i % 1024)))
                    .retrieve()
                    .toBodilessEntity();
        }

        UUID[] body = rest.get()
                .uri(baseUrl + "sample?limit=10")
                .retrieve()
                .body(UUID[].class);

        assertThat(body).isNotNull();
        assertThat(body.length).isLessThanOrEqualTo(10);
    }

    @Test
    void concurrentUpdates_sameId_noErrors() throws InterruptedException {
        UUID id = UUID.randomUUID();
        rest.post()
                .uri(postUrl, id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateShardRequestDto((short) 0))
                .retrieve()
                .toBodilessEntity();

        int threads = 16;
        int iterationsPerThread = 25;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        AtomicInteger failures = new AtomicInteger();

        try {
            List<Future<?>> futures = new ArrayList<>();

            for (int t = 0; t < threads; t++) {
                final int threadIdx = t;
                futures.add(pool.submit(() -> {
                    for (int i = 0; i < iterationsPerThread; i++) {
                        try {
                            short value = (short) ((threadIdx * 31 + i) % 1024);
                            rest.put()
                                    .uri(putUrl, id)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(new UpdateShardRequestDto(value))
                                    .retrieve()
                                    .toBodilessEntity();
                        } catch (Exception e) {
                            failures.incrementAndGet();
                        }
                    }
                }));
            }
        } finally {
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.SECONDS);
        }

        assertThat(failures.get()).isZero();

        var got = rest.get()
                .uri(getUrl, id)
                .retrieve()
                .toEntity(ShardResponseDto.class);

        assertThat(got.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(got.getBody()).isNotNull();
        assertThat(got.getBody().shardIndex()).isBetween((short) 0, (short) 1023);
    }

    @Test
    void concurrentCreates_sameId_exactlyOneSucceeds() throws Exception {
        UUID id = UUID.randomUUID();

        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        AtomicInteger created = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();

        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                final short idx = (short) t;
                futures.add(pool.submit(() -> {
                    var response = rest.post()
                            .uri(postUrl, id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(new CreateShardRequestDto(idx))
                            .retrieve()
                            .toBodilessEntity();
                    if (response.getStatusCode() == HttpStatus.CREATED) {
                        created.incrementAndGet();
                    }
                    else if (response.getStatusCode() == HttpStatus.CONFLICT) {
                        conflicts.incrementAndGet();
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get(15, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.SECONDS);
        }

        assertThat(created.get()).isEqualTo(1);
        assertThat(conflicts.get()).isEqualTo(threads - 1);
    }
}
