---
id: REQ-003
title: Browse the gallery
type: functional
status: draft
priority: must
milestone: M3
---

## User story
As a family member, I want to scroll through all photos as thumbnails, so that I can quickly find the photo I'm looking for.

## Acceptance criteria
- **AC-1** Given there are images, when I open the gallery, then I see a grid of thumbnails, sorted by capture date (falling back to upload time), newest first.
- **AC-2** Given more than 50 images, when I scroll to the end of the loaded thumbnails, then the next page of 50 loads automatically (infinite scroll), with no duplicates or gaps.
- **AC-3** Given there are no images, when I open the gallery, then I see an empty state with a call to action to upload photos.
- **AC-4** Given a phone-sized screen (360 px wide), then the grid adapts its number of columns, and there is no horizontal scrolling.
- **AC-5** Given the thumbnail grid, then thumbnails are lazy-loaded, and each has alt text (the original file name).
- **AC-6** Given the API, then `GET /api/images?page=&size=` returns a page of image metadata with thumbnail URLs, and the total count. `size` is capped at 100.
