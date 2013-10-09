package org.zanata.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class TestEventForScreenshotListener extends AbstractWebDriverEventListener {

    private WebDriver driver;
    private File dirForTest = null;

    /**
     * A registered TestEventListener will perform actions on navigate,
     * click and exception events
     * @param drv the WebDriver to derive screen shots from
     */
    public TestEventForScreenshotListener(WebDriver drv) {
        driver = drv;
    }

    /**
     * Update the screen shot directory for test ID.
     * @param dirForTest directory for a particular test
     */
    public void updateTestID(File dirForTest) {
        this.dirForTest = dirForTest;
    }

    private void createScreenshot(String ofType) {
        Preconditions.checkNotNull(dirForTest,
            "Have you set webdriver.screenshot.dir property?");
        try {
            dirForTest.mkdirs();
            File screenshotFile =
                    ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshotFile,
                new File(dirForTest, generateFileName(ofType)));

        } catch (WebDriverException wde) {
            throw new RuntimeException("[Screenshot]: Invalid WebDriver: "
                    + wde.getMessage());
        } catch (IOException ioe) {
            throw new RuntimeException("[Screenshot]: Failed to write", ioe);
        }
    }

    private String generateFileName(String ofType) {
        return dirForTest.getName()
            .concat(":").concat(String.valueOf(new Date().getTime()))
                .concat(ofType).concat(".png");
    }

    private boolean isAlertPresent(WebDriver driver) {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException nape) {
            return false;
        }
    }

    @Override
    public void afterNavigateTo(String url, WebDriver driver) {
        createScreenshot("_nav");
    }

    @Override
    public void beforeClickOn(WebElement element, WebDriver driver) {
        createScreenshot("_preclick");
    }

    @Override
    public void afterClickOn(WebElement element, WebDriver driver) {
        if (isAlertPresent(driver)) {
            log.info("[Screenshot]: Prevented by Alert");
            return;
        }
        createScreenshot("_click");
    }

    @Override
    public void onException(Throwable throwable, WebDriver driver) {
        createScreenshot("_exc");
    }

}
