package com.johannesmager.pictorres.image;

/** Thrown when a file cannot be decoded as a supported image. */
public class InvalidImageException extends Exception {

  public InvalidImageException(String message) {
    super(message);
  }

  public InvalidImageException(String message, Throwable cause) {
    super(message, cause);
  }
}
