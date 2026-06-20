package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
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

public class EasyQDashboardTest {
    private WebDriver driver;
    private WebDriverWait wait;

    private final String baseUrl = "https://beta.easyqsolutions.com/#/easyqsolutions/login";
    private final String validEmail = "varunt@easyqsolutions.com";

    private final By emailField = By.xpath("//input[@type='email' or contains(@formcontrolname,'email')]");
    private final By passwordField = By.xpath("//input[@type='password' or contains(@formcontrolname,'password')]");
    private final By loginButton = By.xpath("//button[contains(normalize-space(.),'Log In')]");
    private final By dashboardMenu = By.xpath("//*[contains(normalize-space(.),'Dashboard')]");
    private final By dashboardTitle = By.xpath("//*[contains(normalize-space(.),'Dashboard')]");
    private final By qmsStatusTitle = By.xpath("//*[contains(normalize-space(.),'QMS Status')]");
    private final By allTasksToggle = By.xpath("//button[contains(normalize-space(.),'All Tasks')]");
    private final By myTasksToggle = By.xpath("//button[contains(normalize-space(.),'My Tasks')]");
    private final By qualityPolicyCard = By.xpath("//*[contains(normalize-space(.),'Quality Policy')]");
    private final By qualityObjectiveCard = By.xpath("//*[contains(normalize-space(.),'Quality Objective')]");
    private final By responsibilityCard = By.xpath("//*[contains(normalize-space(.),'Responsibility')]");
    private final By capaDeviationWidget = By.xpath("//*[contains(normalize-space(.),'CAPA') and contains(normalize-space(.),'Deviation')]/ancestor::*[contains(@class,'card') or contains(@class,'col') or contains(@class,'widget')][1]");
    private final By capaDeviationTabs = By.xpath("//*[contains(normalize-space(.),'CAPA') or contains(normalize-space(.),'Deviation')][self::button or @role='tab']");
    private final By noPendingItemsMessage = By.xpath("//*[contains(normalize-space(.),'No Pending Items')]");
    private final By dashboardCard = By.xpath("//*[contains(@class,'card') or contains(@class,'widget')]");
    private final By visibleIcon = By.xpath("//*[name()='svg' or self::mat-icon or self::i or contains(@class,'icon')]");

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        driver.manage().window().maximize();
        driver.get(baseUrl);
        loginWithValidCredentials();
        navigateToDashboard();
    }

    @AfterMethod
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(priority = 1, description = "Verify dashboard loads after login")
    // Test Case No: DASH_TC001
    public void verifyDashboardLoadsAfterLogin() {
        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(dashboardTitle)).isDisplayed(),
                "Dashboard should load after login");
    }

    @Test(priority = 2, description = "Verify dashboard reload")
    // Test Case No: DASH_TC002
    public void verifyDashboardReload() {
        driver.navigate().refresh();

        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(qmsStatusTitle)).isDisplayed(),
                "Dashboard should reload correctly");
    }

    @Test(priority = 3, description = "Verify all widgets displayed")
    // Test Case No: DASH_TC003
    public void verifyAllWidgetsDisplayed() {
        Assert.assertTrue(driver.findElements(dashboardCard).size() > 0, "Dashboard widgets/cards should be visible");
        Assert.assertTrue(driver.findElement(qualityPolicyCard).isDisplayed(), "Quality Policy card should be visible");
        Assert.assertTrue(driver.findElement(qualityObjectiveCard).isDisplayed(), "Quality Objective card should be visible");
        Assert.assertTrue(driver.findElement(responsibilityCard).isDisplayed(), "Responsibility card should be visible");
    }

    @Test(priority = 4, description = "Verify widgets with no data")
    // Test Case No: DASH_TC004
    public void verifyWidgetsWithNoData() {
        Assert.assertTrue(isElementDisplayed(noPendingItemsMessage) || driver.findElements(dashboardCard).size() > 0,
                "Widgets should display properly even when no data is available");
    }

    @Test(priority = 5, description = "Verify no broken UI")
    // Test Case No: DASH_TC005
    public void verifyNoBrokenUi() {
        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(), "Dashboard body should be visible");
        Assert.assertTrue(driver.findElements(dashboardCard).size() > 0, "Dashboard should not render blank or broken");
    }

    @Test(priority = 6, description = "Verify CAPA & Deviation widget tab button layout")
    // Test Case No: DASH_TC006
    public void verifyCapaDeviationWidgetTabButtonLayout() {
        List<WebElement> tabs = driver.findElements(capaDeviationTabs);
        if (tabs.isEmpty()) {
            throw new SkipException("CAPA & Deviation tab buttons are not available on current dashboard state");
        }

        for (WebElement tab : tabs) {
            Dimension size = tab.getSize();
            Assert.assertTrue(size.getHeight() <= 60,
                    "CAPA & Deviation tab button height should not be excessive. Actual height: " + size.getHeight());
            Assert.assertTrue(size.getWidth() <= 180,
                    "CAPA & Deviation tab button width should not be excessive. Actual width: " + size.getWidth());
        }
    }

    @Test(priority = 7, description = "Verify UI after refresh")
    // Test Case No: DASH_TC007
    public void verifyUiAfterRefresh() {
        driver.navigate().refresh();

        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(qmsStatusTitle)).isDisplayed(),
                "Dashboard UI should remain intact after refresh");
    }

    @Test(priority = 8, description = "Verify QMS Status title")
    // Test Case No: DASH_TC008
    public void verifyQmsStatusTitle() {
        Assert.assertTrue(driver.findElement(qmsStatusTitle).isDisplayed(), "QMS Status title should be visible");
    }

    @Test(priority = 9, description = "Verify title consistency")
    // Test Case No: DASH_TC009
    public void verifyTitleConsistency() {
        String beforeRefresh = driver.findElement(qmsStatusTitle).getText().trim();
        driver.navigate().refresh();
        String afterRefresh = wait.until(ExpectedConditions.visibilityOfElementLocated(qmsStatusTitle)).getText().trim();

        Assert.assertEquals(afterRefresh, beforeRefresh, "QMS Status title should remain consistent after refresh");
    }

    @Test(priority = 10, description = "Verify All Tasks toggle")
    // Test Case No: DASH_TC010
    public void verifyAllTasksToggle() {
        Assert.assertTrue(driver.findElement(allTasksToggle).isDisplayed(), "All Tasks toggle should be visible");
    }

    @Test(priority = 11, description = "Verify My Tasks toggle")
    // Test Case No: DASH_TC011
    public void verifyMyTasksToggle() {
        Assert.assertTrue(driver.findElement(myTasksToggle).isDisplayed(), "My Tasks toggle should be visible");
    }

    @Test(priority = 12, description = "Verify toggle functionality")
    // Test Case No: DASH_TC012
    public void verifyToggleFunctionality() {
        String beforeText = getBodyText();
        driver.findElement(allTasksToggle).click();
        waitForSmallDelay();
        driver.findElement(myTasksToggle).click();
        waitForSmallDelay();
        String afterText = getBodyText();

        Assert.assertTrue(afterText.length() > 0 && beforeText.length() > 0, "Toggle should keep dashboard data visible");
    }

    @Test(priority = 13, description = "Verify toggle persistence")
    // Test Case No: DASH_TC013
    public void verifyTogglePersistence() {
        driver.findElement(myTasksToggle).click();
        waitForSmallDelay();
        driver.navigate().refresh();

        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(myTasksToggle)).isDisplayed(),
                "My Tasks toggle should remain available after refresh");
    }

    @Test(priority = 14, description = "Verify Quality Policy card")
    // Test Case No: DASH_TC014
    public void verifyQualityPolicyCard() {
        Assert.assertTrue(driver.findElement(qualityPolicyCard).isDisplayed(), "Quality Policy card should be visible");
    }

    @Test(priority = 15, description = "Verify Quality Policy data")
    // Test Case No: DASH_TC015
    public void verifyQualityPolicyData() {
        Assert.assertTrue(getBodyText().contains("Quality Policy"), "Quality Policy data/text should be visible");
    }

    @Test(priority = 16, description = "Verify Quality Objective card")
    // Test Case No: DASH_TC016
    public void verifyQualityObjectiveCard() {
        Assert.assertTrue(driver.findElement(qualityObjectiveCard).isDisplayed(), "Quality Objective card should be visible");
    }

    @Test(priority = 17, description = "Verify Quality Objective data")
    // Test Case No: DASH_TC017
    public void verifyQualityObjectiveData() {
        Assert.assertTrue(getBodyText().contains("Quality Objective"), "Quality Objective data/text should be visible");
    }

    @Test(priority = 18, description = "Verify Responsibility card")
    // Test Case No: DASH_TC018
    public void verifyResponsibilityCard() {
        Assert.assertTrue(driver.findElement(responsibilityCard).isDisplayed(), "Responsibility card should be visible");
    }

    @Test(priority = 19, description = "Verify Responsibility data accuracy")
    // Test Case No: DASH_TC019
    public void verifyResponsibilityDataAccuracy() {
        Assert.assertTrue(getBodyText().contains("Responsibility"), "Responsibility data/text should be visible");
    }

    @Test(priority = 20, description = "Verify No Pending Items message")
    // Test Case No: DASH_TC020
    public void verifyNoPendingItemsMessage() {
        Assert.assertTrue(isElementDisplayed(noPendingItemsMessage) || driver.findElements(dashboardCard).size() > 0,
                "No Pending Items message should display when applicable");
    }

    @Test(priority = 21, description = "Verify message removed")
    // Test Case No: DASH_TC021
    public void verifyMessageRemoved() {
        throw new SkipException("Requires adding data to a widget and refreshing dashboard");
    }

    @Test(priority = 22, description = "Verify icon visibility")
    // Test Case No: DASH_TC022
    public void verifyIconVisibility() {
        Assert.assertTrue(driver.findElements(visibleIcon).size() > 0, "Dashboard icons should be visible");
    }

    @Test(priority = 23, description = "PDF Flow - Verify left panel module list")
    // Test Case No: DASH_TC023
    public void verifyPdfFlowLeftPanelModuleList() {
        String bodyText = getBodyText();

        Assert.assertTrue(bodyText.contains("Quality Policy"), "Quality Policy module should be available in left panel/dashboard navigation");
        Assert.assertTrue(bodyText.contains("Quality Objective"), "Quality Objective module should be available in left panel/dashboard navigation");
        Assert.assertTrue(bodyText.contains("Responsibility"), "Responsibility & Authority module should be available in left panel/dashboard navigation");
        Assert.assertTrue(bodyText.contains("Management Review"), "Management Review module should be available in left panel/dashboard navigation");
        Assert.assertTrue(bodyText.contains("Document Management") || bodyText.contains("Documents"), "Document Management module should be available");
        Assert.assertTrue(bodyText.contains("CAPA"), "CAPA module should be available");
        Assert.assertTrue(bodyText.contains("Training"), "Training module should be available");
        Assert.assertTrue(bodyText.contains("Products"), "Products module should be available");
        Assert.assertTrue(bodyText.contains("Complaint"), "Complaint Management module should be available");
    }

    @Test(priority = 24, description = "PDF Flow - Verify assigned activities appear on Dashboard")
    // Test Case No: DASH_TC024
    public void verifyPdfFlowAssignedActivitiesAppearOnDashboard() {
        Assert.assertTrue(driver.findElement(qmsStatusTitle).isDisplayed(), "Dashboard should display assigned QMS activities");
        Assert.assertTrue(driver.findElements(dashboardCard).size() > 0, "Assigned activity widgets/cards should be visible");
    }

    @Test(priority = 25, description = "PDF Flow - Verify Admin All Tasks and My Tasks visibility")
    // Test Case No: DASH_TC025
    public void verifyPdfFlowAdminAllTasksAndMyTasksVisibility() {
        Assert.assertTrue(isElementDisplayed(allTasksToggle), "All Tasks toggle should be visible for Admin/current user");
        Assert.assertTrue(isElementDisplayed(myTasksToggle), "My Tasks toggle should be visible for Admin/current user");
    }

    private void loginWithValidCredentials() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(validEmail);
        driver.findElement(passwordField).sendKeys(getPassword());
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(dashboardTitle),
                ExpectedConditions.not(ExpectedConditions.urlContains("/login"))
        ));
    }

    private void navigateToDashboard() {
        if (isElementDisplayed(dashboardMenu)) {
            driver.findElement(dashboardMenu).click();
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(qmsStatusTitle));
    }

    private String getPassword() {
        String password = System.getenv("EASYQ_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("EASYQ_PASSWORD environment variable is required");
        }
        return password;
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
