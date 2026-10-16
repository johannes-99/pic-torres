---
id: REQ-004
title: View a single image
type: functional
status: draft
priority: must
milestone: M3
---

## User story
As a family member, I want to view a photo in full size and flip through the gallery, so that I can enjoy the photos and save one when I want to.

## Acceptance criteria
- **AC-1** Given the gallery, when I click a thumbnail, then a viewer opens. It shows the image scaled to fit the screen, along with its file name, capture date and uploader.
- **AC-2** Given the viewer is open, when I press the left or right arrow key, or click the previous/next buttons, then the previous or next image (in gallery order) is shown. The buttons are disabled at the start and end.
- **AC-3** Given the viewer is open, when I press Esc or click close, then the viewer closes, and the gallery keeps its scroll position.
- **AC-4** Given the viewer, then it has its own URL (e.g. `/images/<id>`), which can be opened directly and supports the browser's back button.
- **AC-5** Given the viewer, when I click "Download", then the original file is downloaded with its original file name.
- **AC-6** Given image and thumbnail responses, then they are served only to authenticated members, with the correct `Content-Type` and with `Cache-Control: private, max-age=31536000, immutable`. Image IDs are not guessable (UUIDs).
- **AC-7** Given an image ID that does not exist, when it is requested, then the API returns 404 and the UI shows a "not found" message.
