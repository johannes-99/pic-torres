package com.johannesmager.pictorres.storage;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("NFR-003")
class StorageServiceQuarkusTest {

  @Inject StorageService storage;
  @Inject StorageConfig config;

  @Test
  @DisplayName("NFR-003/AC-1 the application uses the filesystem implementation")
  void injectsFileSystemImplementation() {
    assertThat(storage).isInstanceOf(FileSystemStorageService.class);
  }

  @Test
  @DisplayName("NFR-003/AC-5 tests store files in a per-run temporary directory")
  void storesBelowTemporaryRoot() throws IOException {
    Path tmp = Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().normalize();
    assertThat(config.root().toAbsolutePath().normalize()).startsWithRaw(tmp);
    assertThat(config.root().getFileName().toString()).startsWith("pic-torres-test-");

    StorageKey key =
        storage.store(StorageArea.THUMBNAILS, "jpg", new ByteArrayInputStream(new byte[] {1, 2}));

    assertThat(Files.exists(config.root().resolve(key.value()))).isTrue();
    storage.delete(key);
  }
}
