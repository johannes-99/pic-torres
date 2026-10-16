package com.johannesmager.pictorres.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Access to image files (NFR-003 AC-1). All file I/O in the application goes through this interface
 * so the backend can be swapped (e.g. for S3) without touching callers (ADR-0003).
 */
public interface StorageService {

  /**
   * Stores the content under a new server-generated name.
   *
   * @param extension lower-case file extension without dot, e.g. {@code jpg}
   * @return the key under which the content can be opened later
   */
  StorageKey store(StorageArea area, String extension, InputStream content) throws IOException;

  /** Opens a stored file for reading; the caller closes the stream. */
  InputStream open(StorageKey key) throws IOException;

  /**
   * Deletes a stored file.
   *
   * @return {@code true} if a file was deleted, {@code false} if it did not exist
   */
  boolean delete(StorageKey key) throws IOException;
}
