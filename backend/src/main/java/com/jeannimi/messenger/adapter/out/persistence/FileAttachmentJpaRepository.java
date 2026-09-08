package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.FileAttachmentJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileAttachmentJpaRepository extends JpaRepository<FileAttachmentJpaEntity, UUID> {

  @Query("""
      SELECT f.storageFileName
      FROM FileAttachmentJpaEntity f
      """)
  List<String> findAllStorageFileNames();

  @Query(
      """
      SELECT f
      FROM FileAttachmentJpaEntity f
      WHERE NOT EXISTS (
          SELECT m.id
          FROM MessageJpaEntity m
          WHERE m.attachment.id = f.id
      )
      """)
  List<FileAttachmentJpaEntity> findOrphanAttachments();

  @Modifying
  @Query("""
      DELETE FROM FileAttachmentJpaEntity f
      WHERE f.id IN :ids
      """)
  void deleteAllByIds(@Param("ids") List<UUID> ids);
}
