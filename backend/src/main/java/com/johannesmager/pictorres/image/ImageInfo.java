package com.johannesmager.pictorres.image;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Basic facts about an image file.
 *
 * @param captureDate EXIF {@code DateTimeOriginal}; local camera time without zone, if present
 */
public record ImageInfo(int width, int height, Optional<LocalDateTime> captureDate) {}
