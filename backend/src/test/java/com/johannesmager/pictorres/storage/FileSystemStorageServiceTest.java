package com.johannesmager.pictorres.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("NFR-003")
class FileSystemStorageServiceTest {

  private static final Clock OCTOBER_2026 =
      Clock.fixed(Instant.parse("2026-10-16T12:00:00Z"), ZoneOffset.UTC);

  @TempDir Path root;

  private FileSystemStorageService storage;

  @BeforeEach
  void setUp() {
    storage = new FileSystemStorageService(root, OCTOBER_2026);
  }

  @Test
  @DisplayName("NFR-003/AC-2 originals are stored under originals/<yyyy>/<mm>/<uuid>.<ext>")
  void storesOriginalsByYearAndMonth() throws IOException {
    StorageKey key = storage.store(StorageArea.ORIGINALS, "jpg", content("photo"));

    assertThat(key.value()).matches("originals/2026/10/[0-9a-f-]{36}\\.jpg");
    assertThat(root.resolve(key.value())).hasContent("photo");
  }

  @Test
  @DisplayName("NFR-003/AC-2 thumbnails are stored under thumbnails/<uuid>.<ext>")
  void storesThumbnailsFlat() throws IOException {
    StorageKey key = storage.store(StorageArea.THUMBNAILS, "jpg", content("thumb"));

    assertThat(key.value()).matches("thumbnails/[0-9a-f-]{36}\\.jpg");
    assertThat(root.resolve(key.value())).hasContent("thumb");
  }

  @Test
  @DisplayName("NFR-003/AC-3 every stored file gets a new server-generated name")
  void generatesUniqueNames() throws IOException {
    StorageKey first = storage.store(StorageArea.ORIGINALS, "png", content("a"));
    StorageKey second = storage.store(StorageArea.ORIGINALS, "png", content("a"));

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  @DisplayName("NFR-003/AC-3 extensions that could form a path are rejected")
  void rejectsUnsafeExtensions() {
    for (String extension : new String[] {"../x", "JPG", "", "jpg/..", "toolong", null}) {
      assertThatThrownBy(() -> storage.store(StorageArea.ORIGINALS, extension, content("x")))
          .as("extension %s", extension)
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Test
  @DisplayName("NFR-003/AC-1 stored content can be opened and deleted again")
  void opensAndDeletes() throws IOException {
    StorageKey key = storage.store(StorageArea.ORIGINALS, "webp", content("data"));

    try (InputStream in = storage.open(key)) {
      assertThat(new String(in.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("data");
    }
    assertThat(storage.delete(key)).isTrue();
    assertThat(storage.delete(key)).isFalse();
    assertThatThrownBy(() -> storage.open(key)).isInstanceOf(NoSuchFileException.class);
  }

  @Test
  @DisplayName("NFR-003/AC-5 no temporary files remain after storing")
  void leavesNoTemporaryFiles() throws IOException {
    storage.store(StorageArea.ORIGINALS, "jpg", content("photo"));

    try (Stream<Path> files = Files.walk(root)) {
      assertThat(files.filter(Files::isRegularFile))
          .singleElement()
          .satisfies(file -> assertThat(file.getFileName().toString()).endsWith(".jpg"));
    }
  }

  @Test
  @DisplayName("NFR-003/AC-5 a failing upload stream leaves no files behind")
  void cleansUpOnFailure() throws IOException {
    InputStream failing =
        new InputStream() {
          @Override
          public int read() throws IOException {
            throw new IOException("connection reset");
          }
        };

    assertThatThrownBy(() -> storage.store(StorageArea.ORIGINALS, "jpg", failing))
        .isInstanceOf(IOException.class);
    try (Stream<Path> files = Files.walk(root)) {
      assertThat(files.filter(Files::isRegularFile)).isEmpty();
    }
  }

  private static InputStream content(String text) {
    return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
  }
}
