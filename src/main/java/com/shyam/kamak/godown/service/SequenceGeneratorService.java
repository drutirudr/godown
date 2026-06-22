package com.shyam.kamak.godown.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Slf4j
@Service
@RequiredArgsConstructor
public class SequenceGeneratorService {

    private final JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    // Runs inside its own isolated atomic container with zero connection layer leakage
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public Integer getNextSequence(String type, String financialYear) {
        String key = type + "_" + financialYear;

        jdbcTemplate.update(
                "INSERT IGNORE INTO system_sequences (sequence_key, next_value) VALUES (?, 1)",
                key
        );

        // This ROW LOCK holds processing on this specific key string, preventing duplicates
        // across server instances without locking the entire table or blocking other records.
        Integer currentValue = jdbcTemplate.queryForObject(
                "SELECT next_value FROM system_sequences WHERE sequence_key = ? FOR UPDATE",
                Integer.class,
                key
        );

        jdbcTemplate.update(
                "UPDATE system_sequences SET next_value = next_value + 1 WHERE sequence_key = ?",
                key
        );

        // FIX: Evict mapping anomalies by clearing raw state allocations
        if (entityManager != null) {
            entityManager.clear();
        }

        return currentValue;
    }
}

//@Slf4j
//@Service @RequiredArgsConstructor
//public class SequenceGeneratorService {
//    private final JdbcTemplate jdbcTemplate;
//    @PersistenceContext private EntityManager entityManager;
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
//    public Integer getNextSequence(String type, String financialYear) {
//        String key = type + "_" + financialYear;
//        jdbcTemplate.update("INSERT IGNORE INTO system_sequences (sequence_key, next_value) VALUES (?, 1)", key);
//        Integer currentValue = jdbcTemplate.queryForObject("SELECT next_value FROM system_sequences WHERE sequence_key = ? FOR UPDATE", Integer.class, key);
//        jdbcTemplate.update("UPDATE system_sequences SET next_value = next_value + 1 WHERE sequence_key = ?", key);
//        if (entityManager != null) { entityManager.clear(); }
//        return currentValue;
//    }
//}