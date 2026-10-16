package com.johannesmager.pictorres.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("NFR-003")
class ImageProcessorTest {

  @TempDir Path dir;

  private final ImageProcessor processor = new ImageProcessor(400);

  @Test
  @DisplayName("NFR-003/AC-4 dimensions of a JPEG are read without EXIF capture date")
  void readsJpegInfo() throws Exception {
    Path jpeg = image("photo.jpg", "jpg", 1200, 800);

    ImageInfo info = processor.readInfo(jpeg);

    assertThat(info.width()).isEqualTo(1200);
    assertThat(info.height()).isEqualTo(800);
    assertThat(info.captureDate()).isEmpty();
  }

  @Test
  @DisplayName("NFR-003/AC-4 dimensions of a PNG are read")
  void readsPngInfo() throws Exception {
    ImageInfo info = processor.readInfo(image("drawing.png", "png", 300, 500));

    assertThat(info.width()).isEqualTo(300);
    assertThat(info.height()).isEqualTo(500);
  }

  @Test
  @DisplayName("NFR-003/AC-4 a landscape thumbnail is limited to the configured long edge")
  void landscapeThumbnail() throws Exception {
    BufferedImage thumbnail = thumbnailOf(image("wide.jpg", "jpg", 2000, 1000));

    assertThat(thumbnail.getWidth()).isEqualTo(400);
    assertThat(thumbnail.getHeight()).isEqualTo(200);
  }

  @Test
  @DisplayName("NFR-003/AC-4 a portrait thumbnail is limited to the configured long edge")
  void portraitThumbnail() throws Exception {
    BufferedImage thumbnail = thumbnailOf(image("tall.png", "png", 600, 1200));

    assertThat(thumbnail.getWidth()).isEqualTo(200);
    assertThat(thumbnail.getHeight()).isEqualTo(400);
  }

  @Test
  @DisplayName("NFR-003/AC-4 small images are not enlarged")
  void smallImageNotEnlarged() throws Exception {
    BufferedImage thumbnail = thumbnailOf(image("small.jpg", "jpg", 120, 80));

    assertThat(thumbnail.getWidth()).isEqualTo(120);
    assertThat(thumbnail.getHeight()).isEqualTo(80);
  }

  @Test
  @DisplayName("NFR-003/AC-4 files that are not images are rejected")
  void rejectsNonImages() throws IOException {
    Path text = Files.writeString(dir.resolve("notes.jpg"), "definitely not a picture");

    assertThatThrownBy(() -> processor.readInfo(text)).isInstanceOf(InvalidImageException.class);
    assertThatThrownBy(() -> processor.writeThumbnail(text, new ByteArrayOutputStream()))
        .isInstanceOf(InvalidImageException.class);
  }

  @Test
  @DisplayName("NFR-003/AC-4 truncated images are rejected")
  void rejectsTruncatedImage() throws Exception {
    Path jpeg = image("broken.jpg", "jpg", 800, 600);
    byte[] bytes = Files.readAllBytes(jpeg);
    Files.write(jpeg, java.util.Arrays.copyOf(bytes, 200));

    assertThatThrownBy(() -> processor.writeThumbnail(jpeg, new ByteArrayOutputStream()))
        .isInstanceOf(InvalidImageException.class);
  }

  @Test
  @DisplayName("NFR-003/AC-4 the thumbnail size must be positive")
  void rejectsInvalidConfiguration() {
    assertThatThrownBy(() -> new ImageProcessor(0)).isInstanceOf(IllegalArgumentException.class);
  }

  private BufferedImage thumbnailOf(Path source) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    processor.writeThumbnail(source, out);
    assertThat(processor.thumbnailExtension()).isEqualTo("jpg");
    return ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
  }

  private Path image(String name, String format, int width, int height) throws IOException {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();
    g.setColor(Color.ORANGE);
    g.fillRect(0, 0, width, height);
    g.setColor(Color.BLUE);
    g.fillOval(width / 4, height / 4, width / 2, height / 2);
    g.dispose();
    Path file = dir.resolve(name);
    assertThat(ImageIO.write(image, format, file.toFile())).isTrue();
    return file;
  }
}
