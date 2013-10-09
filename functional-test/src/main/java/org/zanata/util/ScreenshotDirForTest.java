package org.zanata.util;

import lombok.Getter;

import java.io.File;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ScreenshotDirForTest {
    private static final File BASE_DIR =
        new File(PropertiesHolder.getProperty("webdriver.screenshot.dir"));

    @Getter
    private final File dirForTest;

    public ScreenshotDirForTest(String testId) {
        dirForTest = new File(BASE_DIR, testId);
    }

    public static boolean screenshotEnabled() {
        return PropertiesHolder.getProperty("webdriver.screenshot.dir") != null;
    }
}
