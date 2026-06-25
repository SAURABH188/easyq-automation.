package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ActionHelper {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final WaitHelper waitHelper;
    private final int actionDelayMs;
    private final boolean highlightActions;

    public ActionHelper(WebDriver driver, int timeoutSeconds) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        this.waitHelper = new WaitHelper(driver, timeoutSeconds);

        ConfigReader config = new ConfigReader();
        String visualDelay = config.getOptionalSecret("EASYQ_VISUAL_DELAY_MS");
        this.actionDelayMs = parseInt(visualDelay != null ? visualDelay : config.get("actionDelayMs"), 1200);
        this.highlightActions = Boolean.parseBoolean(String.valueOf(config.get("highlightActions")));
    }

    public WebElement waitForVisible(By locator) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        focus(element);
        return element;
    }

    public WebElement waitForClickable(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        focus(element);
        return element;
    }

    public void click(By locator) {
        WebElement element = waitForClickable(locator);
        element.click();
        afterAction();
    }

    public void type(By locator, String value) {
        WebElement element = waitForVisible(locator);
        element.sendKeys(value);
        afterAction();
    }

    public void clearAndType(By locator, String value) {
        WebElement element = waitForVisible(locator);
        element.clear();
        element.sendKeys(value);
        afterAction();
    }

    public void pressEnter(By locator) {
        WebElement element = waitForVisible(locator);
        element.sendKeys(Keys.ENTER);
        afterAction();
    }

    public void pressTab(By locator) {
        WebElement element = waitForVisible(locator);
        element.sendKeys(Keys.TAB);
        afterAction();
    }

    public boolean isVisible(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public void waitAfterAction() {
        afterAction();
    }

    private void afterAction() {
        try {
            waitHelper.waitForAppToLoad();
        } catch (RuntimeException ignored) {
            // Visible element assertions after each action decide whether the page is ready enough.
        }
        pauseForObservation();
    }

    private void focus(WebElement element) {
        if (!highlightActions) {
            return;
        }
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});"
                            + "arguments[0].setAttribute('data-easyq-old-style', arguments[0].getAttribute('style') || '');"
                            + "arguments[0].style.outline='3px solid #ff9800';"
                            + "arguments[0].style.boxShadow='0 0 0 4px rgba(255,152,0,.25)';",
                    element
            );
        } catch (StaleElementReferenceException ignored) {
            // Element changed between wait and highlight; the action will re-check when needed.
        } catch (RuntimeException ignored) {
            // Highlight is only for watchability.
        }
    }

    private void pauseForObservation() {
        if (actionDelayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(actionDelayMs);
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
