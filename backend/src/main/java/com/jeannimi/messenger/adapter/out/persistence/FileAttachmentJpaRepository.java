package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.message.entity.FileAttachment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileAttachmentJpaRepository extends JpaRepository<FileAttachment, UUID> {

  @Query("""
        select f.storageFileName
        from FileAttachment f
    """)
  List<String> findAllStorageFileNames();

  @Query(
      """
      SELECT f
      FROM FileAttachment f
      WHERE NOT EXISTS (
          SELECT m.id
          FROM Message m
          WHERE m.attachment.id = f.id
      )
  """)
  List<FileAttachment> findOrphanAttachments();

  @Modifying
  @Query("""
      delete from FileAttachment f
      where f.id in :ids
  """)
  void deleteAllByIds(@Param("ids") List<UUID> ids);
}
