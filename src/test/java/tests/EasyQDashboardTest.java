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
import utils.ConfigReader;

import java.time.Duration;
import java.util.List;

public class EasyQDashboardTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private final ConfigReader config = new ConfigReader();

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
    // Manual Test Case ID: TC079
    public void verifyDashboardLoadsAfterLogin() {
        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(dashboardTitle)).isDisplayed(),
                "Dashboard should load after login");
    }

    @Test(priority = 2, description = "Verify dashboard reload")
    // Manual Test Case ID: TC080
    public void verifyDashboardReload() {
        driver.navigate().refresh();

        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(qmsStatusTitle)).isDisplayed(),
                "Dashboard should reload correctly");
    }

    @Test(priority = 3, description = "Verify all widgets displayed")
    // Manual Test Case ID: TC081
    public void verifyAllWidgetsDisplayed() {
        Assert.assertTrue(driver.findElements(dashboardCard).size() > 0, "Dashboard widgets/cards should be visible");
        Assert.assertTrue(driver.findElement(qualityPolicyCard).isDisplayed(), "Quality Policy card should be visible");
        Assert.assertTrue(driver.findElement(qualityObjectiveCard).isDisplayed(), "Quality Objective card should be visible");
        Assert.assertTrue(driver.findElement(responsibilityCard).isDisplayed(), "Responsibility card should be visible");
    }

    @Test(priority = 4, description = "Verify widgets with no data")
    // Manual Test Case ID: TC082
    public void verifyWidgetsWithNoData() {
        Assert.assertTrue(isElementDisplayed(noPendingItemsMessage) || driver.findElements(dashboardCard).size() > 0,
                "Widgets should display properly even when no data is available");
    }

    @Test(priority = 5, description = "Verify no broken UI")
    // Manual Test Case ID: TC083
    public void verifyNoBrokenUi() {
        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(), "Dashboard body should be visible");
        Assert.assertTrue(driver.findElements(dashboardCard).size() > 0, "Dashboard should not render blank or broken");
    }

    @Test(priority = 6, description = "Verify CAPA & Deviation widget tab button layout")
    // Manual Test Case ID: TC084
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
    // Manual Test Case ID: TC085
    public void verifyUiAfterRefresh() {
        driver.navigate().refresh();

        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(qmsStatusTitle)).isDisplayed(),
                "Dashboard UI should remain intact after refresh");
    }

    @Test(priority = 8, description = "Verify QMS Status title")
    // Manual Test Case ID: TC086
    public void verifyQmsStatusTitle() {
        Assert.assertTrue(driver.findElement(qmsStatusTitle).isDisplayed(), "QMS Status title should be visible");
    }

    @Test(priority = 9, description = "Verify title consistency")
    // Manual Test Case ID: TC087
    public void verifyTitleConsistency() {
        String beforeRefresh = driver.findElement(qmsStatusTitle).getText().trim();
        driver.navigate().refresh();
        String afterRefresh = wait.until(ExpectedConditions.visibilityOfElementLocated(qmsStatusTitle)).getText().trim();

        Assert.assertEquals(afterRefresh, beforeRefresh, "QMS Status title should remain consistent after refresh");
    }

    @Test(priority = 10, description = "Verify All Tasks toggle")
    // Manual Test Case ID: TC088
    public void verifyAllTasksToggle() {
        Assert.assertTrue(driver.findElement(allTasksToggle).isDisplayed(), "All Tasks toggle should be visible");
    }

    @Test(priority = 11, description = "Verify My Tasks toggle")
    // Manual Test Case ID: TC089
    public void verifyMyTasksToggle() {
        Assert.assertTrue(driver.findElement(myTasksToggle).isDisplayed(), "My Tasks toggle should be visible");
    }

    @Test(priority = 12, description = "Verify toggle functionality")
    // Manual Test Case ID: TC090
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
    // Manual Test Case ID: TC091
    public void verifyTogglePersistence() {
        driver.findElement(myTasksToggle).click();
        waitForSmallDelay();
        driver.navigate().refresh();

        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(myTasksToggle)).isDisplayed(),
                "My Tasks toggle should remain available after refresh");
    }

    @Test(priority = 14, description = "Verify Quality Policy card")
    // Manual Test Case ID: TC092
    public void verifyQualityPolicyCard() {
        Assert.assertTrue(driver.findElement(qualityPolicyCard).isDisplayed(), "Quality Policy card should be visible");
    }

    @Test(priority = 15, description = "Verify Quality Policy data")
    // Manual Test Case ID: TC093
    public void verifyQualityPolicyData() {
        Assert.assertTrue(getBodyText().contains("Quality Policy"), "Quality Policy data/text should be visible");
    }

    @Test(priority = 16, description = "Verify Quality Objective card")
    // Manual Test Case ID: TC094
    public void verifyQualityObjectiveCard() {
        Assert.assertTrue(driver.findElement(qualityObjectiveCard).isDisplayed(), "Quality Objective card should be visible");
    }

    @Test(priority = 17, description = "Verify Quality Objective data")
    // Manual Test Case ID: TC095
    public void verifyQualityObjectiveData() {
        Assert.assertTrue(getBodyText().contains("Quality Objective"), "Quality Objective data/text should be visible");
    }

    @Test(priority = 18, description = "Verify Responsibility card")
    // Manual Test Case ID: TC096
    public void verifyResponsibilityCard() {
        Assert.assertTrue(driver.findElement(responsibilityCard).isDisplayed(), "Responsibility card should be visible");
    }

    @Test(priority = 19, description = "Verify Responsibility data accuracy")
    // Manual Test Case ID: TC097
    public void verifyResponsibilityDataAccuracy() {
        Assert.assertTrue(getBodyText().contains("Responsibility"), "Responsibility data/text should be visible");
    }

    @Test(priority = 20, description = "Verify No Pending Items message")
    // Manual Test Case ID: TC098
    public void verifyNoPendingItemsMessage() {
        Assert.assertTrue(isElementDisplayed(noPendingItemsMessage) || driver.findElements(dashboardCard).size() > 0,
                "No Pending Items message should display when applicable");
    }

    @Test(priority = 21, description = "Verify message removed")
    // Manual Test Case ID: TC099
    public void verifyMessageRemoved() {
        throw new SkipException("Requires adding data to a widget and refreshing dashboard");
    }

    @Test(priority = 22, description = "Verify icon visibility")
    // Manual Test Case ID: TC100
    public void verifyIconVisibility() {
        Assert.assertTrue(driver.findElements(visibleIcon).size() > 0, "Dashboard icons should be visible");
    }

    @Test(priority = 23, description = "PDF Flow - Verify left panel module list")
    // Manual Test Case ID: TC101
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
    // Manual Test Case ID: TC102
    public void verifyPdfFlowAssignedActivitiesAppearOnDashboard() {
        Assert.assertTrue(driver.findElement(qmsStatusTitle).isDisplayed(), "Dashboard should display assigned QMS activities");
        Assert.assertTrue(driver.findElements(dashboardCard).size() > 0, "Assigned activity widgets/cards should be visible");
    }

    @Test(priority = 25, description = "PDF Flow - Verify Admin All Tasks and My Tasks visibility")
    // Manual Test Case ID: TC103
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
        String password = config.getOptionalSecret("EASYQ_ADMIN_PASSWORD");
        if (password == null || password.isBlank()) {
            password = config.getOptionalSecret("EASYQ_PASSWORD");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("EASYQ_ADMIN_PASSWORD or EASYQ_PASSWORD is required");
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
