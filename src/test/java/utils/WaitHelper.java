package utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitHelper {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final int settleDelayMs;

    public WaitHelper(WebDriver driver, int timeoutSeconds) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        ConfigReader config = new ConfigReader();
        this.settleDelayMs = parseInt(config.getOptionalSecret("EASYQ_PAGE_SETTLE_MS"), parseInt(config.get("pageSettleMs"), 1000));
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
        waitForVisibleLoadersToFinish();
        waitForDomToSettle();
    }

    private void waitForVisibleLoadersToFinish() {
        try {
            wait.until(currentDriver -> {
                JavascriptExecutor js = (JavascriptExecutor) currentDriver;
                Object hasVisibleLoader = js.executeScript(
                        "const selectors = ["
                                + "'.loader','.loading','.spinner','.ngx-spinner-overlay','.mat-progress-spinner',"
                                + "'.mat-mdc-progress-spinner','.progress-bar','[role=\"progressbar\"]',"
                                + "'[aria-busy=\"true\"]','.cdk-overlay-backdrop.cdk-overlay-backdrop-showing'"
                                + "];"
                                + "const visible = el => {"
                                + "  const style = window.getComputedStyle(el);"
                                + "  const rect = el.getBoundingClientRect();"
                                + "  return style.display !== 'none' && style.visibility !== 'hidden' && style.opacity !== '0'"
                                + "    && rect.width > 0 && rect.height > 0;"
                                + "};"
                                + "return selectors.some(selector => Array.from(document.querySelectorAll(selector)).some(visible));"
                );
                return !Boolean.TRUE.equals(hasVisibleLoader);
            });
        } catch (RuntimeException ignored) {
            // Some screens keep a small loader label while content is usable; element-level waits still decide readiness.
        }
    }

    private void waitForDomToSettle() {
        if (settleDelayMs <= 0) {
            return;
        }

        try {
            Object firstSnapshot = snapshotDom();
            sleep(settleDelayMs);
            Object secondSnapshot = snapshotDom();

            if (!String.valueOf(firstSnapshot).equals(String.valueOf(secondSnapshot))) {
                sleep(settleDelayMs);
            }
        } catch (RuntimeException ignored) {
            // Dynamic pages may keep polling; the fixed settle delay still gives the UI time to render.
        }
    }

    private Object snapshotDom() {
        return ((JavascriptExecutor) driver).executeScript(
                "return [document.body ? document.body.innerText.length : 0,"
                        + "document.querySelectorAll('*').length,"
                        + "window.location.href].join('|');"
        );
    }

    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (RuntimeException exception) {
            return fallback;
        }
    }
}
