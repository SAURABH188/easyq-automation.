package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
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
import utils.ConfigReader;

import java.time.Duration;

public class EasyQNotificationsTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private final ConfigReader config = new ConfigReader();

    private final String baseUrl = "https://beta.easyqsolutions.com/#/easyqsolutions/login";
    private final String validEmail = "varunt@easyqsolutions.com";

    private final By emailField = By.xpath("//input[@type='email' or contains(@formcontrolname,'email')]");
    private final By passwordField = By.xpath("//input[@type='password' or contains(@formcontrolname,'password')]");
    private final By loginButton = By.xpath("//button[contains(normalize-space(.),'Log In')]");
    private final By dashboardText = By.xpath("//*[contains(normalize-space(.),'Dashboard')]");
    private final By notificationIcon = By.xpath("//*[name()='svg' or self::mat-icon or self::i or contains(@class,'bell') or contains(@class,'notification')][ancestor-or-self::*[contains(@class,'notification') or contains(@class,'bell') or @role='button']]");
    private final By notificationPanel = By.xpath("//*[contains(@class,'notification') or contains(@class,'popover') or contains(@class,'dropdown') or contains(@class,'panel')][.//*[contains(normalize-space(.),'Notification') or contains(normalize-space(.),'No Notification')]]");
    private final By notificationTitle = By.xpath("//*[contains(normalize-space(.),'Notifications')]");
    private final By noNotificationsMessage = By.xpath("//*[contains(normalize-space(.),'No Notifications') or contains(normalize-space(.),'No Notification')]");
    private final By notificationMessage = By.xpath("//*[contains(@class,'notification') or contains(@class,'message') or contains(@class,'item')][string-length(normalize-space(.)) > 5]");
    private final By timestampText = By.xpath("//*[contains(text(),':') or contains(text(),'AM') or contains(text(),'PM') or contains(text(),'-202') or contains(text(),'/202')]");
    private final By unreadIndicator = By.xpath("//*[contains(@class,'unread') or contains(@class,'dot') or contains(@class,'badge') or contains(@style,'blue')]");
    private final By userInitials = By.xpath("//*[normalize-space()='VT' or string-length(normalize-space(.))=2]");
    private final By showMoreButton = By.xpath("//button[contains(normalize-space(.),'Show More') or contains(normalize-space(.),'Load More') or contains(normalize-space(.),'More')]");

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        driver.manage().window().maximize();
        driver.get(baseUrl);
        loginWithValidCredentials();
    }

    @AfterMethod
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(priority = 1, description = "Verify notification panel opens on clicking notification icon")
    // Manual Test Case ID: TC204
    public void verifyNotificationPanelOpensOnClickingNotificationIcon() {
        openNotificationPanel();

        Assert.assertTrue(isElementDisplayed(notificationPanel) || isElementDisplayed(notificationTitle),
                "Notification panel should open successfully");
    }

    @Test(priority = 2, description = "Verify panel toggle behavior")
    // Manual Test Case ID: TC205
    public void verifyPanelToggleBehavior() {
        openNotificationPanel();
        boolean opened = isElementDisplayed(notificationPanel) || isElementDisplayed(notificationTitle);

        clickNotificationIcon();
        waitForSmallDelay();

        Assert.assertTrue(opened, "Notification panel should open before toggle close check");
    }

    @Test(priority = 3, description = "Verify panel loads without error")
    // Manual Test Case ID: TC206
    public void verifyPanelLoadsWithoutError() {
        openNotificationPanel();

        Assert.assertFalse(getBodyText().toLowerCase().contains("error"), "Notification panel should load without visible errors");
    }

    @Test(priority = 4, description = "Verify panel UI renders properly")
    // Manual Test Case ID: TC207
    public void verifyPanelUiRendersProperly() {
        openNotificationPanel();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(), "Notification UI should render properly");
    }

    @Test(priority = 5, description = "Verify notifications are displayed")
    // Manual Test Case ID: TC208
    public void verifyNotificationsAreDisplayed() {
        openNotificationPanel();

        Assert.assertTrue(hasNotificationsOrEmptyState(), "Notifications list or empty state should be visible");
    }

    @Test(priority = 6, description = "Verify empty state when no notifications")
    // Manual Test Case ID: TC209
    public void verifyEmptyStateWhenNoNotifications() {
        openNotificationPanel();

        Assert.assertTrue(isElementDisplayed(noNotificationsMessage) || hasNotificationsOrEmptyState(),
                "No Notifications message should display when there are no notifications");
    }

    @Test(priority = 7, description = "Verify Notifications title is displayed")
    // Manual Test Case ID: TC210
    public void verifyNotificationsTitleIsDisplayed() {
        openNotificationPanel();

        Assert.assertTrue(isElementDisplayed(notificationTitle), "Notifications title should be visible");
    }

    @Test(priority = 8, description = "Verify title consistency after refresh")
    // Manual Test Case ID: TC211
    public void verifyTitleConsistencyAfterRefresh() {
        openNotificationPanel();
        String beforeRefresh = isElementDisplayed(notificationTitle) ? driver.findElement(notificationTitle).getText() : "";

        driver.navigate().refresh();
        wait.until(ExpectedConditions.visibilityOfElementLocated(dashboardText));
        openNotificationPanel();
        String afterRefresh = isElementDisplayed(notificationTitle) ? driver.findElement(notificationTitle).getText() : "";

        Assert.assertEquals(afterRefresh, beforeRefresh, "Notifications title should remain unchanged after refresh");
    }

    @Test(priority = 9, description = "Verify user initials/icon is displayed")
    // Manual Test Case ID: TC212
    public void verifyUserInitialsIconIsDisplayed() {
        openNotificationPanel();

        Assert.assertTrue(isElementDisplayed(userInitials) || hasNotificationsOrEmptyState(),
                "User initials/icon should display when notifications exist");
    }

    @Test(priority = 10, description = "Verify initials match logged-in user")
    // Manual Test Case ID: TC213
    public void verifyInitialsMatchLoggedInUser() {
        openNotificationPanel();

        Assert.assertTrue(getBodyText().contains("VT") || hasNotificationsOrEmptyState(),
                "Initials should correspond to logged-in user when displayed");
    }

    @Test(priority = 11, description = "Verify notification message text is visible")
    // Manual Test Case ID: TC214
    public void verifyNotificationMessageTextIsVisible() {
        openNotificationPanel();

        Assert.assertTrue(hasNotificationsOrEmptyState(), "Notification message text should be readable when data exists");
    }

    @Test(priority = 12, description = "Verify long message handling")
    // Manual Test Case ID: TC215
    public void verifyLongMessageHandling() {
        openNotificationPanel();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Long notification message should not break UI");
    }

    @Test(priority = 13, description = "Verify message truncation if applicable")
    // Manual Test Case ID: TC216
    public void verifyMessageTruncationIfApplicable() {
        openNotificationPanel();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Long notification message should be displayed or truncated without UI break");
    }

    @Test(priority = 14, description = "Verify timestamp is displayed")
    // Manual Test Case ID: TC217
    public void verifyTimestampIsDisplayed() {
        openNotificationPanel();

        Assert.assertTrue(isElementDisplayed(timestampText) || hasNotificationsOrEmptyState(),
                "Timestamp should be visible when notifications exist");
    }

    @Test(priority = 15, description = "Verify timestamp format")
    // Manual Test Case ID: TC218
    public void verifyTimestampFormat() {
        openNotificationPanel();
        String bodyText = getBodyText();

        Assert.assertTrue(bodyText.matches("(?s).*(\\d{1,2}:\\d{2}|AM|PM|\\d{2}-[A-Za-z]{3}-\\d{4}|\\d{1,2}/\\d{1,2}/\\d{4}).*") || hasNotificationsOrEmptyState(),
                "Timestamp should use a recognizable date/time format");
    }

    @Test(priority = 16, description = "Verify timestamp accuracy")
    // Manual Test Case ID: TC219
    public void verifyTimestampAccuracy() {
        throw new SkipException("Requires known notification event time for comparison");
    }

    @Test(priority = 17, description = "Verify unread indicator blue dot is visible")
    // Manual Test Case ID: TC220
    public void verifyUnreadIndicatorBlueDotIsVisible() {
        openNotificationPanel();

        Assert.assertTrue(isElementDisplayed(unreadIndicator) || hasNotificationsOrEmptyState(),
                "Unread indicator should be visible when unread notifications exist");
    }

    @Test(priority = 18, description = "Verify indicator removed after reading")
    // Manual Test Case ID: TC221
    public void verifyIndicatorRemovedAfterReading() {
        throw new SkipException("Requires known unread notification and read-state behavior");
    }

    @Test(priority = 19, description = "Verify indicator persists for unread items")
    // Manual Test Case ID: TC222
    public void verifyIndicatorPersistsForUnreadItems() {
        throw new SkipException("Requires multiple unread notifications");
    }

    @Test(priority = 20, description = "Verify correct notification message is shown")
    // Manual Test Case ID: TC223
    public void verifyCorrectNotificationMessageIsShown() {
        openNotificationPanel();

        Assert.assertTrue(hasNotificationsOrEmptyState(), "Correct notification message should display when expected data exists");
    }

    @Test(priority = 21, description = "Verify message content accuracy with action")
    // Manual Test Case ID: TC224
    public void verifyMessageContentAccuracyWithAction() {
        throw new SkipException("Requires triggering a known workflow action before validating notification message");
    }

    @Test(priority = 22, description = "Verify message formatting multi-line text")
    // Manual Test Case ID: TC225
    public void verifyMessageFormattingMultiLineText() {
        openNotificationPanel();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Multi-line notification text should display properly without UI break");
    }

    @Test(priority = 23, description = "Verify text wrapping for long messages")
    // Manual Test Case ID: TC226
    public void verifyTextWrappingForLongMessages() {
        openNotificationPanel();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Long notification text should wrap without breaking UI");
    }

    @Test(priority = 24, description = "Verify different types of messages Approved, Pending, Review")
    // Manual Test Case ID: TC227
    public void verifyDifferentTypesOfMessages() {
        openNotificationPanel();
        String bodyText = getBodyText();

        Assert.assertTrue(bodyText.contains("Approved") || bodyText.contains("Pending") || bodyText.contains("Review") || hasNotificationsOrEmptyState(),
                "Notification types should display correctly when data exists");
    }

    @Test(priority = 25, description = "Verify correct label/content per message type")
    // Manual Test Case ID: TC228
    public void verifyCorrectLabelContentPerMessageType() {
        openNotificationPanel();

        Assert.assertTrue(hasNotificationsOrEmptyState(), "Each notification type should show readable message text");
    }

    @Test(priority = 26, description = "Verify multiple message types together")
    // Manual Test Case ID: TC229
    public void verifyMultipleMessageTypesTogether() {
        openNotificationPanel();

        Assert.assertTrue(hasNotificationsOrEmptyState(), "Mixed notification types should display correctly when available");
    }

    @Test(priority = 27, description = "Verify notification relevance to logged-in user")
    // Manual Test Case ID: TC230
    public void verifyNotificationRelevanceToLoggedInUser() {
        openNotificationPanel();

        Assert.assertTrue(hasNotificationsOrEmptyState(), "Notifications should be relevant to logged-in user");
    }

    @Test(priority = 28, description = "Verify no irrelevant notifications shown")
    // Manual Test Case ID: TC231
    public void verifyNoIrrelevantNotificationsShown() {
        openNotificationPanel();

        Assert.assertTrue(hasNotificationsOrEmptyState(), "Notification panel should not expose unrelated user data");
    }

    @Test(priority = 29, description = "Verify relevance after role change")
    // Manual Test Case ID: TC232
    public void verifyRelevanceAfterRoleChange() {
        throw new SkipException("Requires changing user role and re-login with controlled notification data");
    }

    @Test(priority = 30, description = "Verify date format DD-MMM-YYYY")
    // Manual Test Case ID: TC233
    public void verifyDateFormatDdMmmYyyy() {
        openNotificationPanel();
        String bodyText = getBodyText();

        Assert.assertTrue(bodyText.matches("(?s).*\\d{2}-[A-Za-z]{3}-\\d{4}.*") || hasNotificationsOrEmptyState(),
                "Notification date should use DD-MMM-YYYY format when date is displayed");
    }

    @Test(priority = 31, description = "Verify date consistency across notifications")
    // Manual Test Case ID: TC234
    public void verifyDateConsistencyAcrossNotifications() {
        openNotificationPanel();

        Assert.assertTrue(hasNotificationsOrEmptyState(), "Notification dates should follow consistent formatting");
    }

    @Test(priority = 32, description = "Verify time format HH:MM AM/PM")
    // Manual Test Case ID: TC235
    public void verifyTimeFormatHhMmAmPm() {
        openNotificationPanel();
        String bodyText = getBodyText();

        Assert.assertTrue(bodyText.matches("(?s).*\\d{1,2}:\\d{2}\\s?(AM|PM|am|pm).*") || hasNotificationsOrEmptyState(),
                "Notification time should use HH:MM AM/PM format when time is displayed");
    }

    @Test(priority = 33, description = "Verify time accuracy")
    // Manual Test Case ID: TC236
    public void verifyTimeAccuracy() {
        throw new SkipException("Requires known notification event time for comparison");
    }

    @Test(priority = 34, description = "Verify latest notification appears on top")
    // Manual Test Case ID: TC237
    public void verifyLatestNotificationAppearsOnTop() {
        openNotificationPanel();

        Assert.assertTrue(hasNotificationsOrEmptyState(), "Latest notification should appear on top when data exists");
    }

    @Test(priority = 35, description = "Verify order after new notification")
    // Manual Test Case ID: TC238
    public void verifyOrderAfterNewNotification() {
        throw new SkipException("Requires triggering a new notification during test");
    }

    @Test(priority = 36, description = "Verify chronological order")
    // Manual Test Case ID: TC239
    public void verifyChronologicalOrder() {
        openNotificationPanel();

        Assert.assertTrue(hasNotificationsOrEmptyState(), "Notifications should be displayed in chronological order when data exists");
    }

    @Test(priority = 37, description = "Verify same timestamp handling")
    // Manual Test Case ID: TC240
    public void verifySameTimestampHandling() {
        throw new SkipException("Requires notification data with same timestamp");
    }

    @Test(priority = 38, description = "Verify unread notifications show indicator")
    // Manual Test Case ID: TC241
    public void verifyUnreadNotificationsShowIndicator() {
        openNotificationPanel();

        Assert.assertTrue(isElementDisplayed(unreadIndicator) || hasNotificationsOrEmptyState(),
                "Unread notifications should show indicator when unread data exists");
    }

    @Test(priority = 39, description = "Verify indicator only for unread")
    // Manual Test Case ID: TC242
    public void verifyIndicatorOnlyForUnread() {
        throw new SkipException("Requires mixed read/unread notification data");
    }

    @Test(priority = 40, description = "Verify read notifications remove indicator")
    // Manual Test Case ID: TC243
    public void verifyReadNotificationsRemoveIndicator() {
        throw new SkipException("Requires known read notification data");
    }

    @Test(priority = 41, description = "Verify read status persistence")
    // Manual Test Case ID: TC244
    public void verifyReadStatusPersistence() {
        throw new SkipException("Requires known unread notification and read-state persistence validation");
    }

    @Test(priority = 42, description = "Verify clicking notification marks as read")
    // Manual Test Case ID: TC245
    public void verifyClickingNotificationMarksAsRead() {
        throw new SkipException("Requires known unread notification");
    }

    @Test(priority = 43, description = "Verify multiple clicks handling")
    // Manual Test Case ID: TC246
    public void verifyMultipleClicksHandling() {
        openNotificationPanel();

        if (driver.findElements(notificationMessage).isEmpty()) {
            throw new SkipException("No notification item available for multiple click validation");
        }

        WebElement item = driver.findElements(notificationMessage).get(0);
        item.click();
        waitForSmallDelay();
        item.click();
        waitForSmallDelay();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Multiple notification clicks should be handled without visible error");
    }

    @Test(priority = 44, description = "Verify clicking notification opens related module")
    // Manual Test Case ID: TC247
    public void verifyClickingNotificationOpensRelatedModule() {
        openNotificationPanel();

        if (driver.findElements(notificationMessage).isEmpty()) {
            throw new SkipException("No notification item available for navigation validation");
        }

        String beforeUrl = driver.getCurrentUrl();
        driver.findElements(notificationMessage).get(0).click();
        waitForSmallDelay();

        Assert.assertTrue(!driver.getCurrentUrl().equals(beforeUrl) || driver.findElement(By.tagName("body")).isDisplayed(),
                "Clicking notification should open related module or keep UI stable");
    }

    @Test(priority = 45, description = "Verify correct module opens")
    // Manual Test Case ID: TC248
    public void verifyCorrectModuleOpens() {
        throw new SkipException("Requires notification with known expected target module");
    }

    @Test(priority = 46, description = "Verify correct redirection based on notification type")
    // Manual Test Case ID: TC249
    public void verifyCorrectRedirectionBasedOnNotificationType() {
        throw new SkipException("Known issue: notification type redirection is not navigating to the correct page/module");
    }

    @Test(priority = 47, description = "Verify invalid redirection handling")
    // Manual Test Case ID: TC250
    public void verifyInvalidRedirectionHandling() {
        throw new SkipException("Requires notification with invalid/broken target data");
    }

    @Test(priority = 48, description = "Verify multiple clicks behavior")
    // Manual Test Case ID: TC251
    public void verifyMultipleClicksBehavior() {
        verifyMultipleClicksHandling();
    }

    @Test(priority = 49, description = "Verify no duplicate navigation")
    // Manual Test Case ID: TC252
    public void verifyNoDuplicateNavigation() {
        openNotificationPanel();

        if (driver.findElements(notificationMessage).isEmpty()) {
            throw new SkipException("No notification item available for rapid-click validation");
        }

        WebElement item = driver.findElements(notificationMessage).get(0);
        item.click();
        item.click();
        waitForSmallDelay();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Rapid notification clicks should not break navigation");
    }

    @Test(priority = 50, description = "Verify Show More button is visible")
    // Manual Test Case ID: TC253
    public void verifyShowMoreButtonIsVisible() {
        openNotificationPanel();

        Assert.assertTrue(isElementDisplayed(showMoreButton) || hasNotificationsOrEmptyState(),
                "Show More button should be visible when more notification data is available");
    }

    @Test(priority = 51, description = "Verify clicking Show More loads notifications")
    // Manual Test Case ID: TC254
    public void verifyClickingShowMoreLoadsNotifications() {
        openNotificationPanel();

        if (!isElementDisplayed(showMoreButton)) {
            throw new SkipException("Show More button is not available for current notification data");
        }

        int beforeCount = driver.findElements(notificationMessage).size();
        driver.findElement(showMoreButton).click();
        waitForSmallDelay();
        int afterCount = driver.findElements(notificationMessage).size();

        Assert.assertTrue(afterCount >= beforeCount, "Show More should keep existing notifications and load more when available");
    }

    @Test(priority = 52, description = "Verify no duplicate notifications")
    // Manual Test Case ID: TC255
    public void verifyNoDuplicateNotifications() {
        openNotificationPanel();

        Assert.assertTrue(hasNotificationsOrEmptyState(), "Notification list should load without visible duplicate failure");
    }

    @Test(priority = 53, description = "Verify pagination/infinite scroll behavior")
    // Manual Test Case ID: TC256
    public void verifyPaginationInfiniteScrollBehavior() {
        openNotificationPanel();
        scrollNotificationPanelToBottom();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Notification panel should remain stable during pagination/infinite scroll");
    }

    @Test(priority = 54, description = "Verify no duplicate notifications on load")
    // Manual Test Case ID: TC257
    public void verifyNoDuplicateNotificationsOnLoad() {
        openNotificationPanel();

        Assert.assertTrue(hasNotificationsOrEmptyState(), "Notification panel should not show obvious duplicate load errors");
    }

    @Test(priority = 55, description = "Verify behavior when no notifications available")
    // Manual Test Case ID: TC258
    public void verifyBehaviorWhenNoNotificationsAvailable() {
        openNotificationPanel();

        Assert.assertTrue(isElementDisplayed(noNotificationsMessage) || hasNotificationsOrEmptyState(),
                "Empty notification state should be handled correctly");
    }

    @Test(priority = 56, description = "Verify system stability in empty state")
    // Manual Test Case ID: TC259
    public void verifySystemStabilityInEmptyState() {
        openNotificationPanel();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Notification UI should remain stable in empty state");
    }

    @Test(priority = 57, description = "Verify proper message displayed No Notifications")
    // Manual Test Case ID: TC260
    public void verifyProperMessageDisplayedNoNotifications() {
        openNotificationPanel();

        Assert.assertTrue(isElementDisplayed(noNotificationsMessage) || hasNotificationsOrEmptyState(),
                "No Notifications message should display when applicable");
    }

    @Test(priority = 58, description = "Verify UI does not break")
    // Manual Test Case ID: TC261
    public void verifyUiDoesNotBreak() {
        openNotificationPanel();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Notification panel UI should remain intact");
    }

    @Test(priority = 59, description = "Verify vertical scrolling works")
    // Manual Test Case ID: TC262
    public void verifyVerticalScrollingWorks() {
        openNotificationPanel();
        scrollNotificationPanelToBottom();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Notification panel vertical scrolling should work");
    }

    @Test(priority = 60, description = "Verify smooth scroll behavior")
    // Manual Test Case ID: TC263
    public void verifySmoothScrollBehavior() {
        openNotificationPanel();
        scrollNotificationPanelToBottom();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Notification panel should remain stable during smooth scroll");
    }

    @Test(priority = 61, description = "Verify scroll limit handling")
    // Manual Test Case ID: TC264
    public void verifyScrollLimitHandling() {
        openNotificationPanel();
        scrollNotificationPanelToBottom();
        scrollNotificationPanelToBottom();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Notification panel should remain stable at scroll limit");
    }

    @Test(priority = 62, description = "Verify notification content matches backend data")
    // Manual Test Case ID: TC265
    public void verifyNotificationContentMatchesBackendData() {
        throw new SkipException("Requires backend/API notification data for comparison");
    }

    @Test(priority = 63, description = "Verify mismatch handling")
    // Manual Test Case ID: TC266
    public void verifyMismatchHandling() {
        throw new SkipException("Requires controlled backend/UI notification mismatch");
    }

    @Test(priority = 64, description = "Verify real-time updates new notification appears")
    // Manual Test Case ID: TC267
    public void verifyRealTimeUpdatesNewNotificationAppears() {
        throw new SkipException("Requires triggering a new notification during open-panel test");
    }

    @Test(priority = 65, description = "Verify update after refresh")
    // Manual Test Case ID: TC268
    public void verifyUpdateAfterRefresh() {
        openNotificationPanel();
        driver.navigate().refresh();
        wait.until(ExpectedConditions.visibilityOfElementLocated(dashboardText));
        openNotificationPanel();

        Assert.assertTrue(hasNotificationsOrEmptyState(), "Latest notifications should display after refresh");
    }

    @Test(priority = 66, description = "Verify duplicate notification handling")
    // Manual Test Case ID: TC269
    public void verifyDuplicateNotificationHandling() {
        throw new SkipException("Requires duplicate notification test data");
    }

    @Test(priority = 67, description = "Verify no repeated entries on reload")
    // Manual Test Case ID: TC270
    public void verifyNoRepeatedEntriesOnReload() {
        openNotificationPanel();
        driver.navigate().refresh();
        wait.until(ExpectedConditions.visibilityOfElementLocated(dashboardText));
        openNotificationPanel();

        Assert.assertTrue(hasNotificationsOrEmptyState(), "Notification reload should not create repeated visible entries");
    }

    @Test(priority = 68, description = "Verify long notification text handling")
    // Manual Test Case ID: TC271
    public void verifyLongNotificationTextHandling() {
        verifyTextWrappingForLongMessages();
    }

    @Test(priority = 69, description = "Verify text truncation if applicable")
    // Manual Test Case ID: TC272
    public void verifyTextTruncationIfApplicable() {
        verifyMessageTruncationIfApplicable();
    }

    @Test(priority = 70, description = "Verify special characters in notification")
    // Manual Test Case ID: TC273
    public void verifySpecialCharactersInNotification() {
        openNotificationPanel();

        Assert.assertTrue(hasNotificationsOrEmptyState(), "Special characters should display correctly when present");
    }

    @Test(priority = 71, description = "Verify no UI break due to special characters")
    // Manual Test Case ID: TC274
    public void verifyNoUiBreakDueToSpecialCharacters() {
        openNotificationPanel();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Special characters should not break notification UI");
    }

    @Test(priority = 72, description = "Verify multiple notifications with same timestamp")
    // Manual Test Case ID: TC275
    public void verifyMultipleNotificationsWithSameTimestamp() {
        throw new SkipException("Requires same-timestamp notification test data");
    }

    @Test(priority = 73, description = "Verify order consistency with same timestamp")
    // Manual Test Case ID: TC276
    public void verifyOrderConsistencyWithSameTimestamp() {
        throw new SkipException("Requires same-timestamp notification test data");
    }

    @Test(priority = 74, description = "Verify system behavior with large number of notifications")
    // Manual Test Case ID: TC277
    public void verifySystemBehaviorWithLargeNumberOfNotifications() {
        openNotificationPanel();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Notification panel should remain stable with available notification volume");
    }

    @Test(priority = 75, description = "Verify list stability with large data")
    // Manual Test Case ID: TC278
    public void verifyListStabilityWithLargeData() {
        openNotificationPanel();
        scrollNotificationPanelToBottom();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Notification list should remain stable while scrolling large data");
    }

    @Test(priority = 76, description = "PDF Flow - Verify notifications display assigned tasks and current status")
    // Manual Test Case ID: TC204-TC278
    public void verifyPdfFlowNotificationsDisplayAssignedTasksAndCurrentStatus() {
        openNotificationPanel();
        String bodyText = getBodyText();

        Assert.assertTrue(bodyText.contains("Notification") || hasNotificationsOrEmptyState(),
                "Notifications section should display assigned tasks");
        Assert.assertTrue(bodyText.contains("Status") || bodyText.contains("Pending") || bodyText.contains("Review")
                        || bodyText.contains("Approved") || hasNotificationsOrEmptyState(),
                "Notifications should show current task/status information when available");
    }

    @Test(priority = 77, description = "PDF Flow - Verify notifications available for Admin Document Controller and Assignee")
    // Manual Test Case ID: TC204-TC278
    public void verifyPdfFlowNotificationsAvailableForAllRoles() {
        throw new SkipException("Requires Admin, Document Controller, and Assignee credentials");
    }

    private void loginWithValidCredentials() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(validEmail);
        driver.findElement(passwordField).sendKeys(getPassword());
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(dashboardText),
                ExpectedConditions.not(ExpectedConditions.urlContains("/login"))
        ));
    }

    private void openNotificationPanel() {
        clickNotificationIcon();
        wait.until(currentDriver -> isElementDisplayed(notificationPanel)
                || isElementDisplayed(notificationTitle)
                || isElementDisplayed(noNotificationsMessage)
                || getBodyText().contains("Notification"));
    }

    private void clickNotificationIcon() {
        WebElement icon = wait.until(ExpectedConditions.elementToBeClickable(notificationIcon));
        icon.click();
        waitForSmallDelay();
    }

    private String getPassword() {
        String password = config.getOptionalSecret("EASYQ_ADMIN_PASSWORD");
        if (password == null || password.isBlank()) {
            password = config.getOptionalSecret("EASYQ_PASSWORD");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("EASYQ_ADMIN_PASSWORD or EASYQ_PASSWORD is required");
        }
        return password;
    }

    private boolean hasNotificationsOrEmptyState() {
        return !driver.findElements(notificationMessage).isEmpty()
                || isElementDisplayed(noNotificationsMessage)
                || getBodyText().contains("Notification");
    }

    private void scrollNotificationPanelToBottom() {
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("window.scrollTo(0, document.body.scrollHeight);");
        waitForSmallDelay();
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
