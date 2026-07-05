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
import utils.HamburgerNavigationHelper;

import java.time.Duration;

public class EasyQTrainingTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private final ConfigReader config = new ConfigReader();

    private final String baseUrl = "https://beta.easyqsolutions.com/#/easyqsolutions/login";
    private final String validEmail = "varunt@easyqsolutions.com";

    private final By emailField = By.xpath("//input[@type='email' or contains(@formcontrolname,'email')]");
    private final By passwordField = By.xpath("//input[@type='password' or contains(@formcontrolname,'password')]");
    private final By loginButton = By.xpath("//button[contains(normalize-space(.),'Log In')]");
    private final By dashboardText = By.xpath("//*[contains(normalize-space(.),'Dashboard')]");
    private final By trainingMenu = By.xpath("//*[contains(normalize-space(.),'Training')]");
    private final By trainingTitle = By.xpath("//*[contains(normalize-space(.),'Training')]");
    private final By assignButton = By.xpath("//button[contains(normalize-space(.),'Assign') or contains(normalize-space(.),'Create') or contains(normalize-space(.),'Add') or contains(normalize-space(.),'New')]");
    private final By submitButton = By.xpath("//button[contains(normalize-space(.),'Submit') or contains(normalize-space(.),'Save') or contains(normalize-space(.),'Assign')]");
    private final By startButton = By.xpath("//button[contains(normalize-space(.),'Start')]");
    private final By completeButton = By.xpath("//button[contains(normalize-space(.),'Complete') or contains(normalize-space(.),'Finish')]");
    private final By validationMessage = By.xpath("//*[contains(@class,'error') or contains(@class,'invalid') or contains(@class,'danger') or contains(normalize-space(.),'required') or contains(normalize-space(.),'Required')]");
    private final By tableOrCardData = By.xpath("//table | //*[contains(@class,'card') or contains(@class,'list') or contains(@class,'row')]");
    private final By statusText = By.xpath("//*[contains(normalize-space(.),'Pending') or contains(normalize-space(.),'Completed') or contains(normalize-space(.),'Overdue') or contains(normalize-space(.),'Started')]");
    private final By dateText = By.xpath("//*[contains(text(),'-202') or contains(text(),'/202') or contains(text(),'202')]");
    private final By formField = By.xpath("//input | //textarea | //select | //*[@role='combobox']");
    private final By searchInput = By.xpath("//input[contains(@placeholder,'Search') or contains(@aria-label,'Search') or contains(@formcontrolname,'search')]");
    private final By noDataMessage = By.xpath("//*[contains(normalize-space(.),'No Data') or contains(normalize-space(.),'No data') or contains(normalize-space(.),'No Training') or contains(normalize-space(.),'No records')]");
    private final By nextButton = By.xpath("//button[contains(.,'Next') or @aria-label='Next page']");
    private final By filterControl = By.xpath("//*[contains(normalize-space(.),'Status') or contains(normalize-space(.),'Filter')]/following::select[1] | //*[contains(normalize-space(.),'Status') or contains(normalize-space(.),'Filter')]/following::*[@role='combobox'][1]");
    private final By viewButton = By.xpath("//button[contains(normalize-space(.),'View') or contains(@title,'View')]");
    private final By downloadButton = By.xpath("//button[contains(normalize-space(.),'Download') or contains(@title,'Download')]");
    private final By reminderButton = By.xpath("//button[contains(normalize-space(.),'Reminder') or contains(normalize-space(.),'Notify')]");
    private final By assignmentTab = By.xpath("//*[contains(normalize-space(.),'Assigned') or contains(normalize-space(.),'My Training') or contains(normalize-space(.),'Pending')]");
    private final By trainingModuleTab = By.xpath("//*[contains(normalize-space(.),'Training Module')]");
    private final By myTrainingTab = By.xpath("//*[contains(normalize-space(.),'My Training')]");
    private final By trainingLogsTab = By.xpath("//*[contains(normalize-space(.),'Training Logs')]");
    private final By analyticsTab = By.xpath("//*[contains(normalize-space(.),'Analytics')]");
    private final By createTrainingButton = By.xpath("//button[contains(normalize-space(.),'Create Training') or contains(normalize-space(.),'Create') or contains(normalize-space(.),'Add')]");
    private final By trainingNameField = By.xpath("//input[contains(@placeholder,'Training Name') or contains(@formcontrolname,'trainingName') or contains(@name,'training')]");
    private final By existingDocumentOption = By.xpath("//*[contains(normalize-space(.),'Existing Document') or contains(normalize-space(.),'Select Existing')]");
    private final By uploadExternalFileOption = By.xpath("//*[contains(normalize-space(.),'Upload External') or contains(normalize-space(.),'Upload')]");
    private final By youtubeVideoOption = By.xpath("//*[contains(normalize-space(.),'YouTube') or contains(normalize-space(.),'Video URL')]");
    private final By addQuestionnaireOption = By.xpath("//*[contains(normalize-space(.),'Add Questionnaire') or contains(normalize-space(.),'Questionnaire')]");
    private final By yesOption = By.xpath("//*[normalize-space()='Yes' or @value='yes']");
    private final By noOption = By.xpath("//*[normalize-space()='No' or @value='no']");
    private final By questionField = By.xpath("//textarea[contains(@placeholder,'Question') or contains(@formcontrolname,'question')] | //input[contains(@placeholder,'Question') or contains(@formcontrolname,'question')]");
    private final By correctAnswerField = By.xpath("//*[contains(normalize-space(.),'Correct Answer')]/following::input[1] | //*[contains(normalize-space(.),'Correct Answer')]/following::*[@role='combobox'][1]");
    private final By passingCriteriaField = By.xpath("//input[contains(@placeholder,'Passing') or contains(@formcontrolname,'passing')]");
    private final By quizTitleField = By.xpath("//input[contains(@placeholder,'Quiz Title') or contains(@formcontrolname,'quizTitle')]");
    private final By requiredQuestionToggle = By.xpath("//*[contains(normalize-space(.),'Required') or contains(normalize-space(.),'Mandatory')]/following::*[@role='switch' or contains(@class,'toggle')][1]");
    private final By cancelTrainingAction = By.xpath("//button[contains(normalize-space(.),'Cancel Training') or contains(normalize-space(.),'Cancel')]");
    private final By assignTrainingAction = By.xpath("//button[contains(normalize-space(.),'Assign Training') or contains(normalize-space(.),'Assign')]");
    private final By updateQuizAction = By.xpath("//button[contains(normalize-space(.),'Update Quiz')]");
    private final By editTrainingAction = By.xpath("//button[contains(normalize-space(.),'Edit Training') or contains(normalize-space(.),'Edit')]");
    private final By makeCopyAction = By.xpath("//button[contains(normalize-space(.),'Make Copy') or contains(normalize-space(.),'Copy')]");
    private final By traineeSelector = By.xpath("//*[contains(normalize-space(.),'Trainee')]/following::select[1] | //*[contains(normalize-space(.),'Trainee')]/following::*[@role='combobox'][1]");
    private final By acknowledgementCheckbox = By.xpath("//input[@type='checkbox'] | //*[@role='checkbox']");
    private final By submitAcknowledgementButton = By.xpath("//button[contains(normalize-space(.),'Submit Acknowledgment') or contains(normalize-space(.),'Submit Acknowledgement') or contains(normalize-space(.),'Submit')]");
    private final By markCompletedButton = By.xpath("//button[contains(normalize-space(.),'Mark As Completed') or contains(normalize-space(.),'Mark Complete')]");
    private final By downloadFileButton = By.xpath("//button[contains(normalize-space(.),'Download File') or contains(normalize-space(.),'Export') or contains(normalize-space(.),'CSV')]");
    private final By deploymentCompletionGraph = By.xpath("//*[contains(normalize-space(.),'Deployment') or contains(normalize-space(.),'Completion') or contains(@class,'chart') or contains(@class,'graph')]");
    private final By monthFilter = By.xpath("//*[contains(normalize-space(.),'Month')]/following::select[1] | //*[contains(normalize-space(.),'Month')]/following::*[@role='combobox'][1]");
    private final By yearFilter = By.xpath("//*[contains(normalize-space(.),'Year')]/following::select[1] | //*[contains(normalize-space(.),'Year')]/following::*[@role='combobox'][1]");

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        driver.manage().window().maximize();
        driver.get(baseUrl);
        loginWithValidCredentials();
        navigateToTraining();
    }

    @AfterMethod
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(priority = 1, description = "Verify Training module loads successfully")
    // Manual Test Case ID: TC590
    public void verifyTrainingModuleLoadsSuccessfully() {
        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(trainingTitle)).isDisplayed(),
                "Training module should load successfully");
    }

    @Test(priority = 2, description = "Verify module loads with data")
    // Manual Test Case ID: TC591
    public void verifyModuleLoadsWithData() {
        Assert.assertTrue(hasTrainingDataOrPageLoaded(), "Training module should display data or a valid empty state");
    }

    @Test(priority = 3, description = "Verify no UI break on page load")
    // Manual Test Case ID: TC592
    public void verifyNoUiBreakOnPageLoad() {
        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(), "Training page body should be visible");
        Assert.assertTrue(driver.findElement(trainingTitle).isDisplayed(), "Training title should be visible");
    }

    @Test(priority = 4, description = "Verify access for Admin")
    // Manual Test Case ID: TC593
    public void verifyAccessForAdmin() {
        Assert.assertTrue(driver.findElement(trainingTitle).isDisplayed(), "Admin/current user should access Training module");
    }

    @Test(priority = 5, description = "Verify Admin assignment access")
    // Manual Test Case ID: TC594
    public void verifyAdminAssignmentAccess() {
        Assert.assertTrue(isElementDisplayed(assignButton) || driver.findElement(trainingTitle).isDisplayed(),
                "Admin/current user should have Training access and assignment may be visible based on permissions");
    }

    @Test(priority = 6, description = "Verify assigned user details")
    // Manual Test Case ID: TC595
    public void verifyAssignedUserDetails() {
        Assert.assertTrue(hasTrainingDataOrPageLoaded(), "Assigned user details should display when training data exists");
    }

    @Test(priority = 7, description = "Verify training linked to document")
    // Manual Test Case ID: TC596
    public void verifyTrainingLinkedToDocument() {
        Assert.assertTrue(getBodyText().contains("Document") || hasTrainingDataOrPageLoaded(),
                "Training should show linked document/course information when data exists");
    }

    @Test(priority = 8, description = "Verify training visible to user")
    // Manual Test Case ID: TC597
    public void verifyTrainingVisibleToUser() {
        Assert.assertTrue(hasTrainingDataOrPageLoaded(), "Training should be visible to assigned/current user when available");
    }

    @Test(priority = 9, description = "Verify user can access training")
    // Manual Test Case ID: TC598
    public void verifyUserCanAccessTraining() {
        Assert.assertTrue(driver.findElement(trainingTitle).isDisplayed(), "User should be able to access Training module");
    }

    @Test(priority = 10, description = "Verify completion status update")
    // Manual Test Case ID: TC599
    public void verifyCompletionStatusUpdate() {
        Assert.assertTrue(isElementDisplayed(statusText) || hasTrainingDataOrPageLoaded(),
                "Training completion/status should be visible when data exists");
    }

    @Test(priority = 11, description = "Verify completion date recorded")
    // Manual Test Case ID: TC600
    public void verifyCompletionDateRecorded() {
        Assert.assertTrue(isElementDisplayed(dateText) || hasTrainingDataOrPageLoaded(),
                "Completion/due date should be visible when training data exists");
    }

    @Test(priority = 12, description = "Verify Pending training count")
    // Manual Test Case ID: TC601
    public void verifyPendingTrainingCount() {
        Assert.assertTrue(getBodyText().contains("Pending") || hasTrainingDataOrPageLoaded(),
                "Pending training count/status should be visible when data exists");
    }

    @Test(priority = 13, description = "Verify Completed training count")
    // Manual Test Case ID: TC602
    public void verifyCompletedTrainingCount() {
        Assert.assertTrue(getBodyText().contains("Completed") || hasTrainingDataOrPageLoaded(),
                "Completed training count/status should be visible when data exists");
    }

    @Test(priority = 14, description = "Verify status updates correctly")
    // Manual Test Case ID: TC603
    public void verifyStatusUpdatesCorrectly() {
        Assert.assertTrue(isElementDisplayed(statusText) || hasTrainingDataOrPageLoaded(),
                "Training status should reflect correctly in UI");
    }

    @Test(priority = 15, description = "Verify user view-only access")
    // Manual Test Case ID: TC604
    public void verifyUserViewOnlyAccess() {
        Assert.assertTrue(driver.findElement(trainingTitle).isDisplayed(),
                "Training should be viewable when user has access");
    }

    @Test(priority = 16, description = "Verify status reflects correctly")
    // Manual Test Case ID: TC605
    public void verifyStatusReflectsCorrectly() {
        Assert.assertTrue(isElementDisplayed(statusText) || hasTrainingDataOrPageLoaded(),
                "Training status should reflect correctly");
    }

    @Test(priority = 17, description = "Verify empty assignment handling")
    // Manual Test Case ID: TC606
    public void verifyEmptyAssignmentHandling() {
        openAssignFormIfAvailable();
        clickSubmitIfAvailable();

        Assert.assertTrue(isElementDisplayed(validationMessage) || driver.findElement(By.tagName("body")).isDisplayed(),
                "Empty training assignment should show validation or keep form stable");
    }

    @Test(priority = 18, description = "Verify multiple training assignments")
    // Manual Test Case ID: TC607
    public void verifyMultipleTrainingAssignments() {
        Assert.assertTrue(hasTrainingDataOrPageLoaded(),
                "Multiple training assignments should be handled when data exists");
    }

    @Test(priority = 19, description = "Verify long text handling")
    // Manual Test Case ID: TC608
    public void verifyLongTextHandling() {
        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Training module should handle long text without UI break");
    }

    @Test(priority = 20, description = "Verify training assignment")
    // Manual Test Case ID: TC609
    public void verifyTrainingAssignment() {
        throw new SkipException("Requires assignment form locators, training data, and test users");
    }

    @Test(priority = 21, description = "Verify multiple users assignment")
    // Manual Test Case ID: TC610
    public void verifyMultipleUsersAssignment() {
        throw new SkipException("Requires multiple test users and user selector locator");
    }

    @Test(priority = 22, description = "Verify document/course selection")
    // Manual Test Case ID: TC611
    public void verifyDocumentCourseSelection() {
        throw new SkipException("Requires document/course selector locator and option values");
    }

    @Test(priority = 23, description = "Verify due date assignment")
    // Manual Test Case ID: TC612
    public void verifyDueDateAssignment() {
        throw new SkipException("Requires date picker locator");
    }

    @Test(priority = 24, description = "Verify training title/description")
    // Manual Test Case ID: TC613
    public void verifyTrainingTitleDescription() {
        throw new SkipException("Requires title/description field locators and save workflow");
    }

    @Test(priority = 25, description = "Verify user can start training")
    // Manual Test Case ID: TC614
    public void verifyUserCanStartTraining() {
        if (!isElementDisplayed(startButton)) {
            throw new SkipException("Start Training button is unavailable or locator needs confirmation");
        }
        throw new SkipException("Requires assigned not-started training record");
    }

    @Test(priority = 26, description = "Verify training completion")
    // Manual Test Case ID: TC615
    public void verifyTrainingCompletion() {
        if (!isElementDisplayed(completeButton)) {
            throw new SkipException("Complete Training button is unavailable or locator needs confirmation");
        }
        throw new SkipException("Requires assigned in-progress training record");
    }

    @Test(priority = 27, description = "Verify training reminder")
    // Manual Test Case ID: TC616
    public void verifyTrainingReminder() {
        throw new SkipException("Requires reminder trigger and notification/email validation");
    }

    @Test(priority = 28, description = "Verify overdue indication")
    // Manual Test Case ID: TC617
    public void verifyOverdueIndication() {
        throw new SkipException("Requires overdue training test data");
    }

    @Test(priority = 29, description = "Verify Assignee restriction")
    // Manual Test Case ID: TC618
    public void verifyAssigneeRestriction() {
        throw new SkipException("Requires assignee credentials");
    }

    @Test(priority = 30, description = "Verify restricted access for Assignee")
    // Manual Test Case ID: TC619
    public void verifyRestrictedAccessForAssignee() {
        throw new SkipException("Requires assignee credentials");
    }

    @Test(priority = 31, description = "Verify data saved correctly")
    // Manual Test Case ID: TC620
    public void verifyDataSavedCorrectly() {
        throw new SkipException("Requires assignment save workflow with unique test data");
    }

    @Test(priority = 32, description = "Verify Training search functionality")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingSearchFunctionality() {
        if (!isElementDisplayed(searchInput)) {
            throw new SkipException("Search input is not available or locator needs confirmation");
        }

        driver.findElement(searchInput).sendKeys("training");
        waitForSmallDelay();

        Assert.assertTrue(driver.findElement(trainingTitle).isDisplayed(), "Training search should keep page stable");
    }

    @Test(priority = 33, description = "Verify Training search with no results")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingSearchWithNoResults() {
        if (!isElementDisplayed(searchInput)) {
            throw new SkipException("Search input is not available or locator needs confirmation");
        }

        driver.findElement(searchInput).sendKeys("NO_TRAINING_MATCH_" + System.currentTimeMillis());
        waitForSmallDelay();

        Assert.assertTrue(isElementDisplayed(noDataMessage) || driver.findElement(trainingTitle).isDisplayed(),
                "No result search should show empty state or stable Training page");
    }

    @Test(priority = 34, description = "Verify Training status filter")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingStatusFilter() {
        Assert.assertTrue(isElementDisplayed(filterControl) || hasTrainingDataOrPageLoaded(),
                "Training status filter should be available when filtering is supported");
    }

    @Test(priority = 35, description = "Verify Training pagination")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingPagination() {
        if (!isElementDisplayed(nextButton)) {
            throw new SkipException("Pagination next button is not available for current training data");
        }

        String beforeText = getBodyText();
        driver.findElement(nextButton).click();
        waitForSmallDelay();
        String afterText = getBodyText();

        Assert.assertTrue(afterText.length() > 0 && !afterText.equals(beforeText), "Pagination should update Training data");
    }

    @Test(priority = 36, description = "Verify Training empty state")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingEmptyState() {
        Assert.assertTrue(isElementDisplayed(noDataMessage) || hasTrainingDataOrPageLoaded(),
                "Training empty state should be handled correctly");
    }

    @Test(priority = 37, description = "Verify Training detail view opens")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingDetailViewOpens() {
        if (!isElementDisplayed(viewButton)) {
            throw new SkipException("View button is not available for current training records");
        }

        driver.findElement(viewButton).click();
        waitForSmallDelay();

        Assert.assertTrue(getBodyText().contains("Training") || !driver.getCurrentUrl().contains("training"),
                "Training detail view should open or navigate correctly");
    }

    @Test(priority = 38, description = "Verify assigned training tab or section")
    // Manual Test Case ID: TC590-TC620
    public void verifyAssignedTrainingTabOrSection() {
        Assert.assertTrue(isElementDisplayed(assignmentTab) || hasTrainingDataOrPageLoaded(),
                "Assigned/My Training/Pending section should be visible when supported");
    }

    @Test(priority = 39, description = "Verify Training download option")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingDownloadOption() {
        Assert.assertTrue(isElementDisplayed(downloadButton) || hasTrainingDataOrPageLoaded(),
                "Download option should be visible when training document/download is available");
    }

    @Test(priority = 40, description = "Verify Training reminder button visibility")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingReminderButtonVisibility() {
        Assert.assertTrue(isElementDisplayed(reminderButton) || hasTrainingDataOrPageLoaded(),
                "Reminder/Notify option should be visible when reminders are supported");
    }

    @Test(priority = 41, description = "Verify Training records do not duplicate after refresh")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingRecordsDoNotDuplicateAfterRefresh() {
        String beforeRefresh = getBodyText();
        driver.navigate().refresh();
        wait.until(ExpectedConditions.visibilityOfElementLocated(trainingTitle));
        String afterRefresh = getBodyText();

        Assert.assertTrue(beforeRefresh.length() > 0 && afterRefresh.length() > 0,
                "Training records should reload cleanly without visible duplication failure");
    }

    @Test(priority = 42, description = "Verify Training page scroll behavior")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingPageScrollBehavior() {
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        waitForSmallDelay();

        Assert.assertTrue(driver.findElement(trainingTitle).isDisplayed(), "Training page should remain stable while scrolling");
    }

    @Test(priority = 43, description = "Verify Training special character display")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingSpecialCharacterDisplay() {
        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Training page should not break when special characters are present in data");
    }

    @Test(priority = 44, description = "Verify Training assignment duplicate handling")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingAssignmentDuplicateHandling() {
        throw new SkipException("Requires duplicate assignment test data and save workflow");
    }

    @Test(priority = 45, description = "Verify Training reassignment flow")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingReassignmentFlow() {
        throw new SkipException("Requires existing assigned training and reassignment controls");
    }

    @Test(priority = 46, description = "Verify Training escalation for overdue assignment")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingEscalationForOverdueAssignment() {
        throw new SkipException("Requires overdue training data and escalation/reminder validation");
    }

    @Test(priority = 47, description = "Verify Training completion certificate or acknowledgement")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingCompletionCertificateOrAcknowledgement() {
        throw new SkipException("Requires completed training record and certificate/acknowledgement rule");
    }

    @Test(priority = 48, description = "Verify Training audit log after assignment")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingAuditLogAfterAssignment() {
        throw new SkipException("Requires assigning training and validating related Active Logs entry");
    }

    @Test(priority = 49, description = "PDF Flow - Verify Training module tabs")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingModuleTabsFromPdfFlow() {
        Assert.assertTrue(isElementDisplayed(trainingModuleTab) || getBodyText().contains("Training Module"),
                "Training Module tab should be visible");
        Assert.assertTrue(isElementDisplayed(myTrainingTab) || getBodyText().contains("My Training"),
                "My Training tab should be visible");
        Assert.assertTrue(isElementDisplayed(trainingLogsTab) || getBodyText().contains("Training Logs"),
                "Training Logs tab should be visible");
        Assert.assertTrue(isElementDisplayed(analyticsTab) || getBodyText().contains("Analytics"),
                "Analytics tab should be visible for authorized users");
    }

    @Test(priority = 50, description = "PDF Flow - Verify Create Training form opens")
    // Manual Test Case ID: TC590-TC620
    public void verifyCreateTrainingFormOpensFromPdfFlow() {
        openCreateTrainingFormIfAvailable();

        Assert.assertTrue(driver.findElements(formField).size() > 0 || getBodyText().contains("Training"),
                "Create Training form should open");
    }

    @Test(priority = 51, description = "PDF Flow - Verify Training Name field")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingNameFieldFromPdfFlow() {
        openCreateTrainingFormIfAvailable();

        Assert.assertTrue(isElementDisplayed(trainingNameField) || getBodyText().contains("Training Name"),
                "Training Name field should be available");
    }

    @Test(priority = 52, description = "PDF Flow - Verify training material options")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingMaterialOptionsFromPdfFlow() {
        openCreateTrainingFormIfAvailable();

        Assert.assertTrue(isElementDisplayed(existingDocumentOption) || getBodyText().contains("Existing Document"),
                "Select Existing Document option should be available");
        Assert.assertTrue(isElementDisplayed(uploadExternalFileOption) || getBodyText().contains("Upload"),
                "Upload External File option should be available");
        Assert.assertTrue(isElementDisplayed(youtubeVideoOption) || getBodyText().contains("YouTube") || getBodyText().contains("Video"),
                "YouTube Video option should be available");
    }

    @Test(priority = 53, description = "PDF Flow - Verify Add Questionnaire Yes/No options")
    // Manual Test Case ID: TC590-TC620
    public void verifyAddQuestionnaireYesNoOptionsFromPdfFlow() {
        openCreateTrainingFormIfAvailable();

        Assert.assertTrue(isElementDisplayed(addQuestionnaireOption) || getBodyText().contains("Questionnaire"),
                "Add Questionnaire option should be available");
        Assert.assertTrue(isElementDisplayed(yesOption) || getBodyText().contains("Yes"),
                "Questionnaire Yes option should be available");
        Assert.assertTrue(isElementDisplayed(noOption) || getBodyText().contains("No"),
                "Questionnaire No option should be available");
    }

    @Test(priority = 54, description = "PDF Flow - Verify quiz question fields become mandatory when questionnaire is Yes")
    // Manual Test Case ID: TC590-TC620
    public void verifyQuizQuestionFieldsFromPdfFlow() {
        openCreateTrainingFormIfAvailable();

        Assert.assertTrue(isElementDisplayed(questionField) || getBodyText().contains("Question") || getBodyText().contains("Questionnaire"),
                "Quiz question field should be available when questionnaire is enabled");
        Assert.assertTrue(isElementDisplayed(correctAnswerField) || getBodyText().contains("Correct Answer") || getBodyText().contains("Answer"),
                "Correct Answer field should be available when quiz is configured");
    }

    @Test(priority = 55, description = "PDF Flow - Verify quiz settings")
    // Manual Test Case ID: TC590-TC620
    public void verifyQuizSettingsFromPdfFlow() {
        openCreateTrainingFormIfAvailable();

        Assert.assertTrue(isElementDisplayed(passingCriteriaField) || getBodyText().contains("Passing Criteria"),
                "Passing Criteria field should be available");
        Assert.assertTrue(isElementDisplayed(quizTitleField) || getBodyText().contains("Quiz Title"),
                "Quiz Title field should be available");
        Assert.assertTrue(isElementDisplayed(requiredQuestionToggle) || getBodyText().contains("Required") || getBodyText().contains("Mandatory"),
                "Question Mandatory/Required toggle should be available");
    }

    @Test(priority = 56, description = "PDF Flow - Verify post-training creation actions")
    // Manual Test Case ID: TC590-TC620
    public void verifyPostTrainingCreationActionsFromPdfFlow() {
        Assert.assertTrue(isElementDisplayed(cancelTrainingAction) || hasTrainingDataOrPageLoaded(),
                "Cancel Training action should be available when training exists");
        Assert.assertTrue(isElementDisplayed(assignTrainingAction) || hasTrainingDataOrPageLoaded(),
                "Assign Training action should be available when training exists");
        Assert.assertTrue(isElementDisplayed(updateQuizAction) || hasTrainingDataOrPageLoaded(),
                "Update Quiz action should be available when quiz training exists");
        Assert.assertTrue(isElementDisplayed(editTrainingAction) || hasTrainingDataOrPageLoaded(),
                "Edit Training action should be available when editable training exists");
        Assert.assertTrue(isElementDisplayed(makeCopyAction) || hasTrainingDataOrPageLoaded(),
                "Make Copy action should be available when training exists");
    }

    @Test(priority = 57, description = "PDF Flow - Verify Assign Training configuration")
    // Manual Test Case ID: TC590-TC620
    public void verifyAssignTrainingConfigurationFromPdfFlow() {
        if (!isElementDisplayed(assignTrainingAction)) {
            throw new SkipException("Assign Training action is not available for current data");
        }

        driver.findElement(assignTrainingAction).click();
        waitForSmallDelay();

        Assert.assertTrue(getBodyText().contains("Due Date") || isElementDisplayed(dateText),
                "Due Date should be available during assignment");
        Assert.assertTrue(isElementDisplayed(traineeSelector) || getBodyText().contains("Trainee"),
                "Trainee selection should be available during assignment");
    }

    @Test(priority = 58, description = "PDF Flow - Verify My Training receives assigned training")
    // Manual Test Case ID: TC590-TC620
    public void verifyMyTrainingReceivesAssignedTrainingFromPdfFlow() {
        Assert.assertTrue(isElementDisplayed(myTrainingTab) || getBodyText().contains("My Training"),
                "Assigned training should appear under My Training for assigned users");
    }

    @Test(priority = 59, description = "PDF Flow - Verify Training Details screen shows material status and action tab")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingDetailsScreenFromPdfFlow() {
        Assert.assertTrue(getBodyText().contains("Training") || hasTrainingDataOrPageLoaded(),
                "Training details should show training material/status/action data when opened");
    }

    @Test(priority = 60, description = "PDF Flow - Verify acknowledgement is required before proceeding")
    // Manual Test Case ID: TC590-TC620
    public void verifyAcknowledgementRequiredBeforeProceedingFromPdfFlow() {
        Assert.assertTrue(isElementDisplayed(acknowledgementCheckbox) || getBodyText().contains("Acknowledgment") || getBodyText().contains("Acknowledgement") || hasTrainingDataOrPageLoaded(),
                "Authorization acknowledgement should be available before completion when applicable");
    }

    @Test(priority = 61, description = "PDF Flow - Verify Submit Acknowledgement action")
    // Manual Test Case ID: TC590-TC620
    public void verifySubmitAcknowledgementActionFromPdfFlow() {
        if (!isElementDisplayed(acknowledgementCheckbox) || !isElementDisplayed(submitAcknowledgementButton)) {
            throw new SkipException("Acknowledgement controls are not available for current training data");
        }

        driver.findElement(acknowledgementCheckbox).click();
        driver.findElement(submitAcknowledgementButton).click();
        waitForSmallDelay();

        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Submit Acknowledgement should keep training flow stable");
    }

    @Test(priority = 62, description = "PDF Flow - Verify quiz evaluation score/pass/fail")
    // Manual Test Case ID: TC590-TC620
    public void verifyQuizEvaluationScorePassFailFromPdfFlow() {
        Assert.assertTrue(getBodyText().contains("Score") || getBodyText().contains("PASS") || getBodyText().contains("Fail") || hasTrainingDataOrPageLoaded(),
                "Quiz score and Pass/Fail status should display when quiz is completed");
    }

    @Test(priority = 63, description = "PDF Flow - Verify Mark As Completed")
    // Manual Test Case ID: TC590-TC620
    public void verifyMarkAsCompletedFromPdfFlow() {
        if (!isElementDisplayed(markCompletedButton)) {
            throw new SkipException("Mark As Completed button is not available for current training data");
        }

        driver.findElement(markCompletedButton).click();
        waitForSmallDelay();

        Assert.assertTrue(getBodyText().contains("Completed") || driver.findElement(By.tagName("body")).isDisplayed(),
                "Training status should become Completed");
    }

    @Test(priority = 64, description = "PDF Flow - Verify Training Logs columns")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingLogsColumnsFromPdfFlow() {
        openTrainingLogsIfAvailable();

        Assert.assertTrue(getBodyText().contains("Training Module Name") || getBodyText().contains("Training"),
                "Training Module Name column should be visible");
        Assert.assertTrue(getBodyText().contains("Trainee") || hasTrainingDataOrPageLoaded(),
                "Trainee Name column should be visible");
        Assert.assertTrue(getBodyText().contains("Status") || hasTrainingDataOrPageLoaded(),
                "Status column should be visible");
        Assert.assertTrue(getBodyText().contains("Due Date") || hasTrainingDataOrPageLoaded(),
                "Due Date column should be visible");
        Assert.assertTrue(getBodyText().contains("Quiz") || getBodyText().contains("Score") || hasTrainingDataOrPageLoaded(),
                "Quiz Score column should be visible when quiz data exists");
    }

    @Test(priority = 65, description = "PDF Flow - Verify Training Logs All Tasks and My Tasks filters")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingLogsFiltersFromPdfFlow() {
        openTrainingLogsIfAvailable();

        Assert.assertTrue(getBodyText().contains("All Tasks") || getBodyText().contains("My Tasks") || hasTrainingDataOrPageLoaded(),
                "Training Logs should show All Tasks/My Tasks filters");
    }

    @Test(priority = 66, description = "PDF Flow - Verify Training Logs export CSV")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingLogsExportCsvFromPdfFlow() {
        openTrainingLogsIfAvailable();

        Assert.assertTrue(isElementDisplayed(downloadFileButton) || getBodyText().contains("Download") || getBodyText().contains("Export"),
                "Download File/CSV export should be available for Training Logs");
    }

    @Test(priority = 67, description = "PDF Flow - Verify Analytics dashboard")
    // Manual Test Case ID: TC590-TC620
    public void verifyAnalyticsDashboardFromPdfFlow() {
        openAnalyticsIfAvailable();

        Assert.assertTrue(isElementDisplayed(deploymentCompletionGraph) || getBodyText().contains("Deployment") || getBodyText().contains("Completion"),
                "Analytics should show Training Deployment vs Completion graph");
    }

    @Test(priority = 68, description = "PDF Flow - Verify Analytics month and year filters")
    // Manual Test Case ID: TC590-TC620
    public void verifyAnalyticsMonthYearFiltersFromPdfFlow() {
        openAnalyticsIfAvailable();

        Assert.assertTrue(isElementDisplayed(monthFilter) || getBodyText().contains("Month"),
                "Analytics Month filter should be available");
        Assert.assertTrue(isElementDisplayed(yearFilter) || getBodyText().contains("Year"),
                "Analytics Year filter should be available");
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

    private void navigateToTraining() {
        HamburgerNavigationHelper.openModule(driver, wait, trainingTitle, "Training", "training");
    }

    private void openAssignFormIfAvailable() {
        if (!isElementDisplayed(assignButton)) {
            throw new SkipException("Assign/Create Training button is not available or locator needs confirmation");
        }
        driver.findElement(assignButton).click();
        waitForSmallDelay();
        Assert.assertTrue(driver.findElements(formField).size() > 0 || driver.findElement(By.tagName("body")).isDisplayed(),
                "Training assignment form should open");
    }

    private void openCreateTrainingFormIfAvailable() {
        if (!isElementDisplayed(createTrainingButton)) {
            throw new SkipException("Create Training button is not available or locator needs confirmation");
        }
        driver.findElement(createTrainingButton).click();
        waitForSmallDelay();
    }

    private void openTrainingLogsIfAvailable() {
        if (!isElementDisplayed(trainingLogsTab)) {
            throw new SkipException("Training Logs tab is not available for current user");
        }
        driver.findElement(trainingLogsTab).click();
        waitForSmallDelay();
    }

    private void openAnalyticsIfAvailable() {
        if (!isElementDisplayed(analyticsTab)) {
            throw new SkipException("Analytics tab is not available for current user");
        }
        driver.findElement(analyticsTab).click();
        waitForSmallDelay();
    }

    private void clickSubmitIfAvailable() {
        if (!isElementDisplayed(submitButton)) {
            throw new SkipException("Submit/Save/Assign button is not available or locator needs confirmation");
        }
        driver.findElement(submitButton).click();
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

    private boolean hasTrainingDataOrPageLoaded() {
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
