\timing on

TRUNCATE TABLE object_shard;

DO $$
DECLARE
    batch_size INT := 500000;
    total INT := 5000000;
    batches INT := total / batch_size;
    i INT;
BEGIN
    FOR i IN 1..batches LOOP
        INSERT INTO object_shard (object_id, shard_index, updated_at)
        SELECT
            gen_random_uuid(),
            (random() * 1023)::smallint,
            now()
        FROM generate_series(1, batch_size);

        RAISE NOTICE 'Batch %/%: inserted % rows total',
              i, batches, i * batch_size;
    END LOOP;
END $$;

ANALYZE object_shard;

SELECT COUNT(*) AS total_rows FROM object_shard;
SELECT shard_index, COUNT(*)
FROM object_shard
GROUP BY shard_index
ORDER BY shard_index
LIMIT 5;