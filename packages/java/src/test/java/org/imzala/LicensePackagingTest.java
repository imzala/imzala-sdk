package org.imzala;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The published Developer and API Terms (§6) state that the full licence text
 * is contained in every package. For the jar that means META-INF/LICENSE and
 * META-INF/NOTICE must be on the artifact classpath, not merely declared as
 * POM {@code <licenses>} metadata. Guards pom.xml's {@code <resources>} block.
 */
class LicensePackagingTest {

  private String readClasspath(String path) throws Exception {
    try (InputStream in = LicensePackagingTest.class.getResourceAsStream(path)) {
      assertNotNull(in, path + " is missing from the artifact classpath");
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  @Test
  void licenseTextIsPackagedInTheArtifact() throws Exception {
    String license = readClasspath("/META-INF/LICENSE");
    assertTrue(license.length() > 500, "META-INF/LICENSE looks truncated");
    assertTrue(license.toLowerCase().contains("license"), "META-INF/LICENSE does not read as a licence");
  }

  @Test
  void noticeTextIsPackagedInTheArtifact() throws Exception {
    String notice = readClasspath("/META-INF/NOTICE");
    assertTrue(notice.length() > 100, "META-INF/NOTICE looks truncated");
  }
}
