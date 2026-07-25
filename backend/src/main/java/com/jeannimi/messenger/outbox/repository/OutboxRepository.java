package com.jeannimi.messenger.outbox.repository;

import com.jeannimi.messenger.outbox.entity.OutboxEvent;
import com.jeannimi.messenger.outbox.entity.OutboxStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

  @Query(
      """
        select e
        from OutboxEvent e
        where e.status = :status
        order by e.id
    """)
  List<OutboxEvent> findBatch(OutboxStatus status, Pageable pageable);
}
