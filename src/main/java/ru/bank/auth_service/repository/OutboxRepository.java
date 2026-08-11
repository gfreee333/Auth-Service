package ru.bank.auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.bank.auth_service.model.entity.Outbox;

import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {
    @Query(value = "SELECT * FROM outbox WHERE status = 'PENDING' " +
            "AND (next_attempt_at IS NULL OR next_attempt_at <= now()) " +
            "ORDER BY created_at LIMIT :limit " +
            "FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<Outbox> findAndLockPendingEvents(@Param("limit") int limit);
}
