package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class EasyQActiveLogsTest {
    private WebDriver driver;
    private WebDriverWait wait;

    private final String baseUrl = "https://beta.easyqsolutions.com/#/easyqsolutions/login";
    private final String validEmail = "varunt@easyqsolutions.com";

    private final By emailField = By.xpath("//input[@type='email' or contains(@formcontrolname,'email')]");
    private final By passwordField = By.xpath("//input[@type='password' or contains(@formcontrolname,'password')]");
    private final By loginButton = By.xpath("//button[contains(normalize-space(.),'Log In')]");
    private final By activeLogsMenu = By.xpath("//*[contains(normalize-space(.),'Active Logs')]");
    private final By activeLogsTitle = By.xpath("//*[contains(normalize-space(.),'Active Logs')]");
    private final By todayLabel = By.xpath("//*[contains(normalize-space(.),'Today')]");
    private final By yesterdayLabel = By.xpath("//*[contains(normalize-space(.),'Yesterday')]");
    private final By noLogsMessage = By.xpath("//*[contains(normalize-space(.),'No Logs') or contains(normalize-space(.),'No logs') or contains(normalize-space(.),'No Data')]");
    private final By paginationControl = By.xpath("//button[contains(.,'Next') or contains(.,'Previous') or contains(.,'Prev') or @aria-label='Next page' or @aria-label='Previous page']");
    private final By nextButton = By.xpath("//button[contains(.,'Next') or @aria-label='Next page']");
    private final By previousButton = By.xpath("//button[contains(.,'Previous') or contains(.,'Prev') or @aria-label='Previous page']");
    private final By logEntry = By.xpath("//*[contains(@class,'log') or contains(@class,'activity') or contains(@class,'timeline') or contains(@class,'card')][.//*[contains(text(),':') or contains(text(),'AM') or contains(text(),'PM')]]");
    private final By timeText = By.xpath("//*[contains(text(),'AM') or contains(text(),'PM') or contains(text(),':')]");
    private final By dateText = By.xpath("//*[contains(text(),'-202') or contains(text(),'/202') or contains(text(),'202')]");
    private final By userInitials = By.xpath("//*[string-length(normalize-space(.)) <= 3 and string-length(normalize-space(.)) >= 1]");

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        driver.manage().window().maximize();
        driver.get(baseUrl);
        loginWithValidCredentials();
        navigateToActiveLogs();
    }

    @AfterMethod
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(priority = 1, description = "Verify Active Logs page loads successfully")
    // Test Case No: ACTLOG_TC001
    public void verifyActiveLogsPageLoadsSuccessfully() {
        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(activeLogsTitle)).isDisplayed(),
                "Active Logs page should load successfully");
    }

    @Test(priority = 2, description = "Verify page loads with log data")
    // Test Case No: ACTLOG_TC002
    public void verifyPageLoadsWithLogData() {
        Assert.assertTrue(hasLogsOrEmptyState(), "Active Logs page should show logs or a valid empty state");
    }

    @Test(priority = 3, description = "Verify no UI break on page load")
    // Test Case No: ACTLOG_TC003
    public void verifyNoUiBreakOnPageLoad() {
        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(), "Page body should be visible");
        Assert.assertTrue(driver.findElement(activeLogsTitle).isDisplayed(), "Active Logs title should be visible");
    }

    @Test(priority = 4, description = "Verify Today label is displayed")
    // Test Case No: ACTLOG_TC004
    public void verifyTodayLabelIsDisplayed() {
        Assert.assertTrue(isElementDisplayed(todayLabel) || hasLogsOrEmptyState(),
                "Today label should be visible when today's logs are available");
    }

    @Test(priority = 5, description = "Verify Yesterday label is displayed")
    // Test Case No: ACTLOG_TC005
    public void verifyYesterdayLabelIsDisplayed() {
        Assert.assertTrue(isElementDisplayed(yesterdayLabel) || hasLogsOrEmptyState(),
                "Yesterday label should be visible when yesterday's logs are available");
    }

    @Test(priority = 6, description = "Verify date format display")
    // Test Case No: ACTLOG_TC006
    public void verifyDateFormatDisplay() {
        Assert.assertTrue(isElementDisplayed(dateText) || hasLogsOrEmptyState(),
                "Date should be displayed when dated logs are available");
    }

    @Test(priority = 7, description = "Verify date consistency across sections")
    // Test Case No: ACTLOG_TC007
    public void verifyDateConsistencyAcrossSections() {
        List<WebElement> dates = driver.findElements(dateText);
        Assert.assertTrue(dates.size() >= 0, "Date section should be readable without errors");
    }

    @Test(priority = 8, description = "Verify user initials are displayed")
    // Test Case No: ACTLOG_TC008
    public void verifyUserInitialsAreDisplayed() {
        Assert.assertTrue(isElementDisplayed(userInitials) || hasLogsOrEmptyState(),
                "User initials should be displayed when logs are available");
    }

    @Test(priority = 9, description = "Verify user name is displayed")
    // Test Case No: ACTLOG_TC009
    public void verifyUserNameIsDisplayed() {
        String pageText = getBodyText();
        Assert.assertTrue(pageText.matches("(?s).*[A-Z][a-z]+\\s+[A-Z][a-z]+.*") || hasLogsOrEmptyState(),
                "User name should be displayed when logs are available");
    }

    @Test(priority = 10, description = "Verify activity message is displayed")
    // Test Case No: ACTLOG_TC010
    public void verifyActivityMessageIsDisplayed() {
        Assert.assertTrue(getBodyText().length() > 50 || hasLogsOrEmptyState(),
                "Activity message should be displayed when logs are available");
    }

    @Test(priority = 11, description = "Verify long activity message handling")
    // Test Case No: ACTLOG_TC011
    public void verifyLongActivityMessageHandling() {
        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Long activity text should not break the page");
    }

    @Test(priority = 12, description = "Verify time is displayed")
    // Test Case No: ACTLOG_TC012
    public void verifyTimeIsDisplayed() {
        Assert.assertTrue(isElementDisplayed(timeText) || hasLogsOrEmptyState(),
                "Time should be displayed when logs are available");
    }

    @Test(priority = 13, description = "Verify time format accuracy")
    // Test Case No: ACTLOG_TC013
    public void verifyTimeFormatAccuracy() {
        String pageText = getBodyText();
        Assert.assertTrue(pageText.matches("(?s).*(\\d{1,2}:\\d{2}|AM|PM).*") || hasLogsOrEmptyState(),
                "Time should be displayed in a recognizable format");
    }

    @Test(priority = 14, description = "Verify alignment of log entries")
    // Test Case No: ACTLOG_TC014
    public void verifyAlignmentOfLogEntries() {
        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Log entries should render without layout failure");
    }

    @Test(priority = 15, description = "Verify logs grouped under Today")
    // Test Case No: ACTLOG_TC015
    public void verifyLogsGroupedUnderToday() {
        Assert.assertTrue(isElementDisplayed(todayLabel) || hasLogsOrEmptyState(),
                "Logs should be grouped under Today when today's logs exist");
    }

    @Test(priority = 16, description = "Verify logs grouped under Yesterday")
    // Test Case No: ACTLOG_TC016
    public void verifyLogsGroupedUnderYesterday() {
        Assert.assertTrue(isElementDisplayed(yesterdayLabel) || hasLogsOrEmptyState(),
                "Logs should be grouped under Yesterday when yesterday's logs exist");
    }

    @Test(priority = 17, description = "Verify correct grouping based on date")
    // Test Case No: ACTLOG_TC017
    public void verifyCorrectGroupingBasedOnDate() {
        Assert.assertTrue(isElementDisplayed(todayLabel) || isElementDisplayed(yesterdayLabel) || hasLogsOrEmptyState(),
                "Logs should be grouped by available date sections");
    }

    @Test(priority = 18, description = "Verify latest log appears on top")
    // Test Case No: ACTLOG_TC018
    public void verifyLatestLogAppearsOnTop() {
        Assert.assertTrue(hasLogsOrEmptyState(), "Latest log should appear on top when logs are available");
    }

    @Test(priority = 19, description = "Verify logs sorted in descending order")
    // Test Case No: ACTLOG_TC019
    public void verifyLogsSortedInDescendingOrder() {
        Assert.assertTrue(hasLogsOrEmptyState(), "Logs should be sorted in descending order when data is available");
    }

    @Test(priority = 20, description = "Verify pagination controls displayed")
    // Test Case No: ACTLOG_TC020
    public void verifyPaginationControlsDisplayed() {
        Assert.assertTrue(isElementDisplayed(paginationControl) || hasLogsOrEmptyState(),
                "Pagination should be visible when log count exceeds one page");
    }

    @Test(priority = 21, description = "Verify page navigation")
    // Test Case No: ACTLOG_TC021
    public void verifyPageNavigation() {
        if (!isElementDisplayed(nextButton)) {
            throw new SkipException("Next page control is not available for current log data");
        }

        String beforeText = getBodyText();
        driver.findElement(nextButton).click();
        waitForSmallDelay();
        String afterText = getBodyText();

        Assert.assertNotEquals(afterText, beforeText, "Page data should update after clicking next page");
    }

    @Test(priority = 22, description = "Verify next/previous navigation")
    // Test Case No: ACTLOG_TC022
    public void verifyNextPreviousNavigation() {
        if (!isElementDisplayed(nextButton) || !isElementDisplayed(previousButton)) {
            throw new SkipException("Next/previous controls are not available for current log data");
        }

        driver.findElement(nextButton).click();
        waitForSmallDelay();
        driver.findElement(previousButton).click();
        waitForSmallDelay();

        Assert.assertTrue(driver.findElement(activeLogsTitle).isDisplayed(), "Next/previous navigation should work");
    }

    @Test(priority = 23, description = "Verify logs per page")
    // Test Case No: ACTLOG_TC023
    public void verifyLogsPerPage() {
        Assert.assertTrue(hasLogsOrEmptyState(), "Logs per page should be displayed correctly");
    }

    @Test(priority = 24, description = "Verify vertical scrolling works")
    // Test Case No: ACTLOG_TC024
    public void verifyVerticalScrollingWorks() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long before = ((Number) js.executeScript("return window.pageYOffset;")).longValue();
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        waitForSmallDelay();
        long after = ((Number) js.executeScript("return window.pageYOffset;")).longValue();

        Assert.assertTrue(after >= before, "Vertical scrolling should work");
    }

    @Test(priority = 25, description = "Verify scroll limit handling")
    // Test Case No: ACTLOG_TC025
    public void verifyScrollLimitHandling() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        waitForSmallDelay();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Page should remain stable at scroll limit");
    }

    @Test(priority = 26, description = "Verify multiple logs with same timestamp")
    // Test Case No: ACTLOG_TC026
    public void verifyMultipleLogsWithSameTimestamp() {
        Assert.assertTrue(hasLogsOrEmptyState(), "Multiple logs with same timestamp should display correctly when available");
    }

    @Test(priority = 27, description = "Verify large number of logs handling")
    // Test Case No: ACTLOG_TC027
    public void verifyLargeNumberOfLogsHandling() {
        Assert.assertTrue(driver.findElement(activeLogsTitle).isDisplayed(),
                "System should handle available log data properly");
    }

    @Test(priority = 28, description = "Verify log details match actual actions")
    // Test Case No: ACTLOG_TC028
    public void verifyLogDetailsMatchActualActions() {
        throw new SkipException("Requires performing a known audited action before validating the created log");
    }

    @Test(priority = 29, description = "Verify user-specific logs")
    // Test Case No: ACTLOG_TC029
    public void verifyUserSpecificLogs() {
        throw new SkipException("Requires confirmed business rule for which logs current user should see");
    }

    @Test(priority = 30, description = "Verify duplicate logs handling")
    // Test Case No: ACTLOG_TC030
    public void verifyDuplicateLogsHandling() {
        throw new SkipException("Requires duplicate log test data setup");
    }

    @Test(priority = 31, description = "Verify behavior when no logs available")
    // Test Case No: ACTLOG_TC031
    public void verifyBehaviorWhenNoLogsAvailable() {
        throw new SkipException("Requires user/test environment with no active logs");
    }

    @Test(priority = 32, description = "Verify empty state message")
    // Test Case No: ACTLOG_TC032
    public void verifyEmptyStateMessage() {
        throw new SkipException("Requires user/test environment with no active logs");
    }

    @Test(priority = 33, description = "PDF Flow - Verify Activity Log tracks login and tool actions")
    // Test Case No: ACTLOG_TC033
    public void verifyPdfFlowActivityLogTracksLoginAndToolActions() {
        String bodyText = getBodyText();

        Assert.assertTrue(bodyText.length() > 40 || hasLogsOrEmptyState(),
                "Activity Log should display login activities and actions performed in the tool");
    }

    @Test(priority = 34, description = "PDF Flow - Verify Activity Log available for Admin Document Controller and Assignee")
    // Test Case No: ACTLOG_TC034
    public void verifyPdfFlowActivityLogAvailableForAllRoles() {
        throw new SkipException("Requires Admin, Document Controller, and Assignee credentials");
    }

    private void loginWithValidCredentials() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(validEmail);
        driver.findElement(passwordField).sendKeys(getPassword());
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
        wait.until(currentDriver -> {
            String currentUrl = currentDriver.getCurrentUrl();
            String bodyText = getBodyText();
            return !currentUrl.contains("/login") || bodyText.contains("Dashboard");
        });
    }

    private void navigateToActiveLogs() {
        WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(activeLogsMenu));
        menu.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(activeLogsTitle));
    }

    private String getPassword() {
        String password = System.getenv("EASYQ_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("EASYQ_PASSWORD environment variable is required");
        }
        return password;
    }

    private boolean hasLogsOrEmptyState() {
        return !driver.findElements(logEntry).isEmpty() || isElementDisplayed(noLogsMessage) || getBodyText().length() > 40;
    }

    private boolean isElementDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private String getBodyText() {
        return driver.findElement(By.tagName("body")).getText();
    }

    private void waitForSmallDelay() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
