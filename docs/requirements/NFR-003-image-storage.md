---
id: NFR-003
title: Image storage and processing
type: non-functional
status: draft
priority: must
milestone: M0
---

## Rationale
Image files are stored on the local filesystem; metadata is stored in PostgreSQL. Storage sits behind an interface so a different backend (e.g. S3) can be added later without changing callers. See ADR-0003.

## Acceptance criteria
- **AC-1** Given the backend, then all file access goes through a CDI interface `StorageService` (store, open, delete). The only implementation in v1 is a filesystem implementation.
- **AC-2** Given the configuration property `pictorres.storage.root`, then all files are stored below that directory, in this layout:
  - `originals/<yyyy>/<mm>/<uuid>.<ext>`
  - `thumbnails/<uuid>.webp` or `thumbnails/<uuid>.jpg`
- **AC-3** Given a stored file, then its name is a server-generated UUID. The original file name is kept only as metadata in the database, and user input never forms a filesystem path, which prevents path traversal.
- **AC-4** Given image processing, then thumbnails are created with Thumbnailator, and EXIF data is read with metadata-extractor.
- **AC-5** Given tests, then storage uses a temporary directory per test run. No test writes outside that directory.
- **AC-6** Given the production deployment, then the storage root is a Docker volume, and a documented backup procedure covers both the volume and the database (see NFR-006).

## Notes
- JVM mode only, because AWT/ImageIO is unreliable in native images.
