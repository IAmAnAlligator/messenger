package com.jeannimi.messenger.repository;

import com.jeannimi.messenger.entity.OutboxEvent;
import com.jeannimi.messenger.entity.OutboxStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

  List<OutboxEvent> findByStatus(OutboxStatus status);
}
