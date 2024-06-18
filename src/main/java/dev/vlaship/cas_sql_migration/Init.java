package dev.vlaship.cas_sql_migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class Init {

    //language=CQL
    private static final String CQL_GET_UNIQUE_IDS = "SELECT DISTINCT requestId FROM operation";
    //language=SQL
    private static final String AZURE_BATCH_INSERT = "INSERT INTO tbl_migration (migration_id, request_id) VALUES (?,?)";

    private final CassandraTemplate cassandraTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Value("${chuncks}")
    private int chuncks;

    public void init() {
        log.info("init");

        log.info("get requestId from cassandra");
        var ids = cassandraTemplate.select(CQL_GET_UNIQUE_IDS, String.class);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tbl_migration (
                migration_id uuid,
                request_id varchar,
                status varchar default 'NEW',
                )
                """);

        // divide ids into chunks
        if (!CollectionUtils.isEmpty(ids)) {
            log.info("insert requestId to postgres");
            for (var i = 0; i < ids.size(); i += chuncks) {
                var toIndex = Math.min(i + chuncks, ids.size());
                var chunk = ids.subList(i, toIndex);
                var migrationId = UUID.randomUUID();
                jdbcTemplate.batchUpdate(AZURE_BATCH_INSERT, chunk, chunk.size(), (ps, argument) -> {
                    ps.setObject(1, migrationId);
                    ps.setObject(2, argument);
                });
            }
        }

        log.info("init done");
    }
}
