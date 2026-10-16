package com.johannesmager.pictorres.image;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import java.util.TimeZone;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.coobird.thumbnailator.Thumbnails;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Reads image facts with ImageIO and metadata-extractor and creates thumbnails with Thumbnailator
 * (NFR-003 AC-4). Runs in JVM mode only (AWT/ImageIO).
 */
@ApplicationScoped
public class ImageProcessor {

  private static final String THUMBNAIL_FORMAT = "jpg";

  private final int thumbnailLongEdge;

  public ImageProcessor(@ConfigProperty(name = "pictorres.thumbnail.long-edge") int longEdge) {
    if (longEdge <= 0) {
      throw new IllegalArgumentException("Thumbnail long edge must be positive: " + longEdge);
    }
    this.thumbnailLongEdge = longEdge;
  }

  /** File extension of the thumbnails written by {@link #writeThumbnail}. */
  public String thumbnailExtension() {
    return THUMBNAIL_FORMAT;
  }

  /** Reads dimensions without decoding the full image, plus the EXIF capture date if present. */
  public ImageInfo readInfo(Path file) throws IOException, InvalidImageException {
    try (ImageInputStream in = ImageIO.createImageInputStream(file.toFile())) {
      Iterator<ImageReader> readers = in == null ? null : ImageIO.getImageReaders(in);
      if (readers == null || !readers.hasNext()) {
        throw new InvalidImageException("Unsupported or unreadable image: " + file.getFileName());
      }
      ImageReader reader = readers.next();
      try {
        reader.setInput(in, true, true);
        return new ImageInfo(reader.getWidth(0), reader.getHeight(0), readCaptureDate(file));
      } catch (IOException e) {
        throw new InvalidImageException("Corrupt image: " + file.getFileName(), e);
      } finally {
        reader.dispose();
      }
    }
  }

  /**
   * Writes a JPEG thumbnail whose longer edge is at most the configured size. The EXIF orientation
   * is applied so portrait photos are not shown rotated. Images smaller than the limit are not
   * enlarged.
   */
  public void writeThumbnail(Path source, OutputStream target)
      throws IOException, InvalidImageException {
    ImageInfo info = readInfo(source);
    int longEdge = Math.min(thumbnailLongEdge, Math.max(info.width(), info.height()));
    try {
      Thumbnails.of(source.toFile())
          .size(longEdge, longEdge)
          .useExifOrientation(true)
          .outputFormat(THUMBNAIL_FORMAT)
          .outputQuality(0.85)
          .toOutputStream(target);
    } catch (IllegalArgumentException | IOException e) {
      throw new InvalidImageException("Cannot create thumbnail: " + source.getFileName(), e);
    }
  }

  private static Optional<LocalDateTime> readCaptureDate(Path file) {
    try {
      Metadata metadata = ImageMetadataReader.readMetadata(file.toFile());
      ExifSubIFDDirectory exif = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
      if (exif == null) {
        return Optional.empty();
      }
      // EXIF dates carry no zone; read them as UTC to keep the camera's wall-clock time.
      Date date = exif.getDateOriginal(TimeZone.getTimeZone(ZoneOffset.UTC));
      return Optional.ofNullable(date)
          .map(d -> LocalDateTime.ofInstant(d.toInstant(), ZoneOffset.UTC));
    } catch (ImageProcessingException | IOException e) {
      // Missing or broken metadata must not prevent the upload; the upload time is used instead.
      return Optional.empty();
    }
  }
}
