package com.johannesmager.pictorres.storage;

import io.smallrye.config.ConfigMapping;
import java.nio.file.Path;

/** Storage settings (NFR-003 AC-2). */
@ConfigMapping(prefix = "pictorres.storage")
public interface StorageConfig {

  /** Directory below which all originals and thumbnails are stored. */
  Path root();
}
