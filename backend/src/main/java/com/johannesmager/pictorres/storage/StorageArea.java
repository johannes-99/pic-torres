package com.johannesmager.pictorres.storage;

/** Top-level areas below the storage root. */
public enum StorageArea {
  /**
   * Uploaded files as received, grouped by year and month: {@code originals/yyyy/mm/<uuid>.<ext>}.
   */
  ORIGINALS("originals"),
  /** Generated previews: {@code thumbnails/<uuid>.<ext>}. */
  THUMBNAILS("thumbnails");

  private final String directory;

  StorageArea(String directory) {
    this.directory = directory;
  }

  public String directory() {
    return directory;
  }
}
