package utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitHelper {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public WaitHelper(WebDriver driver, int timeoutSeconds) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    public void waitForPageReady() {
        wait.until((ExpectedCondition<Boolean>) currentDriver ->
                "complete".equals(((JavascriptExecutor) currentDriver)
                        .executeScript("return document.readyState")));
    }

    public void waitForAngularToSettle() {
        try {
            wait.until(currentDriver -> {
                JavascriptExecutor js = (JavascriptExecutor) currentDriver;
                Object hasAngular = js.executeScript("return !!window.getAllAngularTestabilities");
                if (!Boolean.TRUE.equals(hasAngular)) {
                    return true;
                }
                Object isStable = js.executeScript(
                        "return window.getAllAngularTestabilities().every(function(testability) { return testability.isStable(); });"
                );
                return Boolean.TRUE.equals(isStable);
            });
        } catch (RuntimeException ignored) {
            // Some apps keep background polling active; visible page assertions handle readiness.
        }
    }

    public void waitForAppToLoad() {
        waitForPageReady();
        waitForAngularToSettle();
    }
}
