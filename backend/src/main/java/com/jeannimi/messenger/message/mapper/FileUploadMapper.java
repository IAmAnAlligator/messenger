package com.jeannimi.messenger.message.mapper;

import com.jeannimi.messenger.common.exception_handling.MessageError;
import com.jeannimi.messenger.common.exception_handling.MessageException;
import com.jeannimi.messenger.message.dto.FileUpload;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUploadMapper {

  public FileUpload toFileUpload(MultipartFile file) {

    try {
      return new FileUpload(
          file.getInputStream(), file.getOriginalFilename(), file.getContentType(), file.getSize());

    } catch (IOException e) {
      throw new MessageException(MessageError.FILE_STORAGE_FAILED, "Failed to read uploaded file");
    }
  }
}
