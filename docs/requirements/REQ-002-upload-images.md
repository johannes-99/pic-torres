---
id: REQ-002
title: Upload images
type: functional
status: approved
priority: must
milestone: M2
---

## User story
As a family member, I want to upload several photos at once, so that the whole family can see them in the gallery.

## Acceptance criteria
- **AC-1** Given I am signed in, when I open the upload dialog, then I can select multiple files with a file picker, or drop them onto a drop zone.
- **AC-2** Given I upload JPEG, PNG or WebP files, when the upload completes, then every file is stored, and appears in the gallery without a page reload.
- **AC-3** Given a file of another type (checked by its content, not only its extension), or a file larger than `pictorres.upload.max-size` (default 50 MB), when I upload it, then that file is rejected with a clear message naming the file, while the other files in the same batch are still uploaded.
- **AC-4** Given an upload in progress, then I see per-file progress, and a final summary (succeeded / failed).
- **AC-5** Given an uploaded image, then a thumbnail is generated: 400 px on the long edge, with the EXIF orientation applied, so portrait photos are not rotated.
- **AC-6** Given an uploaded image, then these metadata are stored:
  - the original file name
  - the content type
  - the size
  - the width and height
  - the uploader
  - the upload time
  - the capture date from EXIF `DateTimeOriginal`, when present
- **AC-7** Given a corrupt image file with a valid type, when I upload it, then it is rejected with a message, and no partial files or database rows remain.
- **AC-8** Given an upload request without a valid CSRF token or session, then it is rejected (see NFR-002).

## Notes
- API: `POST /api/images` (multipart), with one result per file in the response.
- Storage: NFR-003.
- HEIC support is deferred to the backlog.
