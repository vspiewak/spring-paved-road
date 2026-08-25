package com.vspiewak.pavedroad.env;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

/** The banner every service on the paved road prints, without asking for it. */
class PlatformBannerTest {

  private static final String BANNER = "platform-banner.txt";

  @Test
  void bannerShipsWithTheStarter() {
    assertThat(new ClassPathResource(BANNER).exists()).isTrue();
  }

  @Test
  void bannerNamesTheServiceItBootsAndTheBootItRunsOn() throws Exception {
    var banner =
        new String(new ClassPathResource(BANNER).getContentAsByteArray(), StandardCharsets.UTF_8);

    // resolved by Boot against the Environment — spring.application.name is what
    // conventions-starter already forces every service to set
    assertThat(banner)
        .contains("${spring.application.name}")
        .contains("${spring-boot.version}")
        .contains("${java.version}");
  }

  @Test
  void bannerLinesStayInsideEightyColumns() throws Exception {
    var banner =
        new String(new ClassPathResource(BANNER).getContentAsByteArray(), StandardCharsets.UTF_8);

    for (String line : banner.lines().toList()) {
      // the ANSI placeholders are substituted away before anything reaches the terminal
      var printed = line.replaceAll("\\$\\{AnsiColor\\.[A-Z_]+}", "");
      assertThat(printed.length()).as("line '%s'", printed).isLessThanOrEqualTo(80);
    }
  }
}
