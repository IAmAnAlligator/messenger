package com.jeannimi.messenger.adapter.out.imagethumbnail;

import com.jeannimi.messenger.application.port.out.GeneratedThumbnail;
import com.jeannimi.messenger.application.port.out.ImageThumbnailPort;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

@Component
public class ThumbnailatorAdapter implements ImageThumbnailPort {

  private static final int THUMBNAIL_WIDTH = 320;
  private static final int THUMBNAIL_HEIGHT = 320;

  private static final double OUTPUT_QUALITY = 0.6;

  private static final long MAX_IMAGE_PIXELS = 25_000_000L;

  @Override
  public GeneratedThumbnail generate(InputStream original) {

    try {
      byte[] originalBytes = original.readAllBytes();

      try (ImageInputStream imageInput = ImageIO.createImageInputStream(
          new ByteArrayInputStream(originalBytes))) {

        if (imageInput == null) {
          throw new ThumbnailGenerationException(
              "Unable to read uploaded image");
        }

        Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);

        if (!readers.hasNext()) {
          throw new ThumbnailGenerationException(
              "Uploaded file is not a valid image");
        }

        ImageReader reader = readers.next();

        try {
          reader.setInput(imageInput, true, true);

          String format = reader.getFormatName().toLowerCase(Locale.ROOT);

          String originalContentType = switch (format) {
            case "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            default -> throw new ThumbnailGenerationException(
                "Only JPEG and PNG images are supported");
          };

          int width = reader.getWidth(0);
          int height = reader.getHeight(0);

          if (width <= 0 || height <= 0) {
            throw new ThumbnailGenerationException(
                "Invalid image dimensions");
          }

          long pixels = (long) width * height;

          if (pixels > MAX_IMAGE_PIXELS) {
            throw new ThumbnailGenerationException(
                "Image dimensions exceed the allowed limit");
          }

          // Маленькие изображения сохраняем без перекодирования.
          if (width <= THUMBNAIL_WIDTH
              && height <= THUMBNAIL_HEIGHT) {

            return new GeneratedThumbnail(
                originalBytes,
                originalContentType);
          }

          BufferedImage image = reader.read(0);

          if (image == null) {
            throw new ThumbnailGenerationException(
                "Failed to decode uploaded image");
          }

          // Большие изображения уменьшаем и сохраняем в JPEG.
          try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Thumbnails.of(image)
                .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                .outputFormat("jpg")
                .outputQuality(OUTPUT_QUALITY)
                .toOutputStream(output);

            return new GeneratedThumbnail(
                output.toByteArray(),
                "image/jpeg");
          }

        } finally {
          reader.dispose();
        }
      }

    } catch (IOException e) {
      throw new ThumbnailGenerationException(
          "Failed to generate image thumbnail", e);
    }
  }
}