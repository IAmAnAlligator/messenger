package com.jeannimi.messenger.adapter.in.web.message.mapper;

import com.jeannimi.messenger.application.message.dto.FileDownloadResult;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class FileDownloadMapper {

  public ResponseEntity<InputStreamResource> toResponse(FileDownloadResult file) {
    InputStreamResource resource = new InputStreamResource(file.inputStream());

    ContentDisposition disposition =
        ContentDisposition.attachment().filename(file.fileName(), StandardCharsets.UTF_8).build();

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(file.contentType()))
        .contentLength(file.size())
        .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
        .cacheControl(CacheControl.noStore())
        .body(resource);
  }

  public ResponseEntity<Resource> toInlineResponse(FileDownloadResult result) {
    InputStreamResource resource = new InputStreamResource(result.inputStream());

    ContentDisposition disposition = ContentDisposition.inline().filename("thumbnail.jpg").build();

    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_JPEG)
        .contentLength(result.size())
        .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
        .cacheControl(CacheControl.noCache())
        .body(resource);
  }
}
