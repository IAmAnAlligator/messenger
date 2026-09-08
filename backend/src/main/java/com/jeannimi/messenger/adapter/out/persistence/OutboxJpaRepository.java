package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.jeannimi.messenger.domain.outbox.OutboxStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxJpaRepository extends JpaRepository<OutboxEventJpaEntity, Long> {

  @Query(
      """
        select e
        from OutboxEventJpaEntity e
        where e.status = :status
        order by e.id
      """)
  List<OutboxEventJpaEntity> findBatch(@Param("status") OutboxStatus status, Pageable pageable);

  @Modifying
  @Query(
      """
        update OutboxEventJpaEntity e
        set e.status = com.jeannimi.messenger.domain.outbox.OutboxStatus.SENT
        where e.id = :id
      """)
  int markSent(@Param("id") Long id);

  @Modifying
  @Query(
      """
        update OutboxEventJpaEntity e
        set e.status = com.jeannimi.messenger.domain.outbox.OutboxStatus.FAILED
        where e.id = :id
      """)
  int markFailed(@Param("id") Long id);
}
