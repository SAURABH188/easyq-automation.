package utils;

import base.BaseTest;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println();
        System.out.println("FAILED TEST: " + result.getMethod().getMethodName());
        if (result.getParameters() != null && result.getParameters().length > 0) {
            System.out.println("TEST DATA: " + Arrays.toString(result.getParameters()));
        }
        System.out.println("FAILURE REASON: " + result.getThrowable());

        WebDriver driver = getDriver(result);
        if (driver != null) {
            saveScreenshot(driver, result.getMethod().getMethodName());
        }
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("Starting suite: " + context.getName());
    }

    private WebDriver getDriver(ITestResult result) {
        Object testClass = result.getInstance();
        if (!(testClass instanceof BaseTest)) {
            return null;
        }

        try {
            Field driverField = BaseTest.class.getDeclaredField("driver");
            driverField.setAccessible(true);
            return (WebDriver) driverField.get(testClass);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            return null;
        }
    }

    private void saveScreenshot(WebDriver driver, String testName) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path screenshotDirectory = Path.of("test-output", "screenshots");
            Files.createDirectories(screenshotDirectory);
            Path target = screenshotDirectory.resolve(testName + ".png");
            Files.copy(screenshot.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("SCREENSHOT: " + target.toAbsolutePath());
        } catch (IOException | RuntimeException exception) {
            System.out.println("SCREENSHOT FAILED: " + exception.getMessage());
        }
    }
}
