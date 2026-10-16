package com.johannesmager.pictorres.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("NFR-003")
class StorageKeyTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "originals/2026/10/0f8fad5b-d9cb-469f-a165-70867728950e.jpg",
        "thumbnails/0f8fad5b-d9cb-469f-a165-70867728950e.webp"
      })
  @DisplayName("NFR-003/AC-2 keys in the documented layout are accepted")
  void acceptsValidKeys(String value) {
    assertThat(new StorageKey(value).value()).isEqualTo(value);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "../etc/passwd",
        "/etc/passwd",
        "originals/../../etc/passwd",
        "originals/2026/10/../../../x.jpg",
        "thumbnails/holiday.jpg",
        "originals/2026/10/0f8fad5b-d9cb-469f-a165-70867728950e.jpg/..",
        "thumbnails\\0f8fad5b-d9cb-469f-a165-70867728950e.jpg",
        ""
      })
  @DisplayName("NFR-003/AC-3 keys that could escape the storage root are rejected")
  void rejectsPathTraversal(String value) {
    assertThatThrownBy(() -> new StorageKey(value)).isInstanceOf(IllegalArgumentException.class);
  }
}
