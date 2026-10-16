---
id: REQ-101
title: Delete images
type: functional
status: approved
priority: should
milestone: backlog
---

## User story
As a family member, I want to delete photos I uploaded, so that I can remove mistakes and duplicates.

## Acceptance criteria (draft)
- **AC-1** Given an image I uploaded, when I delete it and confirm, then the image, its thumbnail and its metadata are removed.
- **AC-2** Given an image someone else uploaded, then I cannot delete it (403), unless I am an administrator.
- **AC-3** Given a deleted image, then its URLs return 404.

## Open questions
- Should deleted images go to a trash with a retention period, or be deleted permanently?
