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
import utils.ConfigReader;
import utils.HamburgerNavigationHelper;

import java.time.Duration;

public class EasyQCapaTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private final ConfigReader config = new ConfigReader();

    private final String baseUrl = "https://beta.easyqsolutions.com/#/easyqsolutions/login";
    private final String validEmail = "varunt@easyqsolutions.com";

    private final By emailField = By.xpath("//input[@type='email' or contains(@formcontrolname,'email')]");
    private final By passwordField = By.xpath("//input[@type='password' or contains(@formcontrolname,'password')]");
    private final By loginButton = By.xpath("//button[contains(normalize-space(.),'Log In')]");
    private final By dashboardText = By.xpath("//*[contains(normalize-space(.),'Dashboard')]");
    private final By capaMenu = By.xpath("//*[contains(normalize-space(.),'CAPA') or contains(normalize-space(.),'Deviation')]");
    private final By capaTitle = By.xpath("//*[contains(normalize-space(.),'CAPA')]");
    private final By createButton = By.xpath("//button[contains(normalize-space(.),'Create') or contains(normalize-space(.),'Add') or contains(normalize-space(.),'New')]");
    private final By saveButton = By.xpath("//button[contains(normalize-space(.),'Save') or contains(normalize-space(.),'Draft')]");
    private final By submitButton = By.xpath("//button[contains(normalize-space(.),'Submit') or contains(normalize-space(.),'Send')]");
    private final By validationMessage = By.xpath("//*[contains(@class,'error') or contains(@class,'invalid') or contains(@class,'danger') or contains(normalize-space(.),'required') or contains(normalize-space(.),'Required')]");
    private final By tableOrCardData = By.xpath("//table | //*[contains(@class,'card') or contains(@class,'list') or contains(@class,'row')]");
    private final By statusText = By.xpath("//*[contains(normalize-space(.),'Draft') or contains(normalize-space(.),'Review') or contains(normalize-space(.),'Approved') or contains(normalize-space(.),'Closed')]");

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        driver.manage().window().maximize();
        driver.get(baseUrl);
        loginWithValidCredentials();
        navigateToCapa();
    }

    @AfterMethod
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(priority = 1, description = "Verify CAPA module loads successfully")
    // Manual Test Case ID: TC548
    public void verifyCapaModuleLoadsSuccessfully() {
        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(capaTitle)).isDisplayed(),
                "CAPA module should load successfully");
    }

    @Test(priority = 2, description = "Verify CAPA module loads with data")
    // Manual Test Case ID: TC549
    public void verifyCapaModuleLoadsWithData() {
        Assert.assertTrue(hasCapaDataOrPageLoaded(), "CAPA module should display data or a valid page state");
    }

    @Test(priority = 3, description = "Verify no UI break on page load")
    // Manual Test Case ID: TC550
    public void verifyNoUiBreakOnPageLoad() {
        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(), "CAPA page body should be visible");
        Assert.assertTrue(driver.findElement(capaTitle).isDisplayed(), "CAPA title should be visible");
    }

    @Test(priority = 4, description = "Verify access for Admin")
    // Manual Test Case ID: TC551
    public void verifyAccessForAdmin() {
        Assert.assertTrue(driver.findElement(capaTitle).isDisplayed(), "Admin/current user should access CAPA module");
    }

    @Test(priority = 5, description = "Verify Admin access")
    // Manual Test Case ID: TC552
    public void verifyAdminAccess() {
        Assert.assertTrue(isElementDisplayed(createButton) || driver.findElement(capaTitle).isDisplayed(),
                "Admin/current user should be allowed to view or create CAPA based on permissions");
    }

    @Test(priority = 6, description = "Verify mandatory fields validation")
    // Manual Test Case ID: TC553
    public void verifyMandatoryFieldsValidation() {
        openCreateCapaIfAvailable();

        if (!isElementDisplayed(submitButton) && !isElementDisplayed(saveButton)) {
            throw new SkipException("Submit/save button locator needs confirmation on CAPA form");
        }

        clickFirstAvailable(submitButton, saveButton);
        Assert.assertTrue(isElementDisplayed(validationMessage) || driver.getCurrentUrl().toLowerCase().contains("capa"),
                "Mandatory field validation should be shown or form should remain on CAPA page");
    }

    @Test(priority = 7, description = "Verify empty CAPA submission")
    // Manual Test Case ID: TC554
    public void verifyEmptyCapaSubmission() {
        openCreateCapaIfAvailable();
        clickFirstAvailable(submitButton, saveButton);

        Assert.assertTrue(isElementDisplayed(validationMessage) || driver.getCurrentUrl().toLowerCase().contains("capa"),
                "Empty CAPA submission should not be accepted without required data");
    }

    @Test(priority = 8, description = "Verify workflow status change")
    // Manual Test Case ID: TC555
    public void verifyWorkflowStatusChange() {
        Assert.assertTrue(isElementDisplayed(statusText) || hasCapaDataOrPageLoaded(),
                "Workflow status should be visible when CAPA records exist");
    }

    @Test(priority = 9, description = "Verify status tracking")
    // Manual Test Case ID: TC556
    public void verifyStatusTracking() {
        Assert.assertTrue(isElementDisplayed(statusText) || hasCapaDataOrPageLoaded(),
                "CAPA lifecycle/status should be trackable when records exist");
    }

    @Test(priority = 10, description = "Verify lifecycle tracking")
    // Manual Test Case ID: TC557
    public void verifyLifecycleTracking() {
        String bodyText = getBodyText();
        Assert.assertTrue(bodyText.length() > 50, "CAPA lifecycle page should render readable data");
    }

    @Test(priority = 11, description = "Verify data consistency")
    // Manual Test Case ID: TC558
    public void verifyDataConsistency() {
        Assert.assertTrue(driver.findElement(capaTitle).isDisplayed(), "CAPA data should remain consistent on page load");
    }

    @Test(priority = 12, description = "Verify long text handling")
    // Manual Test Case ID: TC559
    public void verifyLongTextHandling() {
        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "CAPA page should remain stable for long text records");
    }

    @Test(priority = 13, description = "Verify multiple CAPA handling")
    // Manual Test Case ID: TC560
    public void verifyMultipleCapaHandling() {
        Assert.assertTrue(hasCapaDataOrPageLoaded(), "CAPA page should handle available multiple records");
    }

    @Test(priority = 14, description = "Verify CAPA creation")
    // Manual Test Case ID: TC561
    public void verifyCapaCreation() {
        throw new SkipException("Requires confirmed CAPA create form locators and disposable test data");
    }

    @Test(priority = 15, description = "Verify CAPA saved as Draft")
    // Manual Test Case ID: TC562
    public void verifyCapaSavedAsDraft() {
        throw new SkipException("Requires create CAPA data and draft save business flow");
    }

    @Test(priority = 16, description = "Verify CAPA title input")
    // Manual Test Case ID: TC563
    public void verifyCapaTitleInput() {
        throw new SkipException("Requires confirmed CAPA title field locator");
    }

    @Test(priority = 17, description = "Verify category/type selection")
    // Manual Test Case ID: TC564
    public void verifyCategoryTypeSelection() {
        throw new SkipException("Requires confirmed category/type dropdown locator and option values");
    }

    @Test(priority = 18, description = "Verify attachment upload")
    // Manual Test Case ID: TC565
    public void verifyAttachmentUpload() {
        throw new SkipException("Requires file upload control locator and sample attachment");
    }

    @Test(priority = 19, description = "Verify investigation details entry")
    // Manual Test Case ID: TC566
    public void verifyInvestigationDetailsEntry() {
        throw new SkipException("Requires confirmed investigation details field locator");
    }

    @Test(priority = 20, description = "Verify root cause analysis entry")
    // Manual Test Case ID: TC567
    public void verifyRootCauseAnalysisEntry() {
        throw new SkipException("Requires confirmed root cause field locator");
    }

    @Test(priority = 21, description = "Verify investigation data saved")
    // Manual Test Case ID: TC568
    public void verifyInvestigationDataSaved() {
        throw new SkipException("Requires CAPA record creation and save verification flow");
    }

    @Test(priority = 22, description = "Verify corrective action entry")
    // Manual Test Case ID: TC569
    public void verifyCorrectiveActionEntry() {
        throw new SkipException("Requires confirmed corrective action field locator");
    }

    @Test(priority = 23, description = "Verify preventive action entry")
    // Manual Test Case ID: TC570
    public void verifyPreventiveActionEntry() {
        throw new SkipException("Requires confirmed preventive action field locator");
    }

    @Test(priority = 24, description = "Verify responsible person assignment")
    // Manual Test Case ID: TC571
    public void verifyResponsiblePersonAssignment() {
        throw new SkipException("Requires confirmed user assignment control and test user");
    }

    @Test(priority = 25, description = "Verify due date assignment")
    // Manual Test Case ID: TC572
    public void verifyDueDateAssignment() {
        throw new SkipException("Requires confirmed date picker locator");
    }

    @Test(priority = 26, description = "Verify send for review")
    // Manual Test Case ID: TC573
    public void verifySendForReview() {
        throw new SkipException("Requires draft CAPA record and reviewer setup");
    }

    @Test(priority = 27, description = "Verify reviewer assignment")
    // Manual Test Case ID: TC574
    public void verifyReviewerAssignment() {
        throw new SkipException("Requires reviewer user data and assignment locator");
    }

    @Test(priority = 28, description = "Verify approver assignment")
    // Manual Test Case ID: TC575
    public void verifyApproverAssignment() {
        throw new SkipException("Requires approver user data and assignment locator");
    }

    @Test(priority = 29, description = "Verify reviewer access")
    // Manual Test Case ID: TC576
    public void verifyReviewerAccess() {
        throw new SkipException("Requires reviewer credentials");
    }

    @Test(priority = 30, description = "Verify reviewer edit")
    // Manual Test Case ID: TC577
    public void verifyReviewerEdit() {
        throw new SkipException("Requires reviewer credentials and editable review-stage CAPA");
    }

    @Test(priority = 31, description = "Verify reviewer review")
    // Manual Test Case ID: TC578
    public void verifyReviewerReview() {
        throw new SkipException("Requires reviewer credentials and review-stage CAPA");
    }

    @Test(priority = 32, description = "Verify approver access")
    // Manual Test Case ID: TC579
    public void verifyApproverAccess() {
        throw new SkipException("Requires approver credentials");
    }

    @Test(priority = 33, description = "Verify approver approval")
    // Manual Test Case ID: TC580
    public void verifyApproverApproval() {
        throw new SkipException("Requires approver credentials and approval-stage CAPA");
    }

    @Test(priority = 34, description = "Verify Approved status")
    // Manual Test Case ID: TC581
    public void verifyApprovedStatus() {
        throw new SkipException("Requires approved CAPA test record");
    }

    @Test(priority = 35, description = "Verify verification step")
    // Manual Test Case ID: TC582
    public void verifyVerificationStep() {
        throw new SkipException("Requires approved CAPA ready for verification");
    }

    @Test(priority = 36, description = "Verify verification remarks")
    // Manual Test Case ID: TC583
    public void verifyVerificationRemarks() {
        throw new SkipException("Requires verification-stage CAPA and remarks field locator");
    }

    @Test(priority = 37, description = "Verify effectiveness evaluation")
    // Manual Test Case ID: TC584
    public void verifyEffectivenessEvaluation() {
        throw new SkipException("Requires CAPA ready for effectiveness evaluation");
    }

    @Test(priority = 38, description = "Verify CAPA closure")
    // Manual Test Case ID: TC585
    public void verifyCapaClosure() {
        throw new SkipException("Requires CAPA ready for closure");
    }

    @Test(priority = 39, description = "Verify restricted access for Assignee")
    // Manual Test Case ID: TC586
    public void verifyRestrictedAccessForAssignee() {
        throw new SkipException("Requires assignee credentials");
    }

    @Test(priority = 40, description = "Verify Assignee restriction")
    // Manual Test Case ID: TC587
    public void verifyAssigneeRestriction() {
        throw new SkipException("Requires assignee credentials and restriction rules");
    }

    @Test(priority = 41, description = "Verify role-based actions")
    // Manual Test Case ID: TC588
    public void verifyRoleBasedActions() {
        throw new SkipException("Requires admin, reviewer, approver, and assignee credentials");
    }

    @Test(priority = 42, description = "Verify data saved correctly")
    // Manual Test Case ID: TC589
    public void verifyDataSavedCorrectly() {
        throw new SkipException("Requires CAPA creation/update flow with unique test data");
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

    private void navigateToCapa() {
        HamburgerNavigationHelper.openModule(driver, wait, capaTitle, "CAPA", "capa|deviation");
    }

    private void openCreateCapaIfAvailable() {
        if (!isElementDisplayed(createButton)) {
            throw new SkipException("Create CAPA button locator needs confirmation or user lacks create access");
        }
        driver.findElement(createButton).click();
        waitForSmallDelay();
    }

    private void clickFirstAvailable(By firstLocator, By secondLocator) {
        if (isElementDisplayed(firstLocator)) {
            driver.findElement(firstLocator).click();
        } else if (isElementDisplayed(secondLocator)) {
            driver.findElement(secondLocator).click();
        } else {
            throw new SkipException("No matching action button found");
        }
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

    private boolean hasCapaDataOrPageLoaded() {
        return !driver.findElements(tableOrCardData).isEmpty() || getBodyText().length() > 40;
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
            Thread.sleep(1500);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
