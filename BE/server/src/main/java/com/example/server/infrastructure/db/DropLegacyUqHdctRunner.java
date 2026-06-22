package com.example.server.infrastructure.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Drops the legacy unique constraint uq_hdct that prevents the POS system from
 * storing two order-detail rows with the same variant but different unit prices
 * (which happens when a product's price changes while it's already in the cart).
 *
 * This runner is idempotent: it safely ignores the error if the constraint no
 * longer exists.
 */
@Component
public class DropLegacyUqHdctRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DropLegacyUqHdctRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public DropLegacyUqHdctRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            // Check whether the constraint still exists first
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
                "WHERE CONSTRAINT_NAME = 'uq_hdct' AND TABLE_NAME = 'hoa_don_chi_tiet'",
                Integer.class
            );
            if (count != null && count > 0) {
                jdbcTemplate.execute("ALTER TABLE hoa_don_chi_tiet DROP CONSTRAINT uq_hdct");
                log.info("Successfully dropped legacy constraint uq_hdct from hoa_don_chi_tiet.");
            } else {
                log.debug("Constraint uq_hdct does not exist – nothing to drop.");
            }
        } catch (DataAccessException ex) {
            log.warn("Could not drop constraint uq_hdct: {}", ex.getMessage());
        }
    }
}
