package com.jeannimi.messenger.message.mapper;

import com.jeannimi.messenger.application.message.dto.FileDownloadResult;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class FileDownloadMapper {

  public ResponseEntity<InputStreamResource> toResponse(FileDownloadResult file) {

//    InputStreamResource resource = new InputStreamResource(file.inputStream());
//
//    ContentDisposition contentDisposition =
//        ContentDisposition.inline().filename(file.fileName(), StandardCharsets.UTF_8).build();
//
//    return ResponseEntity.ok()
//        .contentType(MediaType.parseMediaType(file.contentType()))
//        .contentLength(file.size())
//        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
//        .body(resource);

    InputStreamResource resource = new InputStreamResource(file.inputStream());
    return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.contentType()))
        .contentLength(file.size()).header( HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + file.originalFileName() + "\"")
        .body(resource);
  }
}
