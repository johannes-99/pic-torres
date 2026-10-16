# ADR-0003: Local filesystem for image files, PostgreSQL for metadata

- **Status:** accepted
- **Date:** 2026-10-16

## Context
The gallery is self-hosted for one family. The data volume is moderate: thousands to tens of thousands of photos.

## Decision
- Original files and thumbnails are stored on the local filesystem under a configurable root, which is a container volume in production.
- Metadata is stored in PostgreSQL.
- All file access goes through a `StorageService` interface.
- File names are server-generated UUIDs.

## Consequences
- **Positive:**
  - Simple, with no extra service.
  - Easy to back up with standard tools.
  - Files are easy to inspect.
- **Negative:**
  - Not horizontally scalable.
  - Backups must cover two places: the volume and the database.
- **Migration path:** an S3/MinIO implementation of `StorageService` can be added later without changing callers.
- **Considered alternatives:**
  - S3-compatible storage (MinIO): an extra service, unnecessary for this size.
  - BLOBs in the database: makes the database large and backups slow.
