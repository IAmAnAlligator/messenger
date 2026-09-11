package com.jeannimi.messenger.adapter.in.web.message.mapper;

import com.jeannimi.messenger.application.message.dto.FileDownloadResult;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class FileDownloadMapper {

  public ResponseEntity<InputStreamResource> toResponse(FileDownloadResult file) {

    InputStreamResource resource = new InputStreamResource(file.inputStream());
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(file.contentType()))
        .contentLength(file.size())
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + file.originalFileName() + "\"")
        .body(resource);
  }
}
