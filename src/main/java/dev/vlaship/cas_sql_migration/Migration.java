package dev.vlaship.cas_sql_migration;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Migration {

    //language=CQL
    private static final String CAS_GET_DATA = "SELECT * FROM item WHERE migration_id = ?";
    //language=SQL
    private static final String AZURE_BATCH_INSERT = "INSERT INTO item (request_id) VALUES (?)";
    private static final String AZURE_MARK_MIGRATION_COMPLETED = "UPDATE tbl_migration SET status = 'COMPLETED' WHERE migration_id = ?";

    private final ChunkProvider chunkProvider;
    private final CassandraTemplate cassandraTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void migrate() {
        log.info("migration started");

        var migrationId = chunkProvider.getMigrationId();
        while (migrationId != null) {
            log.info("migrationId: {}", migrationId);

            try {
                var list = getMigrationData(migrationId);
                insertMigrationData(list);
                markMigrationCompleted(migrationId);
            } catch (Exception e) {
                log.error("Error while migrating data. id: {} ex: {}", migrationId, e.getMessage(), e);
            }

            migrationId = chunkProvider.getMigrationId();
        }

        log.info("migration done");
    }

    private void markMigrationCompleted(@NonNull String migrationId) {
        jdbcTemplate.update(AZURE_MARK_MIGRATION_COMPLETED, migrationId);
    }

    private void insertMigrationData(@NonNull List<Item> list) {
        if (list.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(AZURE_BATCH_INSERT, list, list.size(), (ps, argument) -> {
            ps.setObject(1, argument.requestId());
        });
    }

    @NonNull
    private List<Item> getMigrationData(@NonNull String migrationId) {
        var statement = SimpleStatement.newInstance(CAS_GET_DATA, 1, migrationId);
        return cassandraTemplate.select(statement, Item.class);
    }
}
