package org.zanata.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.zanata.page.WebDriverFactory;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class TakeScreenshotRule extends TestWatcher {

    private File dirForTest;

    @Override
    protected void starting(Description description) {
        String testDisplayName = description.getDisplayName();
        dirForTest =
            new ScreenshotDirForTest(testDisplayName).getDirForTest();
        WebDriverFactory.INSTANCE.updateListenerTestName(dirForTest);
    }

    @Override
    protected void succeeded(Description description) {
        try {
            FileUtils.deleteDirectory(dirForTest);
        }
        catch (IOException e) {
            log.warn("error deleting screenshot base directory: {}",
                e.getMessage());
        }
    }

    @Override
    protected void failed(Throwable e, Description description) {
        log.error("Test failed. Screenshot is stored at {}", dirForTest);
    }
}
