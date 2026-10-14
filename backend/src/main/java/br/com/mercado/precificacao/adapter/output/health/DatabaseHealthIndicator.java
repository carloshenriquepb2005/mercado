package br.com.mercado.precificacao.adapter.output.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("legacyDatabase")
public class DatabaseHealthIndicator implements HealthIndicator {
    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthIndicator.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Health.up().build();
        } catch (Exception exception) {
            log.warn("Banco legado indisponível: {}", exception.getClass().getSimpleName());
            return Health.down().build();
        }
    }
}

