package com.jeannimi.messenger.application.port.out;

public record GeneratedThumbnail(
    byte[] content,
    String contentType
) {
  public String extension() {
    return switch (contentType) {
      case "image/png" -> ".png";
      case "image/jpeg" -> ".jpg";
      default -> throw new IllegalStateException(
          "Unsupported thumbnail content type: " + contentType);
    };
  }
}