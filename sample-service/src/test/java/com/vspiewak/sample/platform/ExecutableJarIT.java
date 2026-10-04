package com.vspiewak.sample.platform;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.junit.jupiter.api.Test;

/**
 * Proves the parent packages a service as an executable jar : the {@code *IT} lane runs after
 * {@code package}, so the jar under test is the one the build just produced — the one that ships.
 */
class ExecutableJarIT {

  @Test
  void shouldPackageTheServiceAsAnExecutableJar() throws IOException {
    // given
    try (var jar = new JarFile(builtJar().toFile())) {

      // when
      var manifest = jar.getManifest().getMainAttributes();

      // then
      assertThat(manifest.getValue("Main-Class"))
          .isEqualTo("org.springframework.boot.loader.launch.JarLauncher");
      assertThat(manifest.getValue("Start-Class"))
          .isEqualTo("com.vspiewak.sample.SampleServiceApplication");
      assertThat(jar.stream().map(entry -> entry.getName()))
          .anyMatch(name -> name.startsWith("BOOT-INF/lib/service-starter-"));
    }
  }

  private static Path builtJar() throws IOException {
    try (var files = Files.list(Path.of("target"))) {
      return files
          .filter(file -> file.getFileName().toString().matches("sample-service-.*\\.jar"))
          .findFirst()
          .orElseThrow();
    }
  }
}
