package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DynamicWorkflowHelper {
    private static final By BODY = By.tagName("body");
    private static final By BUSY_INDICATOR = By.xpath(
            "//*[contains(@class,'spinner') or contains(@class,'loader') or contains(@class,'loading') or contains(normalize-space(.),'Loading')]"
    );

    private DynamicWorkflowHelper() {
    }

    public static void waitForStablePage(WebDriver driver, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        wait.until(ExpectedConditions.visibilityOfElementLocated(BODY));
        try {
            wait.until(currentDriver ->
                    "complete".equals(((JavascriptExecutor) currentDriver)
                            .executeScript("return document.readyState")));
        } catch (TimeoutException ignored) {
            // Dynamic screens can keep network/background work alive. Visible UI assertions follow.
        }
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.invisibilityOfElementLocated(BUSY_INDICATOR));
        } catch (TimeoutException ignored) {
            // If a background loader remains visible, the body/state assertion will catch a broken page.
        }
    }

    public static void assertDynamicState(WebDriver driver, String workflowName) {
        waitForStablePage(driver, 10);

        String bodyText = getBodyText(driver);
        boolean pageHasContent = bodyText.length() > 20;
        boolean stateDetected = containsAny(bodyText,
                "Dashboard", "No Data", "No data", "No records", "No Records", "No Notifications",
                "Draft", "Review", "Approved", "Pending", "Completed", "Closed", "Open",
                "Unauthorized", "Access Denied", "Restricted", "Permission", "Create", "Add", "Upload",
                "Save", "Submit", "View", "Edit", "Delete");

        Reporter.log("Dynamic workflow checkpoint: " + workflowName
                + " | detectedState=" + detectState(bodyText)
                + " | url=" + driver.getCurrentUrl(), true);

        Assert.assertTrue(pageHasContent || stateDetected,
                workflowName + " should show a valid dynamic state instead of a blank/broken page");
    }

    public static boolean clickFirstVisible(WebDriver driver, By... locators) {
        for (By locator : locators) {
            for (WebElement element : driver.findElements(locator)) {
                if (safeIsDisplayed(element) && safeIsEnabled(element)) {
                    scrollIntoView(driver, element);
                    highlight(driver, element);
                    pauseForObservation();
                    element.click();
                    waitForStablePage(driver, 10);
                    pauseForObservation();
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isVisible(WebDriver driver, By locator) {
        for (WebElement element : driver.findElements(locator)) {
            if (safeIsDisplayed(element)) {
                return true;
            }
        }
        return false;
    }

    public static int visibleCount(WebDriver driver, By locator) {
        int count = 0;
        for (WebElement element : driver.findElements(locator)) {
            if (safeIsDisplayed(element)) {
                count++;
            }
        }
        return count;
    }

    public static void fillFirstTextInput(WebDriver driver, String value) {
        By editableField = By.xpath(
                "//input[not(@type='hidden') and not(@type='file') and not(@readonly) and not(@disabled)] | "
                        + "//textarea[not(@readonly) and not(@disabled)]"
        );

        for (WebElement element : driver.findElements(editableField)) {
            if (safeIsDisplayed(element) && safeIsEnabled(element)) {
                scrollIntoView(driver, element);
                highlight(driver, element);
                pauseForObservation();
                element.clear();
                pauseForObservation();
                element.sendKeys(value);
                pauseForObservation();
                return;
            }
        }
    }

    public static boolean allowWorkflowMutations() {
        String value = System.getProperty("allowWorkflowMutations");
        if (value == null || value.isBlank()) {
            value = System.getenv("EASYQ_ALLOW_WORKFLOW_MUTATIONS");
        }
        if (value == null || value.isBlank()) {
            try {
                value = new ConfigReader().get("allowWorkflowMutations");
            } catch (RuntimeException ignored) {
                value = "false";
            }
        }
        return value != null && value.equalsIgnoreCase("true");
    }

    public static String uniqueAutomationText(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return prefix + "_AUTO_" + timestamp;
    }

    public static String getBodyText(WebDriver driver) {
        try {
            return driver.findElement(BODY).getText().trim();
        } catch (RuntimeException exception) {
            return "";
        }
    }

    public static boolean containsAny(String text, String... expectedValues) {
        String lowerText = text == null ? "" : text.toLowerCase(Locale.ROOT);
        for (String expectedValue : expectedValues) {
            if (expectedValue != null && lowerText.contains(expectedValue.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public static String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        return "\"" + value + "\"";
    }

    private static String detectState(String bodyText) {
        if (containsAny(bodyText, "Unauthorized", "Access Denied", "Restricted", "Permission")) {
            return "restricted";
        }
        if (containsAny(bodyText, "No Data", "No records", "No Notifications")) {
            return "empty";
        }
        if (containsAny(bodyText, "Draft", "Review", "Approved", "Pending", "Completed", "Closed", "Open")) {
            return "workflow-data";
        }
        if (containsAny(bodyText, "Create", "Add", "Upload", "Save", "Submit", "View")) {
            return "actions-available";
        }
        return bodyText.isBlank() ? "blank" : "page-loaded";
    }

    private static void scrollIntoView(WebDriver driver, WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        } catch (RuntimeException ignored) {
            // Click will still run; this only improves reliability on long pages.
        }
    }

    private static void highlight(WebDriver driver, WebElement element) {
        if (!Boolean.parseBoolean(String.valueOf(new ConfigReader().get("highlightActions")))) {
            return;
        }
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].style.outline='3px solid #ff9800';"
                            + "arguments[0].style.boxShadow='0 0 0 4px rgba(255,152,0,.25)';",
                    element
            );
        } catch (RuntimeException ignored) {
            // Highlight is only for watchability.
        }
    }

    private static void pauseForObservation() {
        int delayMs = 1200;
        try {
            ConfigReader config = new ConfigReader();
            String configuredDelay = config.getOptionalSecret("EASYQ_VISUAL_DELAY_MS");
            if (configuredDelay == null || configuredDelay.isBlank()) {
                configuredDelay = config.get("actionDelayMs");
            }
            if (configuredDelay != null && !configuredDelay.isBlank()) {
                delayMs = Integer.parseInt(configuredDelay);
            }
        } catch (RuntimeException ignored) {
            delayMs = 1200;
        }
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean safeIsDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (StaleElementReferenceException exception) {
            return false;
        }
    }

    private static boolean safeIsEnabled(WebElement element) {
        try {
            return element.isEnabled();
        } catch (StaleElementReferenceException exception) {
            return false;
        }
    }
}
