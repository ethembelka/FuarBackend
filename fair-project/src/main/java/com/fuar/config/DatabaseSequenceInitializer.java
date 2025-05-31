package com.fuar.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DatabaseSequenceInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Reset sequences for all tables that use identity columns
        resetSequence("users");
        resetSequence("events");
        resetSequence("token");
        // Add other sequences as needed
    }

    private void resetSequence(String tableName) {
        try {
            String query = String.format(
                "SELECT setval('%s_id_seq', COALESCE((SELECT MAX(id) FROM %s), 1), true)",
                tableName, tableName
            );
            jdbcTemplate.execute(query);
        } catch (Exception e) {
            // Log error and continue - this isn't fatal if it fails
            System.err.println("Failed to reset sequence for table " + tableName + ": " + e.getMessage());
        }
    }
}
