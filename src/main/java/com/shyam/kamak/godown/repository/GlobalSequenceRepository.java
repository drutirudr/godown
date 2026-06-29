package com.shyam.kamak.godown.repository;

import com.shyam.kamak.godown.model.GlobalSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GlobalSequenceRepository extends JpaRepository<GlobalSequence, String> {

    // 🚀 FOR UPDATE locks the row securely until the transaction commits, preventing duplicates
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM GlobalSequence s WHERE s.entityName = :entityName")
    Optional<GlobalSequence> findAndLockByEntityName(@Param("entityName") String entityName);
}