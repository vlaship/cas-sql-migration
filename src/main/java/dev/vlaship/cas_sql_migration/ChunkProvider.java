package dev.vlaship.cas_sql_migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChunkProvider {

    private final JdbcTemplate jdbcTemplate;

    @Nullable
    public synchronized String getMigrationId() {
        log.info("provideChunk");

        try {
            var migration_id = jdbcTemplate.queryForObject("SELECT migration_id FROM tbl_migration WHERE status = 'NEW' LIMIT 1", String.class);

            if (migration_id != null) {
                jdbcTemplate.update("UPDATE tbl_migration SET status = 'IN_PROGRESS' WHERE migration_id = ?", migration_id);
            }

            return migration_id;
        } catch (Exception e) {
            log.error("Error while getting migration id. {}", e.getMessage(), e);
            return null;
        }
    }
}
