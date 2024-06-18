package dev.vlaship.cas_sql_migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobMigration {

    private final Init init;
    private final Migration migration;

    @EventListener(ApplicationReadyEvent.class)
    public void migrate() {
        log.info("app is running");

        init.init();

        var threads = Runtime.getRuntime().availableProcessors();
        try (var executor = Executors.newFixedThreadPool(threads)) {
            for (var i = 0; i < threads; i++) {
                CompletableFuture.runAsync(migration::migrate, executor);
            }
        }

        log.info("app is done");
    }
}
