package com.johannesmager.pictorres;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Tag;

/**
 * NFR-004/AC-4 layering: resource (api) → service → persistence / storage / image.
 *
 * <ul>
 *   <li>{@code ..api..} — REST resources and DTOs
 *   <li>{@code ..service..} — use cases and transactions
 *   <li>{@code ..persistence..} — entities and Panache repositories
 *   <li>{@code ..storage..}, {@code ..image..} — file storage and image processing
 * </ul>
 */
@Tag("NFR-004")
@AnalyzeClasses(
    packages = "com.johannesmager.pictorres",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  @ArchTest
  static final ArchRule layers_are_respected =
      layeredArchitecture()
          .consideringOnlyDependenciesInLayers()
          .withOptionalLayers(true)
          .layer("Api")
          .definedBy("..api..")
          .layer("Service")
          .definedBy("..service..")
          .layer("Persistence")
          .definedBy("..persistence..")
          .layer("Storage")
          .definedBy("..storage..")
          .layer("Image")
          .definedBy("..image..")
          .whereLayer("Api")
          .mayNotBeAccessedByAnyLayer()
          .whereLayer("Service")
          .mayOnlyBeAccessedByLayers("Api")
          .whereLayer("Persistence")
          .mayOnlyBeAccessedByLayers("Service")
          .whereLayer("Storage")
          .mayOnlyBeAccessedByLayers("Service")
          .whereLayer("Image")
          .mayOnlyBeAccessedByLayers("Service")
          .as(
              "NFR-004/AC-4 resources never access repositories, storage or image processing directly");

  @ArchTest
  static final ArchRule no_cycles =
      slices()
          .matching("com.johannesmager.pictorres.(*)..")
          .should()
          .beFreeOfCycles()
          .as("NFR-004/AC-4 packages are free of cycles");
}
