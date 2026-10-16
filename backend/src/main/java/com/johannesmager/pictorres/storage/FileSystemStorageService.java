package com.johannesmager.pictorres.storage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Stores files on the local filesystem below {@code pictorres.storage.root} (NFR-003, ADR-0003).
 */
@ApplicationScoped
public class FileSystemStorageService implements StorageService {

  private static final Pattern EXTENSION = Pattern.compile("[a-z0-9]{1,5}");

  private final Path root;
  private final Clock clock;

  @Inject
  public FileSystemStorageService(StorageConfig config) {
    this(config.root(), Clock.systemUTC());
  }

  FileSystemStorageService(Path root, Clock clock) {
    this.root = root.toAbsolutePath().normalize();
    this.clock = clock;
  }

  @Override
  public StorageKey store(StorageArea area, String extension, InputStream content)
      throws IOException {
    if (extension == null || !EXTENSION.matcher(extension).matches()) {
      throw new IllegalArgumentException("Invalid file extension: " + extension);
    }
    StorageKey key = newKey(area, extension);
    Path target = resolve(key);
    Files.createDirectories(target.getParent());

    // Write to a temporary file in the same directory first, so readers never see partial files.
    Path temp = Files.createTempFile(target.getParent(), ".upload-", ".tmp");
    try {
      Files.copy(content, temp, StandardCopyOption.REPLACE_EXISTING);
      Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
    } finally {
      Files.deleteIfExists(temp);
    }
    return key;
  }

  @Override
  public InputStream open(StorageKey key) throws IOException {
    Path file = resolve(key);
    if (!Files.isRegularFile(file)) {
      throw new NoSuchFileException(key.value());
    }
    return Files.newInputStream(file);
  }

  @Override
  public boolean delete(StorageKey key) throws IOException {
    return Files.deleteIfExists(resolve(key));
  }

  private StorageKey newKey(StorageArea area, String extension) {
    String name = UUID.randomUUID() + "." + extension;
    return switch (area) {
      case ORIGINALS -> {
        ZonedDateTime now = ZonedDateTime.now(clock.withZone(ZoneOffset.UTC));
        yield new StorageKey(
            "%s/%04d/%02d/%s"
                .formatted(area.directory(), now.getYear(), now.getMonthValue(), name));
      }
      case THUMBNAILS -> new StorageKey(area.directory() + "/" + name);
    };
  }

  private Path resolve(StorageKey key) {
    Path file = root.resolve(key.value()).normalize();
    // Defence in depth: StorageKey already rejects anything that could escape the root.
    if (!file.startsWith(root)) {
      throw new IllegalArgumentException("Storage key escapes storage root: " + key);
    }
    return file;
  }
}
