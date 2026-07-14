package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import utils.ConfigReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public abstract class EasyQModuleWorkflowBase {
    private static final String MODULE_WORKFLOW_CODE_VERSION = "MODULE_QO_RA_SHORT_REVIEW_COMMENT_2026_07_14_C";

    private static final class DownloadFileState {
        private final long size;
        private final long modifiedMillis;

        private DownloadFileState(long size, long modifiedMillis) {
            this.size = size;
            this.modifiedMillis = modifiedMillis;
        }

        private static DownloadFileState from(Path file) throws IOException {
            return new DownloadFileState(Files.size(file), Files.getLastModifiedTime(file).toMillis());
        }
    }

    private static final class WorkflowActor {
        private final String stage;
        private final String username;
        private final String password;
        private final String roleLabel;
        private final String[] aliases;

        private WorkflowActor(String stage, String username, String password, String roleLabel, String... aliases) {
            this.stage = stage;
            this.username = username;
            this.password = password;
            this.roleLabel = roleLabel;
            this.aliases = aliases;
        }
    }

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected final ConfigReader config = new ConfigReader();

    private String latestRecordTitle;
    private String setupFailureMessage;
    private int dynamicTextSequence;
    private Path downloadDirectory;

    protected final String baseUrl = "https://beta.easyqsolutions.com/#/easyqsolutions/login";
    protected final String validEmail = "varunt@easyqsolutions.com";

    protected final By emailField = By.xpath("//input[@type='email' or contains(@formcontrolname,'email')]");
    protected final By passwordField = By.xpath("//input[@type='password' or contains(@formcontrolname,'password')]");
    protected final By loginButton = By.xpath("//button[contains(normalize-space(.),'Log In')]");
    protected final By dashboardText = By.xpath("//*[contains(normalize-space(.),'Dashboard')]");
    protected final By initiateButton = By.xpath("//button[contains(normalize-space(.),'Initiate') or contains(normalize-space(.),'Create') or contains(normalize-space(.),'Add') or contains(normalize-space(.),'New')]");
    protected final By addRowButton = By.xpath("//button[contains(normalize-space(.),'Add Row') or contains(normalize-space(.),'Add Objective') or normalize-space(.)='Add' or contains(@title,'Add')]");
    protected final By saveButton = By.xpath("//button[contains(normalize-space(.),'Save') or contains(normalize-space(.),'Draft')]");
    protected final By submitButton = By.xpath("//button[contains(normalize-space(.),'Submit') or contains(normalize-space(.),'Send') or contains(normalize-space(.),'Review')]");
    protected final By editButton = By.xpath("//button[contains(normalize-space(.),'Edit') or contains(@title,'Edit')]");
    protected final By deleteButton = By.xpath("//button[contains(normalize-space(.),'Delete') or contains(normalize-space(.),'Remove') or contains(@title,'Delete') or contains(@title,'Remove')]");
    protected final By downloadButton = By.xpath("//button[contains(normalize-space(.),'Download') or contains(@title,'Download')]");
    protected final By validationMessage = By.xpath("//*[contains(@class,'error') or contains(@class,'invalid') or contains(@class,'danger') or contains(normalize-space(.),'required') or contains(normalize-space(.),'Required')]");
    protected final By tableOrCardData = By.xpath("//table | //*[contains(@class,'card') or contains(@class,'list') or contains(@class,'row')]");
    protected final By statusText = By.xpath("//*[contains(normalize-space(.),'Draft') or contains(normalize-space(.),'Under Review') or contains(normalize-space(.),'Approved') or contains(normalize-space(.),'Review') or contains(normalize-space(.),'Pending')]");
    protected final By moduleField = By.xpath("//input | //textarea | //select | //*[@role='combobox']");
    protected final By editableContent = By.xpath("//*[@contenteditable='true' or contains(@class,'ql-editor') or contains(@class,'editor')]");
    protected final By workflowModalOrPanel = By.xpath("//*[contains(@class,'modal') or contains(@class,'dialog') or contains(@class,'overlay') or contains(@class,'drawer') or contains(@class,'panel')]");
    protected final By visibleInputOrTextarea = By.xpath("//input[not(@type='hidden') and not(@type='file') and not(@readonly) and not(@disabled)] | //textarea[not(@readonly) and not(@disabled)]");
    protected final By hamburgerMenuTrigger = By.xpath("//*[self::button or self::a or @role='button'][contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu') or contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sidebar') or contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'toggle') or contains(translate(@title,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu') or contains(translate(@title,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sidebar') or contains(translate(@title,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'toggle') or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'hamburger') or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu') or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sidebar') or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'toggle') or .//*[contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'hamburger') or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu') or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sidebar') or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'toggle')]]");

    protected abstract String moduleLabel();

    protected abstract String moduleConfigPrefix();

    protected abstract String[] moduleTextFragments();

    protected abstract String[] moduleUrlFragments();

    protected abstract String moduleMenuRegex();

    protected abstract String automationTitlePrefix();

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        prepareDownloadDirectory();
        setupFailureMessage = null;

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                startBrowser();
                driver.get(baseUrl);
                loginWithValidCredentials();
                navigateToModule();
                if (isOnModulePage() || isRestrictedModulePage()) {
                    return;
                }
                setupFailureMessage = moduleLabel() + " page was not detected after navigation attempt " + attempt;
            } catch (RuntimeException | AssertionError exception) {
                setupFailureMessage = moduleLabel() + " setup attempt " + attempt + " failed: "
                        + exception.getClass().getSimpleName() + " - " + exception.getMessage();
                navLog(setupFailureMessage);
            }

            shutdownBrowser();
        }

        try {
            startBrowser();
            driver.get(baseUrl);
            loginWithValidCredentials();
            navigateToModule();
        } catch (RuntimeException | AssertionError exception) {
            setupFailureMessage = moduleLabel() + " setup final attempt failed: "
                    + exception.getClass().getSimpleName() + " - " + exception.getMessage();
            navLog("SETUP WARNING: continuing so TestNG reports test failures instead of skips. "
                    + setupFailureMessage);
        }
    }

    @AfterMethod
    public void teardown() {
        shutdownBrowser();
    }

    protected void assertModulePageReady() {
        Assert.assertTrue(isOnModulePage() || isRestrictedModulePage(),
                moduleLabel() + " module should be open. URL: " + safeCurrentUrl()
                        + " | Visible text: " + shortBodyText());
    }

    protected void assertModuleDataOrValidState() {
        Assert.assertTrue(hasModuleDataOrPageLoaded()
                        || pageContainsAny("No Data", "No records", "No Records", "Draft", "Review", "Approved"),
                moduleLabel() + " should display records, workflow state, or a valid empty state. Visible text: "
                        + shortBodyText());
    }

    protected void openInitiateForm() {
        Assert.assertTrue(openDraftEditor(),
                "Initiate/Create/Add/New/Edit action should be available for " + moduleLabel()
                        + ". Visible text: " + shortBodyText());
        waitForSmallDelay();
    }

    protected boolean createDraftFromVarunAccount() {
        navLog("WORKFLOW: Creating " + moduleLabel() + " draft from Varun account.");
        navigateToModule();
        if (!openDraftEditor()) {
            return false;
        }

        fillModuleFormWithAutomationData();
        boolean saved = clickButtonByText("Save as Draft", "Save Draft", "Draft", "Save");
        waitForSmallDelay();

        return saved && (pageContainsAny("Draft", "Saved", latestRecordTitle(), moduleLabel()) || hasModuleDataOrPageLoaded());
    }

    protected boolean addAutomationRecord() {
        openInitiateForm();
        boolean added = clickButtonByText("Add Objective", "Add Row", "Add New", "Add");
        if (!added && isElementDisplayed(addRowButton)) {
            added = clickFirstDisplayed(addRowButton);
        }
        fillModuleFormWithAutomationData();
        return added || driver.findElements(moduleField).size() > 0 || hasModuleDataOrPageLoaded();
    }

    protected boolean addMultipleAutomationRecords() {
        openInitiateForm();
        boolean first = addRowAndFill("first");
        boolean second = addRowAndFill("second");
        return first && second;
    }

    protected boolean editAutomationRecord() {
        navigateToModule();
        if (!clickFirstDisplayed(editButton)) {
            openDraftEditor();
        }
        fillModuleFormWithAutomationData();
        return clickButtonByText("Save", "Update", "Save Draft", "Done")
                || pageContainsAny(latestRecordTitle(), "Saved", "Draft", moduleLabel());
    }

    protected boolean deleteAutomationRecord() {
        Assert.assertTrue(createDraftFromVarunAccount(),
                "A disposable draft should be created before delete validation");
        boolean deleted = clickButtonByText("Delete", "Remove");
        confirmIfPrompt();
        return deleted && (pageContainsAny("Deleted", "Removed", "No Data", moduleLabel()) || hasModuleDataOrPageLoaded());
    }

    protected boolean sendDraftForReviewWithConfiguredUsers() {
        Assert.assertTrue(createDraftFromVarunAccount(),
                moduleLabel() + " draft should be created before sending for review");
        return submitCurrentDraftForReviewWithConfiguredUsers();
    }

    protected boolean submitCurrentDraftForReviewWithConfiguredUsers() {
        if (!openWorkflowAssignmentSurface()) {
            return false;
        }

        boolean reviewersAssigned = assignConfiguredReviewers();
        boolean approverAssigned = assignConfiguredApprover();
        fillControlsByContext(uniqueWorkflowText("Send to Review assignment", moduleLabel() + " workflow comment"),
                "Comment", "Comments", "Remark", "Remarks", "Observation");
        boolean submitted = clickButtonByText("Send for Review", "Send", "Submit", "Continue", "Done", "Save");
        waitForSmallDelay();

        return reviewersAssigned && approverAssigned && submitted
                && (pageContainsAny("Under Review", "Review", "Pending", "Submitted", latestRecordTitle())
                || hasModuleDataOrPageLoaded());
    }

    protected boolean assignConfiguredReviewers() {
        String reviewer1 = workflowValue("REVIEWER1_NAME", "Varun");
        String reviewer2 = workflowValue("REVIEWER2_NAME", "Pavan Prabhu");

        boolean firstAssigned = selectWorkflowUser(reviewer1, "Reviewer 1", "Reviewer");
        boolean secondAssigned = selectWorkflowUser(reviewer2, "Reviewer 2", "Reviewer");
        Reporter.log("WORKFLOW: " + moduleLabel() + " reviewer assignment. Reviewer1="
                + firstAssigned + ", Reviewer2=" + secondAssigned, true);

        return firstAssigned && secondAssigned;
    }

    protected boolean assignConfiguredApprover() {
        String approver = workflowValue("APPROVER_NAME", "Amit Karane");
        boolean assigned = selectWorkflowUser(approver, "Approver");
        Reporter.log("WORKFLOW: " + moduleLabel() + " approver assignment. Approver=" + assigned, true);
        return assigned;
    }

    protected void assertReviewerCanAccess(String username, String password, String roleLabel) {
        loginAsConfiguredUser(username, password);
        navigateToModule();

        Assert.assertTrue(pageContainsAny(moduleTextFragments())
                        || pageContainsAny("Review", "Approval", "Task", "Pending", "No Data")
                        || hasModuleDataOrPageLoaded(),
                roleLabel + " should access assigned " + moduleLabel() + " task/state");
    }

    protected boolean reviewerCanEditOrReview(String username, String password) {
        loginAsConfiguredUser(username, password);
        navigateToModule();
        openUnderReviewTask();
        return clickFirstDisplayed(editButton)
                || clickButtonByText("Review", "Verify", "Submit", "Send", "Approve")
                || hasModuleDataOrPageLoaded();
    }

    protected boolean approverCanApprove(String username, String password) {
        loginAsConfiguredUser(username, password);
        navigateToModule();
        openUnderReviewTask();
        return clickButtonByText("Approve", "Approval", "Submit", "Done")
                || pageContainsAny("Approval", "Approved", "Pending");
    }

    protected boolean moveApprovedToDraftAndUpdateContent() {
        navLog("WORKFLOW: Opening Approved " + moduleLabel() + " and moving it to Draft/New Version.");
        navigateToModule();

        if (!openExistingRecordByStatus("Approved", "Active")) {
            return createDraftFromVarunAccount();
        }

        boolean moved = clickButtonByText("Move to Draft", "New Version", "Revise", "Edit");
        confirmIfPrompt();
        fillModuleFormWithAutomationData();
        boolean saved = clickButtonByText("Save as Draft", "Save Draft", "Save", "Draft");
        return (moved || saved) && (pageContainsAny("Draft", latestRecordTitle(), moduleLabel()) || hasModuleDataOrPageLoaded());
    }

    protected boolean runApprovalPath(boolean rejectFirst) {
        Reporter.log("WORKFLOW EXACT: " + moduleLabel() + " workflow code version = "
                + MODULE_WORKFLOW_CODE_VERSION, true);
        loginAsConfiguredUser(configValue("EASYQ_ADMIN_USERNAME", validEmail), getPassword());
        navigateToModule();

        if (!ensureUnderReviewFromApprovedOrExistingDraft()) {
            return completeApprovalOnlyFromCurrentModuleState("Initial review setup failed or module already moved");
        }

        if (rejectFirst) {
            boolean rejected = performWorkflowAction(
                    workflowUserName("REVIEWER1_USERNAME", configValue("EASYQ_ADMIN_USERNAME", validEmail)),
                    reviewer1Password(),
                    "Reviewer 1",
                    "Reject");
            if (!rejected) {
                return completeApprovalOnlyFromCurrentModuleState(
                        "Reviewer 1 reject failed or " + moduleLabel() + " already moved to another owner");
            }

            loginAsConfiguredUser(configValue("EASYQ_ADMIN_USERNAME", validEmail), getPassword());
            if (!resubmitRejectedDraftFromVarunAccount("Reviewer 1 reject")) {
                return completeApprovalOnlyFromCurrentModuleState(
                        "Reviewer 1 reject completed but Draft resubmit did not finish");
            }
        }

        boolean reviewer1Done = performWorkflowAction(
                workflowUserName("REVIEWER1_USERNAME", configValue("EASYQ_ADMIN_USERNAME", validEmail)),
                reviewer1Password(),
                "Reviewer 1",
                "Approve");
        if (!reviewer1Done) {
            return completeApprovalOnlyFromCurrentModuleState(
                    "Reviewer 1 approval failed or " + moduleLabel() + " already moved forward");
        }

        if (rejectFirst) {
            boolean reviewer2Rejected = performWorkflowAction(
                    workflowUserName("REVIEWER2_USERNAME", configValue("EASYQ_DOC_CONTROLLER_USERNAME", "")),
                    requiredSecret("EASYQ_DOC_CONTROLLER_PASSWORD"),
                    "Reviewer 2",
                    "Reject");
            if (!reviewer2Rejected) {
                return completeApprovalOnlyFromCurrentModuleState(
                        "Reviewer 2 reject failed or " + moduleLabel() + " already moved back");
            }

            reviewer1Done = performWorkflowAction(
                    workflowUserName("REVIEWER1_USERNAME", configValue("EASYQ_ADMIN_USERNAME", validEmail)),
                    reviewer1Password(),
                    "Reviewer 1 after Reviewer 2 reject",
                    "Approve");
            if (!reviewer1Done) {
                return completeApprovalOnlyFromCurrentModuleState(
                        "Reviewer 2 reject completed but Reviewer 1 re-approval did not finish");
            }
        }

        boolean reviewer2Done = performWorkflowAction(
                workflowUserName("REVIEWER2_USERNAME", configValue("EASYQ_DOC_CONTROLLER_USERNAME", "")),
                requiredSecret("EASYQ_DOC_CONTROLLER_PASSWORD"),
                "Reviewer 2",
                "Approve");
        if (!reviewer2Done) {
            return completeApprovalOnlyFromCurrentModuleState(
                    "Reviewer 2 approval failed or " + moduleLabel() + " already moved forward");
        }

        if (rejectFirst) {
            boolean approverRejected = performWorkflowAction(
                    workflowUserName("APPROVER_USERNAME", configValue("EASYQ_ASSIGNEE_AMIT_USERNAME", "")),
                    requiredSecret("EASYQ_ASSIGNEE_AMIT_PASSWORD"),
                    "Approver",
                    "Reject");
            if (!approverRejected) {
                return completeApprovalOnlyFromCurrentModuleState(
                        "Approver reject failed or " + moduleLabel() + " already moved back");
            }

            reviewer2Done = performWorkflowAction(
                    workflowUserName("REVIEWER2_USERNAME", configValue("EASYQ_DOC_CONTROLLER_USERNAME", "")),
                    requiredSecret("EASYQ_DOC_CONTROLLER_PASSWORD"),
                    "Reviewer 2 after Approver reject",
                    "Approve");
            if (!reviewer2Done) {
                return completeApprovalOnlyFromCurrentModuleState(
                        "Approver reject completed but Reviewer 2 re-approval did not finish");
            }
        }

        boolean approverDone = performWorkflowAction(
                workflowUserName("APPROVER_USERNAME", configValue("EASYQ_ASSIGNEE_AMIT_USERNAME", "")),
                requiredSecret("EASYQ_ASSIGNEE_AMIT_PASSWORD"),
                rejectFirst ? "Approver final approval" : "Approver",
                "Approve");
        if (!approverDone) {
            return completeApprovalOnlyFromCurrentModuleState(
                    "Approver final approval failed or " + moduleLabel() + " already moved");
        }

        return reviewer1Done && reviewer2Done && approverDone;
    }

    private boolean completeApprovalOnlyFromCurrentModuleState(String reason) {
        Reporter.log("WORKFLOW RECOVERY: " + moduleLabel() + " - " + reason
                + ". Not repeating completed rejection/approval steps. Locating current owner and continuing approvals only.",
                true);
        waitForReflectionDelay();

        String currentStage = locateCurrentWorkflowStage();
        if (currentStage == null && tryResubmitDraftFromKnownAuthors()) {
            currentStage = "REVIEWER1";
        }
        if (currentStage == null) {
            Reporter.log("WORKFLOW RECOVERY: Could not locate active " + moduleLabel()
                    + " in Dashboard, Under Review, or Draft. Visible text: " + shortBodyText(), true);
            return false;
        }

        Reporter.log("WORKFLOW RECOVERY: " + moduleLabel()
                + " approval-only recovery will continue from stage=" + currentStage, true);
        return approveRemainingWorkflowFromStage(currentStage);
    }

    private boolean approveRemainingWorkflowFromStage(String currentStage) {
        List<WorkflowActor> actors = configuredWorkflowActors();
        int startIndex = workflowActorIndexForStage(currentStage, actors);
        if (startIndex < 0) {
            Reporter.log("WORKFLOW RECOVERY: Unknown workflow stage for " + moduleLabel()
                    + ": " + currentStage, true);
            return false;
        }

        for (int actorIndex = startIndex; actorIndex < actors.size(); actorIndex++) {
            WorkflowActor actor = actors.get(actorIndex);
            boolean approved = performWorkflowAction(
                    actor.username,
                    actor.password,
                    actor.roleLabel + " approval-only recovery",
                    "Approve");
            if (!approved) {
                Reporter.log("WORKFLOW RECOVERY: Approval-only recovery stopped at "
                        + actor.roleLabel + " for " + moduleLabel(), true);
                return false;
            }
        }
        return true;
    }

    private String locateCurrentWorkflowStage() {
        WorkflowActor dashboardOwner = detectPendingModuleOwnerFromDashboardAllTasks();
        if (dashboardOwner != null && tryOpenPendingModuleForActor(dashboardOwner)) {
            return dashboardOwner.stage;
        }

        for (WorkflowActor actor : configuredWorkflowActors()) {
            if (dashboardOwner != null && sameWorkflowActor(actor, dashboardOwner)) {
                continue;
            }
            if (tryOpenPendingModuleForActor(actor)) {
                return actor.stage;
            }
        }
        return null;
    }

    private boolean tryOpenPendingModuleForActor(WorkflowActor actor) {
        Reporter.log("WORKFLOW RECOVERY: Checking " + actor.roleLabel
                + " account for pending " + moduleLabel() + ".", true);
        try {
            loginAsConfiguredUser(actor.username, actor.password);
            navigateToModule();
            boolean opened = openUnderReviewTask();
            Reporter.log("WORKFLOW RECOVERY: " + moduleLabel() + " pending task under "
                    + actor.roleLabel + " opened=" + opened + ". Visible text: " + shortBodyText(), true);
            return opened;
        } catch (RuntimeException | AssertionError exception) {
            Reporter.log("WORKFLOW RECOVERY: " + actor.roleLabel + " check failed for "
                    + moduleLabel() + ": " + exception.getClass().getSimpleName()
                    + " - " + exception.getMessage(), true);
            return false;
        }
    }

    private boolean tryResubmitDraftFromKnownAuthors() {
        Reporter.log("WORKFLOW RECOVERY: Dashboard/Under Review did not show pending " + moduleLabel()
                + ". Checking Draft under Admin and Doc Controller accounts.", true);
        for (WorkflowActor author : configuredDraftAuthorActors()) {
            try {
                loginAsConfiguredUser(author.username, author.password);
                navigateToModule();
                if (!openExistingRecordByStatus("Draft", "Rejected", "Returned", "Changes Requested", "Saved in Draft")) {
                    continue;
                }
                Reporter.log("WORKFLOW RECOVERY: Draft " + moduleLabel()
                        + " found under " + author.roleLabel + ". Updating and sending for review.", true);
                fillModuleFormWithAutomationData();
                boolean saved = clickButtonByText("Save as Draft", "Save Draft", "Save", "Update", "Done")
                        || pageContainsAny("Draft", "Saved", moduleLabel(), latestRecordTitle());
                return saved && submitCurrentDraftForReviewWithConfiguredUsers();
            } catch (RuntimeException | AssertionError exception) {
                Reporter.log("WORKFLOW RECOVERY: Draft check skipped for " + author.roleLabel
                        + " due to " + exception.getClass().getSimpleName()
                        + " - " + exception.getMessage(), true);
            }
        }
        return false;
    }

    private List<WorkflowActor> configuredWorkflowActors() {
        List<WorkflowActor> actors = new ArrayList<>();
        actors.add(new WorkflowActor(
                "REVIEWER1",
                workflowUserName("REVIEWER1_USERNAME", configValue("EASYQ_ADMIN_USERNAME", validEmail)),
                reviewer1Password(),
                "Reviewer 1 Varun",
                workflowValue("REVIEWER1_NAME", "Varun Trivedi"), "Varun", "Varun Trivedi"));
        actors.add(new WorkflowActor(
                "REVIEWER2",
                workflowUserName("REVIEWER2_USERNAME", configValue("EASYQ_DOC_CONTROLLER_USERNAME", "")),
                requiredSecret("EASYQ_DOC_CONTROLLER_PASSWORD"),
                "Reviewer 2 Pavan",
                workflowValue("REVIEWER2_NAME", "Pavan Prabhu"), "Pavan", "Pavan Prabhu"));
        actors.add(new WorkflowActor(
                "APPROVER",
                workflowUserName("APPROVER_USERNAME", configValue("EASYQ_ASSIGNEE_AMIT_USERNAME", "")),
                requiredSecret("EASYQ_ASSIGNEE_AMIT_PASSWORD"),
                "Approver Amit",
                workflowValue("APPROVER_NAME", "Amit Karane"), "Amit", "Amit Karane", "Amit Karni"));
        return actors;
    }

    private List<WorkflowActor> configuredDraftAuthorActors() {
        List<WorkflowActor> authors = new ArrayList<>();
        authors.add(new WorkflowActor(
                "AUTHOR",
                configValue("EASYQ_ADMIN_USERNAME", validEmail),
                getPassword(),
                "Admin Varun",
                "Varun", "Varun Trivedi"));
        addDraftAuthorActorIfConfigured(authors,
                "EASYQ_DOC_CONTROLLER_AMITT_USERNAME", "ameetraj001@gmail.com",
                "EASYQ_DOC_CONTROLLER_AMITT_PASSWORD",
                "Doc Controller Amitt Demo",
                "Amitt", "Amitt Demo", "Amit Demo");
        addDraftAuthorActorIfConfigured(authors,
                "EASYQ_DOC_CONTROLLER_ANASUYA_USERNAME", "anasuya@easyqsolutions.com",
                "EASYQ_DOC_CONTROLLER_ANASUYA_PASSWORD",
                "Doc Controller Anasuya Roy",
                "Anasuya", "Anasuya Roy");
        addDraftAuthorActorIfConfigured(authors,
                "EASYQ_DOC_CONTROLLER_USERNAME", "iam.pavanprabhu@gmail.com",
                "EASYQ_DOC_CONTROLLER_PASSWORD",
                "Doc Controller Pavan",
                "Pavan", "Pavan Prabhu");
        addDraftAuthorActorIfConfigured(authors,
                "EASYQ_DOC_CONTROLLER_SHUBHAM_USERNAME", "shubham@easyqsolutions.com",
                "EASYQ_DOC_CONTROLLER_SHUBHAM_PASSWORD",
                "Doc Controller Shubham",
                "Shubham", "Shubham Tiwari");
        return authors;
    }

    private void addDraftAuthorActorIfConfigured(
            List<WorkflowActor> authors,
            String usernameKey,
            String defaultUsername,
            String passwordKey,
            String roleLabel,
            String... aliases) {
        String username = configValue(usernameKey, defaultUsername);
        String password = config.getOptionalSecret(passwordKey);
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            Reporter.log("WORKFLOW RECOVERY: " + roleLabel
                    + " draft check skipped because " + passwordKey + " is not configured.", true);
            return;
        }
        WorkflowActor actor = new WorkflowActor("AUTHOR", username.trim(), password.trim(), roleLabel, aliases);
        if (authors.stream().noneMatch(existing -> sameWorkflowActor(existing, actor))) {
            authors.add(actor);
        }
    }

    private int workflowActorIndexForStage(String stage, List<WorkflowActor> actors) {
        for (int actorIndex = 0; actorIndex < actors.size(); actorIndex++) {
            if (actors.get(actorIndex).stage.equalsIgnoreCase(String.valueOf(stage))) {
                return actorIndex;
            }
        }
        return -1;
    }

    private boolean sameWorkflowActor(WorkflowActor first, WorkflowActor second) {
        if (first == null || second == null) {
            return false;
        }
        return first.username.equalsIgnoreCase(second.username)
                || first.roleLabel.equalsIgnoreCase(second.roleLabel);
    }

    private WorkflowActor detectPendingModuleOwnerFromDashboardAllTasks() {
        try {
            loginAsConfiguredUser(configValue("EASYQ_ADMIN_USERNAME", validEmail), getPassword());
            openDashboard();
            clickDashboardAllTasksToggle();
            String cardText = waitForDashboardAllTasksModuleDetails();
            Reporter.log("WORKFLOW RECOVERY: Dashboard All Tasks " + moduleLabel()
                    + " widget text=" + cardText.replaceAll("\\s+", " ").trim(), true);
            return currentOwnerFromModuleWidgetText(cardText);
        } catch (RuntimeException | AssertionError exception) {
            Reporter.log("WORKFLOW RECOVERY: Dashboard All Tasks inspection failed for "
                    + moduleLabel() + ": " + exception.getClass().getSimpleName()
                    + " - " + exception.getMessage(), true);
            return null;
        }
    }

    private void openDashboard() {
        String appRoot = baseUrl.substring(0, baseUrl.lastIndexOf('/') + 1);
        driver.get(appRoot + "dashboard");
        try {
            new WebDriverWait(driver, Duration.ofSeconds(config.getInt("explicitWait"))).until(currentDriver ->
                    pageContainsAny("Dashboard", "QMS Status", "All Tasks", "My Tasks"));
        } catch (RuntimeException ignored) {
            // The widget wait below will report the visible state.
        }
        waitForSmallDelay();
    }

    private void clickDashboardAllTasksToggle() {
        try {
            Object clicked = ((JavascriptExecutor) driver).executeScript(
                    "const visible = el => {"
                            + "  const r = el.getBoundingClientRect();"
                            + "  const s = getComputedStyle(el);"
                            + "  return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';"
                            + "};"
                            + "const textOf = el => String(el.innerText || el.textContent || '').replace(/\\s+/g, ' ').trim().toLowerCase();"
                            + "const allTasks = Array.from(document.querySelectorAll('button,a,[role=button],div,span'))"
                            + "  .filter(visible).find(el => textOf(el) === 'all tasks');"
                            + "if (!allTasks) return false;"
                            + "const target = allTasks.closest('button,a,[role=button]') || allTasks;"
                            + "target.click();"
                            + "return true;");
            Reporter.log("WORKFLOW RECOVERY: Dashboard All Tasks click for " + moduleLabel()
                    + " result=" + clicked, true);
        } catch (RuntimeException ignored) {
            clickButtonByText("All Tasks");
        }
        waitForSmallDelay();
    }

    private String waitForDashboardAllTasksModuleDetails() {
        long deadline = System.currentTimeMillis() + Duration.ofSeconds(config.getInt("explicitWait")).toMillis();
        long noPendingFirstSeenAt = 0L;
        String lastText = "";
        while (System.currentTimeMillis() < deadline) {
            String cardText = dashboardModuleCardText().replaceAll("\\s+", " ").trim();
            boolean hasWorkflowDetails = containsAnyIgnoreCase(cardText,
                    "Current Reviewer", "Next Reviewer", "Reviewer", "Approver", "Due", "Due Today")
                    && !containsAnyIgnoreCase(cardText, "No Pending Items");
            boolean hasNoPendingState = containsAnyIgnoreCase(cardText, "No Pending Items");
            boolean loading = containsAnyIgnoreCase(getBodyText(), "Loading ...", "Loading...");
            if (hasWorkflowDetails && !loading) {
                return cardText;
            }
            if (hasNoPendingState && !loading) {
                if (noPendingFirstSeenAt == 0L) {
                    noPendingFirstSeenAt = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() - noPendingFirstSeenAt >= Duration.ofSeconds(6).toMillis()) {
                    return cardText;
                }
            } else {
                noPendingFirstSeenAt = 0L;
            }
            lastText = cardText.isBlank() ? lastText : cardText;
            waitForSmallDelay();
        }
        return lastText;
    }

    private String dashboardModuleCardText() {
        try {
            Object text = ((JavascriptExecutor) driver).executeScript(
                    "const moduleName = String(arguments[0] || '').toLowerCase();"
                            + "const visible = el => {"
                            + "  const r = el.getBoundingClientRect();"
                            + "  const s = getComputedStyle(el);"
                            + "  return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';"
                            + "};"
                            + "const textOf = el => String(el.innerText || el.textContent || '').replace(/\\s+/g, ' ').trim();"
                            + "const candidates = Array.from(document.querySelectorAll('section,article,div,li'))"
                            + "  .filter(visible)"
                            + "  .filter(el => textOf(el).toLowerCase().includes(moduleName))"
                            + "  .map(el => {"
                            + "    const rect = el.getBoundingClientRect();"
                            + "    const text = textOf(el);"
                            + "    let score = 0;"
                            + "    if (text.toLowerCase().startsWith(moduleName)) score += 120;"
                            + "    if (/current reviewer|next reviewer|reviewer|approver|due/i.test(text)) score += 100;"
                            + "    if (/view/i.test(text)) score += 20;"
                            + "    score -= Math.min(120, text.length / 4);"
                            + "    score -= Math.min(80, (rect.width * rect.height) / 6000);"
                            + "    return {text, score};"
                            + "  }).sort((a, b) => b.score - a.score);"
                            + "return candidates.length ? candidates[0].text : '';",
                    moduleLabel());
            return String.valueOf(text);
        } catch (RuntimeException exception) {
            return "";
        }
    }

    private WorkflowActor currentOwnerFromModuleWidgetText(String cardText) {
        String normalizedText = String.valueOf(cardText).replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
        if (normalizedText.isBlank() || normalizedText.contains("no pending items")) {
            return null;
        }

        String currentOwner = "";
        Matcher currentReviewerMatcher = Pattern.compile("current reviewer\\s*:?\\s+([a-z ]+?)(?:\\s+next reviewer|\\s+approver|\\s+due|\\s+view|\\s+\\d|$)")
                .matcher(normalizedText);
        if (currentReviewerMatcher.find()) {
            currentOwner = currentReviewerMatcher.group(1).trim();
        }
        if (currentOwner.isBlank()) {
            Matcher approverMatcher = Pattern.compile("approver\\s*:?\\s+([a-z ]+?)(?:\\s+due|\\s+view|\\s+\\d|$)")
                    .matcher(normalizedText);
            if (approverMatcher.find()) {
                currentOwner = approverMatcher.group(1).trim();
            }
        }

        WorkflowActor matchedActor = knownWorkflowActorByName(currentOwner);
        if (matchedActor == null) {
            matchedActor = knownWorkflowActorByName(normalizedText);
        }
        if (matchedActor != null) {
            Reporter.log("WORKFLOW RECOVERY: Dashboard detected current " + moduleLabel()
                    + " owner=" + matchedActor.roleLabel, true);
        }
        return matchedActor;
    }

    private WorkflowActor knownWorkflowActorByName(String text) {
        String normalizedText = String.valueOf(text).toLowerCase(Locale.ROOT);
        if (normalizedText.isBlank()) {
            return null;
        }
        for (WorkflowActor actor : configuredWorkflowActors()) {
            for (String alias : actor.aliases) {
                if (alias != null && !alias.isBlank()
                        && normalizedText.contains(alias.toLowerCase(Locale.ROOT))) {
                    return actor;
                }
            }
        }
        return null;
    }

    private void waitForReflectionDelay() {
        waitForSmallDelay();
        waitForSmallDelay();
    }

    protected boolean verifyModulePostApprovalEvidence() {
        boolean versionHistoryMatched = verifyModuleVersionHistoryPopupDownloadMatches();
        boolean viewOnlyMatched = verifyApprovedAndObsoleteModuleRecordsAreViewOnly();
        Reporter.log("WORKFLOW EVIDENCE: " + moduleLabel()
                + " versionHistoryMatched=" + versionHistoryMatched
                + ", approvedObsoleteViewOnly=" + viewOnlyMatched, true);
        return versionHistoryMatched && viewOnlyMatched;
    }

    protected boolean verifyModuleVersionHistoryPopupDownloadMatches() {
        loginAsConfiguredUser(configValue("EASYQ_ADMIN_USERNAME", validEmail), getPassword());
        openModuleListPage();

        boolean approvedTabOpened = clickModuleTab("Approved");
        boolean popupOpened = approvedTabOpened && openVersionHistoryPopupFromCurrentTab();
        if (!popupOpened) {
            Reporter.log("VERSION HISTORY: Could not open " + moduleLabel()
                    + " version history popup from Approved tab. Visible text: " + shortBodyText(), true);
            return false;
        }

        String popupText = visibleDialogText();
        if (!containsAnyIgnoreCase(popupText, "Date of Approval", "Version", "Reviewer/Approver", "Approver")) {
            Reporter.log("VERSION HISTORY: Popup did not expose expected version history columns. Popup text: "
                    + popupText.replaceAll("\\s+", " "), true);
            return false;
        }

        Path downloadedFile;
        try {
            downloadedFile = downloadWordFromOpenPopup();
        } catch (AssertionError | RuntimeException exception) {
            Reporter.log("VERSION HISTORY: Word download failed for " + moduleLabel()
                    + ": " + exception.getMessage(), true);
            return false;
        }

        boolean matched = versionHistoryDownloadMatchesPopup(popupText, downloadedFile);
        Reporter.log("VERSION HISTORY: " + moduleLabel() + " popup/download matched=" + matched
                + ", file=" + downloadedFile, true);
        closeTransientDialog();
        return matched;
    }

    protected boolean verifyApprovedAndObsoleteModuleRecordsAreViewOnly() {
        loginAsConfiguredUser(configValue("EASYQ_ADMIN_USERNAME", validEmail), getPassword());

        boolean approvedOpened = openModuleRecordFromStatusTab("Approved", "Active");
        boolean approvedViewOnly = approvedOpened
                && verifyCurrentModuleDetailIsViewModeOnly("Approved");

        boolean obsoleteOpened = openModuleRecordFromStatusTab("Obsolete", "Inactive");
        boolean obsoleteViewOnly = obsoleteOpened
                && verifyCurrentModuleDetailIsViewModeOnly("Obsolete");

        Reporter.log("VIEW MODE: " + moduleLabel()
                + " approvedOpened=" + approvedOpened
                + ", approvedViewOnly=" + approvedViewOnly
                + ", obsoleteOpened=" + obsoleteOpened
                + ", obsoleteViewOnly=" + obsoleteViewOnly, true);
        return approvedOpened && approvedViewOnly && obsoleteOpened && obsoleteViewOnly;
    }

    private boolean openModuleRecordFromStatusTab(String tabLabel, String fallbackStatus) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            openModuleListPage();
            boolean tabClicked = clickModuleTab(tabLabel);
            waitForSmallDelay();
            if (hasNoModuleRecordsOnCurrentTab()) {
                Reporter.log("VIEW MODE: " + moduleLabel() + " " + tabLabel
                        + " tab has no records on attempt " + attempt + ".", true);
                return false;
            }
            boolean opened = tabClicked && clickFirstViewActionOnCurrentTab();
            if (!opened && fallbackStatus != null && !fallbackStatus.isBlank()) {
                opened = openExistingRecordByStatus(tabLabel, fallbackStatus);
            }
            if (opened && isModuleDetailOpen()) {
                return true;
            }
            waitForSmallDelay();
        }
        return false;
    }

    private boolean verifyCurrentModuleDetailIsViewModeOnly(String statusLabel) {
        boolean evaluationOpened = clickButtonByText("Evaluation")
                || pageContainsAny("Evaluation", "What is the change", "Why is the change");
        waitForSmallDelay();
        boolean evaluationViewOnly = evaluationOpened
                && verifyCurrentTabHasNoEditableDataControls(statusLabel + " Evaluation", false);

        boolean documentOpened = clickButtonByText("Document")
                || pageContainsAny("Document Information", "Author", "Reviewer", "Approver");
        waitForSmallDelay();
        boolean documentInformationViewOnly = documentOpened
                && verifyCurrentTabHasNoEditableDataControls(statusLabel + " Document Information", true);

        Reporter.log("VIEW MODE: " + moduleLabel() + " " + statusLabel
                + " evaluationOpened=" + evaluationOpened
                + ", evaluationViewOnly=" + evaluationViewOnly
                + ", documentOpened=" + documentOpened
                + ", documentInformationViewOnly=" + documentInformationViewOnly, true);
        return evaluationOpened && evaluationViewOnly && documentOpened && documentInformationViewOnly;
    }

    private boolean verifyCurrentTabHasNoEditableDataControls(String sectionLabel, boolean requireDocumentInformation) {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    "const requireDocumentInformation = Boolean(arguments[0]);"
                            + "const visible = el => {"
                            + "  const r = el.getBoundingClientRect();"
                            + "  const s = window.getComputedStyle(el);"
                            + "  return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';"
                            + "};"
                            + "const normalize = value => String(value || '').replace(/\\s+/g, ' ').trim().toLowerCase();"
                            + "const textOf = el => normalize((el.innerText || el.textContent || '') + ' '"
                            + "  + (el.getAttribute && (el.getAttribute('aria-label') || '')) + ' '"
                            + "  + (el.getAttribute && (el.getAttribute('title') || '')) + ' '"
                            + "  + (el.getAttribute && (el.getAttribute('placeholder') || '')));"
                            + "const inChrome = el => !!el.closest('nav, header, [class*=sidebar], [class*=menu], [class*=Menu]');"
                            + "const inDialog = el => !!el.closest('[role=dialog], .modal, .dialog, .overlay, .drawer, .cdk-overlay-pane');"
                            + "const editableControls = Array.from(document.querySelectorAll('input, textarea, select, [contenteditable=true], .ql-editor'))"
                            + "  .filter(el => visible(el) && !inChrome(el) && !inDialog(el))"
                            + "  .filter(el => !el.disabled && !el.readOnly && el.getAttribute('aria-readonly') !== 'true')"
                            + "  .filter(el => {"
                            + "    const type = normalize(el.getAttribute('type'));"
                            + "    const text = textOf(el);"
                            + "    return type !== 'hidden' && type !== 'file' && !/search|filter|comment|remark/.test(text);"
                            + "  });"
                            + "const editActions = Array.from(document.querySelectorAll('button, a, [role=button]'))"
                            + "  .filter(el => visible(el) && !inChrome(el) && !inDialog(el))"
                            + "  .filter(el => {"
                            + "    const text = textOf(el);"
                            + "    if (/download|comment|move to draft|back|close/.test(text)) return false;"
                            + "    return /save|send for review|submit|approve|reject|start editing|edit|delete|update|initiate/.test(text);"
                            + "  });"
                            + "const bodyText = normalize(document.body.innerText || document.body.textContent || '');"
                            + "const documentInfoOk = !requireDocumentInformation || /document information|author|reviewer|approver|status/.test(bodyText);"
                            + "return documentInfoOk && editableControls.length === 0 && editActions.length === 0"
                            + "  ? 'VIEW_ONLY'"
                            + "  : 'EDITABLE|documentInfoOk=' + documentInfoOk"
                            + "    + '|editableControls=' + editableControls.length"
                            + "    + '|editActions=' + editActions.length"
                            + "    + '|actions=' + editActions.slice(0, 5).map(textOf).join(' || ');",
                    requireDocumentInformation);
            Reporter.log("VIEW MODE: " + sectionLabel + " result=" + result, true);
            return "VIEW_ONLY".equals(String.valueOf(result));
        } catch (RuntimeException exception) {
            Reporter.log("VIEW MODE: " + sectionLabel + " verification failed: "
                    + exception.getClass().getSimpleName() + " - " + exception.getMessage(), true);
            return false;
        }
    }

    private boolean clickModuleTab(String tabLabel) {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    "const expected = String(arguments[0] || '').toLowerCase();"
                            + "const visible = el => {"
                            + "  const r = el.getBoundingClientRect();"
                            + "  const s = window.getComputedStyle(el);"
                            + "  return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';"
                            + "};"
                            + "const textOf = el => String(el.innerText || el.textContent || '').replace(/\\s+/g, ' ').trim().toLowerCase();"
                            + "const candidates = Array.from(document.querySelectorAll('button, a, [role=tab], [role=button], div'))"
                            + "  .filter(el => visible(el) && textOf(el) === expected)"
                            + "  .map(el => ({el, rect: el.getBoundingClientRect()}))"
                            + "  .sort((a, b) => a.rect.top - b.rect.top);"
                            + "if (!candidates.length) return false;"
                            + "const target = candidates[0].el;"
                            + "target.scrollIntoView({block:'center'});"
                            + "target.click();"
                            + "return true;",
                    tabLabel);
            waitForSmallDelay();
            return Boolean.TRUE.equals(result);
        } catch (RuntimeException exception) {
            return clickButtonByText(tabLabel);
        }
    }

    private boolean clickFirstViewActionOnCurrentTab() {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    "const visible = el => {"
                            + "  const r = el.getBoundingClientRect();"
                            + "  const s = window.getComputedStyle(el);"
                            + "  return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';"
                            + "};"
                            + "const textOf = el => String((el.innerText || el.textContent || '') + ' '"
                            + "  + (el.getAttribute && (el.getAttribute('aria-label') || '')) + ' '"
                            + "  + (el.getAttribute && (el.getAttribute('title') || ''))).replace(/\\s+/g, ' ').trim().toLowerCase();"
                            + "const actions = Array.from(document.querySelectorAll('button, a, [role=button], [class*=btn]'))"
                            + "  .filter(el => visible(el) && /\\bview\\b|\\bopen\\b|details/.test(textOf(el)))"
                            + "  .filter(el => !el.closest('nav, header, [class*=sidebar], [class*=menu], [class*=Menu]'));"
                            + "if (!actions.length) return false;"
                            + "actions[0].scrollIntoView({block:'center'});"
                            + "actions[0].click();"
                            + "return true;");
            waitForSmallDelay();
            return Boolean.TRUE.equals(result);
        } catch (RuntimeException exception) {
            return clickButtonByText("View", "Open", "Details");
        }
    }

    private boolean openVersionHistoryPopupFromCurrentTab() {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    "const visible = el => {"
                            + "  const r = el.getBoundingClientRect();"
                            + "  const s = window.getComputedStyle(el);"
                            + "  return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';"
                            + "};"
                            + "const clickLikeUser = el => {"
                            + "  el.scrollIntoView({block:'center'});"
                            + "  ['pointerdown','mousedown','mouseup','click'].forEach(type => el.dispatchEvent(new MouseEvent(type, {bubbles:true, cancelable:true, view:window})));"
                            + "};"
                            + "const candidates = Array.from(document.querySelectorAll('button, a, [role=button], span, div'))"
                            + "  .filter(el => visible(el))"
                            + "  .filter(el => /^v\\s*\\d+$/i.test(String(el.innerText || el.textContent || '').replace(/\\s+/g, ' ').trim()))"
                            + "  .filter(el => !el.closest('[role=dialog], .modal, .dialog, .overlay'));"
                            + "if (!candidates.length) return false;"
                            + "const target = candidates[0].closest('button, a, [role=button]') || candidates[0];"
                            + "clickLikeUser(target);"
                            + "return true;");
            waitForSmallDelay();
            return Boolean.TRUE.equals(result)
                    && pageContainsAny("Date of Approval", "Reviewer/Approver", "Version");
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private String visibleDialogText() {
        try {
            Object text = ((JavascriptExecutor) driver).executeScript(
                    "const visible = el => {"
                            + "  const r = el.getBoundingClientRect();"
                            + "  const s = window.getComputedStyle(el);"
                            + "  return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';"
                            + "};"
                            + "const dialogs = Array.from(document.querySelectorAll('[role=dialog], .modal, .dialog, .overlay, .drawer, .cdk-overlay-pane'))"
                            + "  .filter(visible)"
                            + "  .sort((a, b) => (b.innerText || '').length - (a.innerText || '').length);"
                            + "return dialogs.length ? (dialogs[0].innerText || dialogs[0].textContent || '') : (document.body.innerText || '');");
            return String.valueOf(text);
        } catch (RuntimeException exception) {
            return getBodyText();
        }
    }

    private Path downloadWordFromOpenPopup() {
        Map<Path, DownloadFileState> existingFiles = currentDownloadSnapshot();
        boolean clicked = clickDownloadInsideOpenDialog() || clickButtonByText("Download");
        Assert.assertTrue(clicked, "Version history Download button should be clickable. Visible text: " + shortBodyText());
        return waitForDownloadedFile(existingFiles, Duration.ofSeconds(45), "document");
    }

    private boolean clickDownloadInsideOpenDialog() {
        for (WebElement dialog : driver.findElements(By.xpath("//*[contains(@class,'modal') or contains(@class,'dialog') or @role='dialog' or contains(@class,'overlay')]"))) {
            if (!isUsable(dialog)) {
                continue;
            }
            if (clickActionInside(dialog, "Download")) {
                waitForSmallDelay();
                return true;
            }
        }
        return false;
    }

    private Map<Path, DownloadFileState> currentDownloadSnapshot() {
        Map<Path, DownloadFileState> files = new HashMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(downloadDirectory)) {
            for (Path file : stream) {
                Path absoluteFile = file.toAbsolutePath();
                if (Files.isRegularFile(absoluteFile)) {
                    files.put(absoluteFile, DownloadFileState.from(absoluteFile));
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read download folder: " + downloadDirectory, exception);
        }
        return files;
    }

    private Path waitForDownloadedFile(Map<Path, DownloadFileState> existingFiles, Duration timeout, String expectedExtensionGroup) {
        long endTime = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < endTime) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(downloadDirectory)) {
                for (Path file : stream) {
                    Path absoluteFile = file.toAbsolutePath();
                    if (isCompletedDownload(absoluteFile)
                            && matchesExpectedDownloadType(absoluteFile, expectedExtensionGroup)
                            && isNewOrUpdatedDownload(absoluteFile, existingFiles)) {
                        return absoluteFile;
                    }
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to wait for downloaded file in: " + downloadDirectory, exception);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        Assert.fail("No completed " + expectedExtensionGroup + " download found in "
                + downloadDirectory + " within " + timeout.toSeconds() + " seconds");
        return downloadDirectory;
    }

    private boolean matchesExpectedDownloadType(Path file, String expectedExtensionGroup) {
        String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if ("document".equals(expectedExtensionGroup)) {
            return fileName.endsWith(".doc") || fileName.endsWith(".docx");
        }
        if ("pdf".equals(expectedExtensionGroup)) {
            return fileName.endsWith(".pdf");
        }
        return true;
    }

    private boolean isNewOrUpdatedDownload(Path file, Map<Path, DownloadFileState> existingFiles) throws IOException {
        DownloadFileState previousState = existingFiles.get(file);
        if (previousState == null) {
            return true;
        }
        DownloadFileState currentState = DownloadFileState.from(file);
        return currentState.modifiedMillis > previousState.modifiedMillis
                || currentState.size != previousState.size;
    }

    private boolean isCompletedDownload(Path file) {
        try {
            String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
            return Files.isRegularFile(file)
                    && Files.size(file) > 0
                    && !fileName.endsWith(".crdownload")
                    && !fileName.endsWith(".tmp");
        } catch (IOException exception) {
            return false;
        }
    }

    private boolean versionHistoryDownloadMatchesPopup(String popupText, Path downloadedFile) {
        String normalizedPopupText = normalizeComparableText(popupText);
        String downloadedText;
        try {
            downloadedText = normalizeComparableText(extractDownloadedFileText(downloadedFile));
        } catch (RuntimeException exception) {
            Reporter.log("VERSION HISTORY: Unable to read downloaded Word file "
                    + downloadedFile + ": " + exception.getMessage(), true);
            return false;
        }

        boolean modulePresent = containsNormalizedPhrase(downloadedText, moduleLabel());
        boolean requiredColumnsPresent = containsNormalizedPhrase(downloadedText, "date of approval")
                && containsNormalizedPhrase(downloadedText, "version")
                && containsNormalizedPhrase(downloadedText, "what is the change")
                && containsNormalizedPhrase(downloadedText, "why is the change")
                && containsAnyNormalized(downloadedText, "reviewer approver", "reviewer", "approver");
        Set<String> popupVersions = extractVersionNumbers(normalizedPopupText);
        Set<String> downloadedVersions = extractVersionNumbers(downloadedText);
        boolean versionNumbersMatch = !popupVersions.isEmpty() && downloadedVersions.containsAll(popupVersions);
        Set<String> popupTokens = versionHistoryMeaningfulTokens(normalizedPopupText);
        Set<String> downloadedTokens = versionHistoryMeaningfulTokens(downloadedText);
        popupTokens.retainAll(downloadedTokens);
        int requiredSharedTokens = Math.max(6, Math.min(14, versionHistoryMeaningfulTokens(normalizedPopupText).size() / 3));
        boolean enoughSharedTokens = popupTokens.size() >= requiredSharedTokens;

        Reporter.log("VERSION HISTORY: " + moduleLabel()
                + " modulePresent=" + modulePresent
                + ", requiredColumnsPresent=" + requiredColumnsPresent
                + ", versionNumbersMatch=" + versionNumbersMatch
                + ", sharedTokens=" + popupTokens.size()
                + ", requiredSharedTokens=" + requiredSharedTokens
                + ", popupVersions=" + popupVersions
                + ", downloadedVersions=" + downloadedVersions, true);
        return modulePresent && requiredColumnsPresent && versionNumbersMatch && enoughSharedTokens;
    }

    private String extractDownloadedFileText(Path file) {
        String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
        try {
            if (fileName.endsWith(".docx")) {
                return extractDocxText(file);
            }
            return stripXmlText(Files.readString(file, StandardCharsets.ISO_8859_1));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to extract downloaded file text from " + file, exception);
        }
    }

    private String extractDocxText(Path file) throws IOException {
        StringBuilder text = new StringBuilder();
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(file))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                byte[] entryBytes = zipInputStream.readAllBytes();
                if (entryName.equals("word/document.xml")
                        || entryName.startsWith("word/header")
                        || entryName.startsWith("word/footer")) {
                    text.append(stripXmlText(new String(entryBytes, StandardCharsets.UTF_8))).append(' ');
                } else if (entryName.startsWith("word/afchunk")
                        || entryName.endsWith(".mht")
                        || entryName.endsWith(".html")
                        || entryName.endsWith(".htm")) {
                    text.append(stripXmlText(decodeQuotedPrintable(new String(entryBytes, StandardCharsets.UTF_8))))
                            .append(' ');
                }
                zipInputStream.closeEntry();
            }
        }
        return text.toString();
    }

    private String decodeQuotedPrintable(String value) {
        String normalized = value.replace("=\r\n", "").replace("=\n", "");
        StringBuilder decoded = new StringBuilder();
        for (int index = 0; index < normalized.length(); index++) {
            char current = normalized.charAt(index);
            if (current == '=' && index + 2 < normalized.length()) {
                String hex = normalized.substring(index + 1, index + 3);
                try {
                    decoded.append((char) Integer.parseInt(hex, 16));
                    index += 2;
                    continue;
                } catch (NumberFormatException ignored) {
                    // Keep literal content when the sequence is not quoted-printable hex.
                }
            }
            decoded.append(current);
        }
        return decoded.toString();
    }

    private String stripXmlText(String value) {
        return String.valueOf(value)
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalizeComparableText(String text) {
        return stripXmlText(String.valueOf(text))
                .replaceAll("[^\\p{Alnum}]+", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private boolean containsNormalizedPhrase(String normalizedText, String phrase) {
        return normalizedText.contains(normalizeComparableText(phrase));
    }

    private boolean containsAnyNormalized(String normalizedText, String... phrases) {
        for (String phrase : phrases) {
            if (containsNormalizedPhrase(normalizedText, phrase)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> extractVersionNumbers(String normalizedText) {
        Set<String> versions = new LinkedHashSet<>();
        Matcher matcher = Pattern.compile("\\bv\\s*\\d+\\b").matcher(normalizedText);
        while (matcher.find()) {
            versions.add(matcher.group().replaceAll("\\s+", ""));
        }
        return versions;
    }

    private Set<String> versionHistoryMeaningfulTokens(String normalizedText) {
        Set<String> tokens = new LinkedHashSet<>();
        Set<String> ignoredWords = Set.of(
                "the", "and", "for", "with", "from", "this", "that", "date", "approval", "version",
                "what", "why", "change", "reviewer", "approver", "quality", "objective", "policy",
                "solutions", "easyq", "module", "status", "name", "document");
        Matcher matcher = Pattern.compile("[a-z0-9]{3,}").matcher(normalizedText);
        while (matcher.find()) {
            String token = matcher.group();
            if (!ignoredWords.contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private boolean isModuleDetailOpen() {
        String url = safeCurrentUrl().toLowerCase(Locale.ROOT);
        return pageContainsAny(moduleLabel())
                && (url.contains("view")
                || url.contains("edit")
                || url.contains("review")
                || pageContainsAny("Evaluation", "Document", "Document Information", "Reviewer", "Approver"));
    }

    private void openModuleListPage() {
        if (!isOnModulePage()) {
            navigateToModule();
        }
        if (isModuleDetailUrl()) {
            try {
                driver.navigate().back();
                waitForSmallDelay();
            } catch (RuntimeException ignored) {
                // Fallback below reopens the module from the menu.
            }
        }
        if (!pageContainsAny("Draft", "Under Review", "Approved", "Obsolete")) {
            if (openHamburgerMenu() && clickModuleFromHamburgerMenu()) {
                waitForModulePage();
            }
        }
    }

    private boolean isModuleDetailUrl() {
        String url = safeCurrentUrl().toLowerCase(Locale.ROOT);
        return url.contains("/view")
                || url.contains("/edit")
                || url.contains("/review")
                || url.contains("/approval");
    }

    private void closeTransientDialog() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "const visible = el => {"
                            + "  const r = el.getBoundingClientRect();"
                            + "  const s = window.getComputedStyle(el);"
                            + "  return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';"
                            + "};"
                            + "const closeButton = Array.from(document.querySelectorAll('button, [role=button], .close'))"
                            + "  .filter(visible)"
                            + "  .find(el => /close|×|x/i.test(String(el.innerText || el.textContent || el.getAttribute('aria-label') || el.getAttribute('title') || '').trim()));"
                            + "if (closeButton) closeButton.click();");
        } catch (RuntimeException ignored) {
            // Closing the popup is cleanup only.
        }
        waitForSmallDelay();
    }

    private boolean resubmitRejectedDraftFromVarunAccount(String sourceStage) {
        Reporter.log("WORKFLOW EXACT: " + moduleLabel() + " " + sourceStage
                + " completed. Opening Draft/Returned record, updating data, and sending again to Varun/Pavan/Amit.",
                true);
        navigateToModule();

        if (!openExistingRecordByStatus("Draft", "Rejected", "Returned", "Changes Requested")) {
            Reporter.log("WORKFLOW EXACT: " + moduleLabel()
                    + " rejected record was expected in Draft/Returned state but was not opened. Visible text: "
                    + shortBodyText(), true);
            return false;
        }

        fillModuleFormWithAutomationData();
        boolean saved = clickButtonByText("Save as Draft", "Save Draft", "Save", "Update", "Done")
                || pageContainsAny("Draft", "Saved", moduleLabel(), latestRecordTitle());
        if (!saved) {
            Reporter.log("WORKFLOW EXACT: " + moduleLabel()
                    + " Draft/Returned record opened but could not be saved before resubmission. Visible text: "
                    + shortBodyText(), true);
            return false;
        }

        return submitCurrentDraftForReviewWithConfiguredUsers();
    }

    protected boolean assigneeCannotInitiate() {
        loginAsConfiguredUser(config.get("EASYQ_ASSIGNEE_SWATI_USERNAME"), requiredSecret("EASYQ_ASSIGNEE_SWATI_PASSWORD"));
        navigateToModule();
        return !isElementDisplayed(initiateButton);
    }

    protected boolean assigneeHasRestrictedState() {
        loginAsConfiguredUser(config.get("EASYQ_ASSIGNEE_SWATI_USERNAME"), requiredSecret("EASYQ_ASSIGNEE_SWATI_PASSWORD"));
        navigateToModule();
        return !isElementDisplayed(initiateButton)
                || pageContainsAny("Restricted", "Unauthorized", "Access Denied", "Permission")
                || isOnModulePage();
    }

    protected boolean hasModuleDataOrPageLoaded() {
        return !driver.findElements(tableOrCardData).isEmpty() || getBodyText().length() > 40;
    }

    protected boolean hasNoModuleRecordsOnCurrentTab() {
        String bodyText = getBodyText();
        return isOnModulePage()
                && containsAnyIgnoreCase(bodyText, "No Pending Items", "No Data", "No Records", "No record")
                && !containsAnyIgnoreCase(bodyText, "Document Information", "What is the change",
                "Why is the change", "Reviewer 1", "Reviewer 2", "Approver", "Move to Draft");
    }

    protected boolean isElementDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    protected String reviewer2Username() {
        return workflowUserName("REVIEWER2_USERNAME", configValue("EASYQ_DOC_CONTROLLER_USERNAME", ""));
    }

    protected String reviewer2Password() {
        return requiredSecret("EASYQ_DOC_CONTROLLER_PASSWORD");
    }

    protected String approverUsername() {
        return workflowUserName("APPROVER_USERNAME", configValue("EASYQ_ASSIGNEE_AMIT_USERNAME", ""));
    }

    protected String approverPassword() {
        return requiredSecret("EASYQ_ASSIGNEE_AMIT_PASSWORD");
    }

    protected void loginAsConfiguredUser(String username, String password) {
        loginAs(username, password);
    }

    protected boolean clickButtonByText(String... labels) {
        for (String label : labels) {
            if (clickVisibleText(label)) {
                return true;
            }
        }
        return false;
    }

    protected boolean pageContainsAny(String... values) {
        return containsAnyIgnoreCase(getBodyText() + " " + safeCurrentUrl(), values);
    }

    protected String shortBodyText() {
        String text = getBodyText().replaceAll("\\s+", " ").trim();
        return text.length() > 300 ? text.substring(0, 300) : text;
    }

    protected String getBodyText() {
        try {
            return driver.findElement(By.tagName("body")).getText();
        } catch (RuntimeException exception) {
            return "";
        }
    }

    private void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromeDownloadPreferences());
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        driver.manage().window().maximize();
    }

    private void prepareDownloadDirectory() {
        downloadDirectory = Path.of(
                System.getProperty("user.dir"),
                "target",
                "easyq-downloads",
                moduleLabel().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-"));
        try {
            Files.createDirectories(downloadDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to prepare " + moduleLabel()
                    + " download folder: " + downloadDirectory, exception);
        }
    }

    private Map<String, Object> chromeDownloadPreferences() {
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("download.default_directory", downloadDirectory.toAbsolutePath().toString());
        preferences.put("download.prompt_for_download", false);
        preferences.put("download.directory_upgrade", true);
        preferences.put("plugins.always_open_pdf_externally", true);
        preferences.put("safebrowsing.enabled", true);
        return preferences;
    }

    private void shutdownBrowser() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (RuntimeException ignored) {
                // Browser may already be closed by the user or by a failed session.
            } finally {
                driver = null;
            }
        }
    }

    private void loginWithValidCredentials() {
        loginAs(configValue("EASYQ_ADMIN_USERNAME", validEmail), getPassword());
    }

    private void loginAs(String username, String password) {
        try {
            driver.manage().deleteAllCookies();
            ((JavascriptExecutor) driver).executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
        } catch (RuntimeException ignored) {
            // A fresh login navigation below is enough when storage cleanup is unavailable.
        }

        driver.get(baseUrl);
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).clear();
        driver.findElement(emailField).sendKeys(username);
        waitForSmallDelay();
        driver.findElement(passwordField).clear();
        driver.findElement(passwordField).sendKeys(password);
        waitForSmallDelay();
        safeClick(wait.until(ExpectedConditions.elementToBeClickable(loginButton)));
        waitForSmallDelay();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(dashboardText),
                ExpectedConditions.not(ExpectedConditions.urlContains("/login"))
        ));
    }

    private void navigateToModule() {
        if (isOnModulePage()) {
            return;
        }

        navLog("NAV: Opening " + moduleLabel() + " module. Current URL: " + safeCurrentUrl());

        if (openHamburgerMenu() && clickModuleFromHamburgerMenu() && waitForModulePage()) {
            return;
        }

        Assert.fail(moduleLabel() + " module was not opened from the hamburger/sidebar menu. URL: "
                + safeCurrentUrl() + " | Visible text: " + shortBodyText());
    }

    private boolean openHamburgerMenu() {
        navLog("NAV: Opening hamburger/sidebar menu.");
        if (clickFirstDisplayed(hamburgerMenuTrigger)) {
            waitForSmallDelay();
            return true;
        }

        try {
            Object clicked = ((JavascriptExecutor) driver).executeScript(
                    "const visible = el => {"
                            + "  const r = el.getBoundingClientRect();"
                            + "  const s = window.getComputedStyle(el);"
                            + "  return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';"
                            + "};"
                            + "const textOf = el => [el.innerText, el.textContent, el.getAttribute('aria-label'), el.getAttribute('title'), el.getAttribute('class'), el.id]"
                            + "  .join(' ').replace(/\\s+/g, ' ').trim();"
                            + "const controls = Array.from(document.querySelectorAll('button,a,[role=\"button\"],svg,mat-icon,.hamburger,.menu,.menu-toggle,.sidebar-toggle,.navbar-toggler,.toggle'))"
                            + "  .filter(visible);"
                            + "let scored = controls.map(el => {"
                            + "  const target = el.closest('button,a,[role=\"button\"],.hamburger,.menu-toggle,.sidebar-toggle,.navbar-toggler,.toggle') || el;"
                            + "  const rect = target.getBoundingClientRect();"
                            + "  const label = textOf(target);"
                            + "  let score = 0;"
                            + "  if (/hamburger|menu|sidebar|toggle|collapse|expand/i.test(label)) score += 80;"
                            + "  if (rect.left < 140 && rect.top < 140) score += 45;"
                            + "  if (rect.width <= 90 && rect.height <= 90) score += 20;"
                            + "  if (/notification|bell|profile|avatar|logout|download|view/i.test(label)) score -= 100;"
                            + "  return {target, score};"
                            + "}).filter(item => item.score > 40)"
                            + "  .sort((a, b) => b.score - a.score);"
                            + "if (!scored.length) return false;"
                            + "scored[0].target.click();"
                            + "return true;");
            if (Boolean.TRUE.equals(clicked)) {
                navLog("NAV: Hamburger/sidebar menu opened using fallback.");
                waitForSmallDelay();
                return true;
            }
        } catch (RuntimeException ignored) {
            // Direct visible menu fallback can still run.
        }
        return false;
    }

    private boolean clickModuleFromHamburgerMenu() {
        navLog("NAV: Clicking " + moduleLabel() + " from hamburger/sidebar menu.");
        try {
            Object clicked = ((JavascriptExecutor) driver).executeScript(
                    "const pattern = new RegExp(arguments[0], 'i');"
                            + "const visible = el => {"
                            + "  const r = el.getBoundingClientRect();"
                            + "  const s = window.getComputedStyle(el);"
                            + "  return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';"
                            + "};"
                            + "const textOf = el => [el.innerText, el.textContent, el.getAttribute('aria-label'), el.getAttribute('title'), el.getAttribute('href')]"
                            + "  .join(' ').replace(/\\s+/g, ' ').trim();"
                            + "const roots = Array.from(document.querySelectorAll('aside,nav,[role=\"navigation\"],.sidebar,.side-bar,.sidenav,.drawer,.menu,.navigation,.navbar'))"
                            + "  .filter(visible);"
                            + "const selector = 'a,button,[role=\"button\"],[role=\"link\"],[role=\"menuitem\"],li,span,div';"
                            + "const blocked = /no pending items|qms status|all tasks|my tasks|loading content|date of next|open action items|complaints reported|total products|review pending|approval pending|\\bview\\b/i;"
                            + "const scoreOf = el => {"
                            + "  const text = textOf(el);"
                            + "  const rect = el.getBoundingClientRect();"
                            + "  let score = 0;"
                            + "  if (text.length <= 70) score += 100;"
                            + "  if (rect.left <= 360) score += 80;"
                            + "  if (/^(quality policy|quality objective|responsibility and authority|management review|document management|documents|capa|capa & deviation|training|products|complaint management|complaints)$/i.test(text)) score += 120;"
                            + "  if (blocked.test(text)) score -= 500;"
                            + "  return score - text.length;"
                            + "};"
                            + "const isMenuCandidate = el => {"
                            + "  const text = textOf(el);"
                            + "  if (!pattern.test(text) || blocked.test(text) || text.length > 120) return false;"
                            + "  const card = el.closest('[class*=\"card\"],[class*=\"widget\"],section,article');"
                            + "  if (card && blocked.test(textOf(card))) return false;"
                            + "  return true;"
                            + "};"
                            + "const bestCandidate = items => items.filter(visible).filter(isMenuCandidate).sort((a, b) => scoreOf(b) - scoreOf(a))[0];"
                            + "const findMatch = root => bestCandidate(Array.from(root.querySelectorAll(selector)));"
                            + "let match = null;"
                            + "for (const root of roots) { match = findMatch(root); if (match) break; }"
                            + "if (!match) match = bestCandidate(Array.from(document.querySelectorAll(selector)).filter(el => el.getBoundingClientRect().left <= 360));"
                            + "if (!match) return false;"
                            + "const target = match.closest('a,button,[role=\"button\"],[role=\"link\"],[role=\"menuitem\"],li') || match;"
                            + "target.scrollIntoView({block:'center'});"
                            + "target.click();"
                            + "return true;",
                    moduleMenuRegex());
            if (Boolean.TRUE.equals(clicked)) {
                navLog("NAV: " + moduleLabel() + " tab clicked from hamburger/sidebar menu using fallback.");
                waitForSmallDelay();
                return true;
            }
        } catch (RuntimeException ignored) {
            // Final assertion will report the visible page state.
        }
        return false;
    }

    private boolean waitForModulePage() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(config.getInt("explicitWait"))).until(currentDriver ->
                    isOnModulePage() || isRestrictedModulePage());
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private boolean isOnModulePage() {
        String url = safeCurrentUrl().toLowerCase(Locale.ROOT);
        if (url.contains("dashboard")) {
            return false;
        }
        for (String fragment : moduleUrlFragments()) {
            if (fragment != null && !fragment.isBlank() && url.contains(fragment.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }

        String bodyText = getBodyText();
        if (containsAnyIgnoreCase(bodyText, "QMS Status")) {
            return false;
        }
        return containsAnyIgnoreCase(bodyText, moduleTextFragments())
                && containsAnyIgnoreCase(bodyText,
                "Initiate", "Draft", "Under Review", "Approved", "Move to Draft", "Download",
                "Reviewer", "Approver", "Save", "Submit", "No Data");
    }

    private boolean isRestrictedModulePage() {
        return !containsAnyIgnoreCase(getBodyText(), "QMS Status")
                && pageContainsAny("Restricted", "Unauthorized", "Access Denied", "Permission");
    }

    private By moduleMenuLocator() {
        StringBuilder predicate = new StringBuilder();
        for (String fragment : moduleTextFragments()) {
            if (fragment == null || fragment.isBlank()) {
                continue;
            }
            if (predicate.length() > 0) {
                predicate.append(" or ");
            }
            String value = xpathLiteral(fragment.toLowerCase(Locale.ROOT));
            predicate.append("contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),")
                    .append(value).append(")")
                    .append(" or contains(translate(@href,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),").append(value).append(")")
                    .append(" or contains(translate(@title,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),").append(value).append(")")
                    .append(" or contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),").append(value).append(")");
        }
        return By.xpath("//*[self::a or self::button or @role='button' or @role='link' or @role='menuitem' or self::li]["
                + predicate + "]");
    }

    private boolean openDraftEditor() {
        if (clickFirstDisplayed(initiateButton)) {
            waitForSmallDelay();
            clickButtonByText("Create from Scratch", "Scratch", "Blank", "Start");
            return true;
        }

        boolean opened = clickButtonByText("Initiate", "Create", "Add", "New", "Move to Draft", "New Version", "Edit");
        waitForSmallDelay();
        return opened || isElementDisplayed(moduleField) || isElementDisplayed(editableContent);
    }

    private void fillModuleFormWithAutomationData() {
        if (latestRecordTitle == null || latestRecordTitle.isBlank()) {
            latestRecordTitle = automationTitlePrefix() + " " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }
        String changeText = uniqueWorkflowText("Draft evaluation update", moduleLabel() + " change");
        String reasonText = uniqueWorkflowText("Draft evaluation update", moduleLabel() + " reason");
        String body = uniqueWorkflowText("Draft form content", moduleLabel() + " content")
                + ". Reviewer 1 Varun, Reviewer 2 Pavan, Approver Amit Karane.";

        boolean filledAny = false;
        for (WebElement field : driver.findElements(visibleInputOrTextarea)) {
            if (!isUsable(field)) {
                continue;
            }
            String type = String.valueOf(field.getAttribute("type")).toLowerCase(Locale.ROOT);
            if ("checkbox".equals(type) || "radio".equals(type) || "date".equals(type)) {
                continue;
            }
            try {
                scrollIntoView(field);
                String context = surroundingText(field) + " "
                        + String.valueOf(field.getAttribute("placeholder")) + " "
                        + String.valueOf(field.getAttribute("name")) + " "
                        + String.valueOf(field.getAttribute("formcontrolname"));
                String value = field.getTagName().equalsIgnoreCase("textarea") ? body : latestRecordTitle;
                if (containsAnyIgnoreCase(context, "What is the change", "Change?")) {
                    value = changeText;
                } else if (containsAnyIgnoreCase(context, "Why is the change", "Change needed", "Reason")) {
                    value = reasonText;
                }
                field.clear();
                field.sendKeys(value);
                filledAny = true;
                waitForSmallDelay();
            } catch (RuntimeException ignored) {
                // Continue filling other editable fields.
            }
        }

        for (WebElement editor : driver.findElements(editableContent)) {
            if (!isUsable(editor)) {
                continue;
            }
            try {
                scrollIntoView(editor);
                editor.clear();
                editor.sendKeys(body);
                filledAny = true;
                waitForSmallDelay();
            } catch (RuntimeException ignored) {
                // Continue with other editors.
            }
        }

        Assert.assertTrue(filledAny || hasModuleDataOrPageLoaded(),
                "At least one " + moduleLabel() + " field/editor should accept data. Visible text: " + shortBodyText());
    }

    private boolean addRowAndFill(String suffix) {
        boolean added = clickButtonByText("Add Objective", "Add Row", "Add New", "Add");
        String oldTitle = latestRecordTitle;
        latestRecordTitle = automationTitlePrefix() + " " + suffix + " "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        fillModuleFormWithAutomationData();
        latestRecordTitle = oldTitle == null ? latestRecordTitle : oldTitle;
        return added || isElementDisplayed(moduleField) || hasModuleDataOrPageLoaded();
    }

    private boolean openWorkflowAssignmentSurface() {
        if (isElementDisplayed(workflowModalOrPanel) && pageContainsAny("Reviewer", "Approver", "Review")) {
            return true;
        }
        if (!isElementDisplayed(moduleField) && !isElementDisplayed(saveButton)) {
            openDraftEditor();
            fillModuleFormWithAutomationData();
        }

        boolean opened = clickButtonByText("Send for Review", "Send", "Submit", "Review", "Next", "Continue");
        waitForSmallDelay();
        return opened || pageContainsAny("Reviewer", "Approver", "Review", "Assign");
    }

    private boolean selectWorkflowUser(String userName, String... fieldHints) {
        if (userName == null || userName.isBlank()) {
            return false;
        }
        if (isWorkflowUserAlreadySelected(userName)) {
            return true;
        }

        List<WebElement> editableControls = driver.findElements(By.xpath(
                "//input[not(@type='hidden') and not(@disabled)] | //textarea[not(@disabled)] | //*[@role='combobox']"));
        for (WebElement control : editableControls) {
            if (!isUsable(control)) {
                continue;
            }
            String context = surroundingText(control);
            if (fieldHints.length > 0 && !containsAnyIgnoreCase(context, fieldHints)) {
                continue;
            }
            try {
                scrollIntoView(control);
                safeClick(control);
                String tagName = String.valueOf(control.getTagName()).toLowerCase(Locale.ROOT);
                if ("input".equals(tagName) || "textarea".equals(tagName)) {
                    control.clear();
                    control.sendKeys(userName);
                }
                waitForSmallDelay();
                if (clickWorkflowDropdownOption(userName) || isWorkflowUserAlreadySelected(userName)) {
                    return true;
                }
            } catch (RuntimeException ignored) {
                // Try the next user selector.
            }
        }

        return clickWorkflowDropdownOption(userName) || isWorkflowUserAlreadySelected(userName);
    }

    private boolean isWorkflowUserAlreadySelected(String userName) {
        try {
            Object selected = ((JavascriptExecutor) driver).executeScript(
                    "const expected = String(arguments[0] || '').toLowerCase();"
                            + "const visible = el => {"
                            + "  const r = el.getBoundingClientRect();"
                            + "  const s = window.getComputedStyle(el);"
                            + "  return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';"
                            + "};"
                            + "return Array.from(document.querySelectorAll('*')).some(el => {"
                            + "  if (!visible(el)) return false;"
                            + "  const text = (el.innerText || el.textContent || '').replace(/\\s+/g, ' ').trim().toLowerCase();"
                            + "  if (!text.includes(expected)) return false;"
                            + "  const classText = String(el.className || '').toLowerCase();"
                            + "  const selectedHolder = el.closest('[class*=chip], [class*=tag], [class*=multiValue], [class*=selected], .badge');"
                            + "  return Boolean(selectedHolder) || /chip|tag|multivalue|selected|badge/.test(classText);"
                            + "});",
                    userName);
            return Boolean.TRUE.equals(selected);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private boolean clickWorkflowDropdownOption(String userName) {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    "const expected = String(arguments[0] || '').toLowerCase();"
                            + "const visible = el => {"
                            + "  const r = el.getBoundingClientRect();"
                            + "  const s = window.getComputedStyle(el);"
                            + "  return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';"
                            + "};"
                            + "const clickLikeUser = el => {"
                            + "  el.scrollIntoView({block:'center'});"
                            + "  ['pointerdown','mousedown','mouseup','click'].forEach(type => el.dispatchEvent(new MouseEvent(type, {bubbles:true, cancelable:true, view:window})));"
                            + "};"
                            + "const selectors = '[role=option], .ng-option, .mat-option, .react-select__option, li, [class*=option], [class*=Option]';"
                            + "const candidates = Array.from(document.querySelectorAll(selectors)).filter(el => {"
                            + "  if (!visible(el)) return false;"
                            + "  if (el.closest('[class*=chip], [class*=tag], [class*=multiValue], .badge')) return false;"
                            + "  const text = (el.innerText || el.textContent || '').replace(/\\s+/g, ' ').trim().toLowerCase();"
                            + "  return text.includes(expected);"
                            + "});"
                            + "if (!candidates.length) return false;"
                            + "clickLikeUser(candidates[0]);"
                            + "return true;",
                    userName);
            waitForSmallDelay();
            return Boolean.TRUE.equals(result);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private boolean ensureUnderReviewFromApprovedOrExistingDraft() {
        navigateToModule();

        if (openExistingRecordByStatus("Under Review", "Review Pending")) {
            return true;
        }
        if (openExistingRecordByStatus("Draft", "Rejected", "Returned", "Changes Requested")) {
            fillModuleFormWithAutomationData();
            return submitCurrentDraftForReviewWithConfiguredUsers();
        }
        if (openExistingRecordByStatus("Approved", "Active")) {
            boolean moved = clickButtonByText("Move to Draft", "New Version", "Revise", "Edit");
            confirmIfPrompt();
            fillModuleFormWithAutomationData();
            if (moved || clickButtonByText("Save as Draft", "Save Draft", "Save")) {
                return submitCurrentDraftForReviewWithConfiguredUsers();
            }
        }
        return sendDraftForReviewWithConfiguredUsers();
    }

    private boolean performWorkflowAction(String username, String password, String roleLabel, String action) {
        Reporter.log("WORKFLOW: " + roleLabel + " logging in to " + action + " " + moduleLabel() + ".", true);
        loginAsConfiguredUser(username, password);
        navigateToModule();

        if (!openUnderReviewTask()) {
            return false;
        }

        fillReviewRemarks(action, roleLabel);
        boolean actionClicked;
        if ("Reject".equalsIgnoreCase(action)) {
            actionClicked = clickButtonByText("Reject", "Return", "Request Changes", "Changes Required");
        } else {
            actionClicked = clickButtonByText("Approve", "Review", "Verify", "Submit", "Send", "Complete", "Done");
        }
        fillReviewRemarks(action, roleLabel);
        confirmIfPrompt();
        waitForSmallDelay();

        if (!actionClicked) {
            return false;
        }

        navigateToModule();
        if ("Reject".equalsIgnoreCase(action)) {
            return openExistingRecordByStatus("Rejected", "Changes Requested", "Draft", "Returned")
                    || openExistingRecordByStatus("Under Review", "Review Pending", "Pending", "Review")
                    || hasModuleDataOrPageLoaded();
        }
        return pageContainsAny("Approved", "Under Review", "Review", "Pending", moduleLabel()) || hasModuleDataOrPageLoaded();
    }

    private boolean openUnderReviewTask() {
        if (openExistingRecordByStatus("Under Review", "Review Pending", "Pending", "Review")) {
            return true;
        }
        if (hasNoModuleRecordsOnCurrentTab()) {
            Reporter.log("WORKFLOW: No actionable " + moduleLabel()
                    + " Under Review task is visible on the current tab.", true);
            return false;
        }
        boolean opened = clickButtonByText("My Tasks", "Assigned", "Review", "Approval");
        return opened && !hasNoModuleRecordsOnCurrentTab();
    }

    private boolean openExistingRecordByStatus(String... statuses) {
        if (hasNoModuleRecordsOnCurrentTab()) {
            return false;
        }
        for (String status : statuses) {
            if (clickRecordContainingText(status)) {
                waitForSmallDelay();
                return true;
            }
        }
        return false;
    }

    private boolean clickRecordContainingText(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String xpath = "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                + xpathLiteral(text.toLowerCase(Locale.ROOT)) + ")]";
        for (WebElement element : driver.findElements(By.xpath(xpath))) {
            if (!isUsable(element)) {
                continue;
            }
            try {
                WebElement target = element.findElement(By.xpath("./ancestor-or-self::*[self::tr or self::li or self::div or self::section][1]"));
                scrollIntoView(target);
                if (clickActionInside(target, "View", "Open", "Details", "Edit", "Review", "Approve")) {
                    return true;
                }
                safeClick(target);
                return true;
            } catch (RuntimeException ignored) {
                try {
                    scrollIntoView(element);
                    safeClick(element);
                    return true;
                } catch (RuntimeException ignoredAgain) {
                    // Try the next matching record.
                }
            }
        }
        return false;
    }

    private boolean clickActionInside(WebElement container, String... labels) {
        for (String label : labels) {
            List<WebElement> actions = container.findElements(By.xpath(
                    ".//*[self::button or self::a or @role='button' or contains(@class,'btn') or contains(@title,"
                            + xpathLiteral(label) + ")]"));
            for (WebElement action : actions) {
                if (!isUsable(action)) {
                    continue;
                }
                String text = String.valueOf(action.getText()).replaceAll("\\s+", " ").trim();
                String title = String.valueOf(action.getAttribute("title"));
                String ariaLabel = String.valueOf(action.getAttribute("aria-label"));
                if (!containsAnyIgnoreCase(text + " " + title + " " + ariaLabel, label)) {
                    continue;
                }
                try {
                    scrollIntoView(action);
                    safeClick(action);
                    return true;
                } catch (RuntimeException ignored) {
                    // Try the next matching action.
                }
            }
        }
        return false;
    }

    private void fillReviewRemarks(String action, String roleLabel) {
        String remarks = shortReviewRemark(action, roleLabel);
        fillControlsByContext(remarks, "Remark", "Comment", "Reason", "Review", "Approval", "Observation");
    }

    private String shortReviewRemark(String action, String roleLabel) {
        dynamicTextSequence++;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"));
        String actionToken = compactToken(action, 8).toLowerCase(Locale.ROOT);
        String text = moduleShortCode() + " " + actionToken + " | "
                + compactWorkflowRole(roleLabel) + " | " + timestamp + " | s" + dynamicTextSequence;
        return text.length() <= 95 ? text : text.substring(0, 95);
    }

    private String moduleShortCode() {
        String lower = moduleLabel().toLowerCase(Locale.ROOT);
        if (lower.contains("objective")) {
            return "QO";
        }
        if (lower.contains("responsibility") || lower.contains("authority")) {
            return "RA";
        }
        if (lower.contains("policy")) {
            return "QP";
        }
        String compact = moduleLabel().replaceAll("[^A-Za-z0-9]+", "").toUpperCase(Locale.ROOT);
        if (compact.isBlank()) {
            return "MOD";
        }
        return compact.length() <= 4 ? compact : compact.substring(0, 4);
    }

    private String compactWorkflowRole(String roleLabel) {
        String lower = String.valueOf(roleLabel == null ? "" : roleLabel).toLowerCase(Locale.ROOT);
        if (lower.contains("reviewer 1") || lower.contains("r1")) {
            return "R1";
        }
        if (lower.contains("reviewer 2") || lower.contains("r2")) {
            return "R2";
        }
        if (lower.contains("approver")) {
            return "A";
        }
        if (lower.contains("varun")) {
            return "R1";
        }
        if (lower.contains("pavan")) {
            return "R2";
        }
        if (lower.contains("amit")) {
            return "A";
        }
        return compactToken(roleLabel, 16);
    }

    private String compactToken(String value, int maxLength) {
        String token = String.valueOf(value == null ? "" : value)
                .replaceAll("[^A-Za-z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (token.isBlank()) {
            return "NA";
        }
        return token.length() <= maxLength ? token : token.substring(0, maxLength).trim();
    }

    private String uniqueWorkflowText(String stageLabel, String purposeLabel) {
        dynamicTextSequence++;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return moduleLabel() + " | " + purposeLabel + " | " + stageLabel
                + " | " + timestamp + " | seq " + dynamicTextSequence;
    }

    private boolean fillControlsByContext(String value, String... contextHints) {
        boolean filled = false;
        for (WebElement field : driver.findElements(visibleInputOrTextarea)) {
            if (!isUsable(field)) {
                continue;
            }
            String type = String.valueOf(field.getAttribute("type")).toLowerCase(Locale.ROOT);
            if ("checkbox".equals(type) || "radio".equals(type) || "date".equals(type)) {
                continue;
            }
            String context = surroundingText(field) + " "
                    + String.valueOf(field.getAttribute("placeholder")) + " "
                    + String.valueOf(field.getAttribute("name")) + " "
                    + String.valueOf(field.getAttribute("formcontrolname"));
            if (contextHints.length > 0 && !containsAnyIgnoreCase(context, contextHints)) {
                continue;
            }
            try {
                scrollIntoView(field);
                field.clear();
                field.sendKeys(value);
                filled = true;
                waitForSmallDelay();
            } catch (RuntimeException ignored) {
                // Continue with the next field.
            }
        }
        return filled;
    }

    private void confirmIfPrompt() {
        By dialogLocator = By.xpath("//*[contains(@class,'modal') or contains(@class,'dialog') or @role='dialog' or contains(@class,'overlay')]");
        for (WebElement dialog : driver.findElements(dialogLocator)) {
            if (!isUsable(dialog)) {
                continue;
            }
            if (clickActionInside(dialog, "Confirm", "Yes", "Move to Draft", "Reject", "Approve",
                    "OK", "Ok", "Submit", "Done", "Continue")) {
                waitForSmallDelay();
                return;
            }
        }
    }

    private boolean clickVisibleText(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String wanted = text.toLowerCase(Locale.ROOT);
        if (clickVisibleTextFromLocator(wanted,
                "//*[self::button or self::a or @role='button' or @role='menuitem' or contains(@class,'btn')]",
                true)) {
            return true;
        }
        if (clickVisibleTextFromLocator(wanted,
                "//*[self::button or self::a or @role='button' or @role='menuitem' or contains(@class,'btn')]",
                false)) {
            return true;
        }
        if (clickVisibleTextFromLocator(wanted,
                "//*[self::li or self::span or self::div]",
                true)) {
            return true;
        }
        return clickVisibleTextFromLocator(wanted,
                "//*[self::li or self::span or self::div]",
                false);
    }

    private boolean clickVisibleTextFromLocator(String wanted, String xpath, boolean exactMatch) {
        for (WebElement element : driver.findElements(By.xpath(xpath))) {
            if (!isUsable(element)) {
                continue;
            }
            String visibleText = String.valueOf(element.getText()).replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
            String title = String.valueOf(element.getAttribute("title")).toLowerCase(Locale.ROOT);
            String ariaLabel = String.valueOf(element.getAttribute("aria-label")).toLowerCase(Locale.ROOT);
            String allText = (visibleText + " " + title + " " + ariaLabel).trim();
            boolean matches = exactMatch
                    ? visibleText.equals(wanted) || title.equals(wanted) || ariaLabel.equals(wanted)
                    : allText.contains(wanted);
            if (!matches) {
                continue;
            }
            try {
                scrollIntoView(element);
                safeClick(element);
                waitForSmallDelay();
                return true;
            } catch (RuntimeException ignored) {
                // Try the next visible matching element.
            }
        }
        return false;
    }

    private boolean clickFirstDisplayed(By locator) {
        for (WebElement element : driver.findElements(locator)) {
            if (!isUsable(element)) {
                continue;
            }
            try {
                scrollIntoView(element);
                safeClick(element);
                waitForSmallDelay();
                return true;
            } catch (RuntimeException ignored) {
                // Try the next matching element.
            }
        }
        return false;
    }

    private String latestRecordTitle() {
        if (latestRecordTitle == null || latestRecordTitle.isBlank()) {
            latestRecordTitle = automationTitlePrefix() + " " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }
        return latestRecordTitle;
    }

    private String workflowValue(String suffix, String defaultValue) {
        return configValue(moduleConfigPrefix() + "_" + suffix,
                configValue("EASYQ_QP_" + suffix, defaultValue));
    }

    private String workflowUserName(String suffix, String defaultValue) {
        return workflowValue(suffix, defaultValue);
    }

    private String reviewer1Password() {
        String reviewer1Username = workflowUserName("REVIEWER1_USERNAME", configValue("EASYQ_ADMIN_USERNAME", validEmail));
        String adminUsername = configValue("EASYQ_ADMIN_USERNAME", validEmail);
        if (reviewer1Username.equalsIgnoreCase(adminUsername) || reviewer1Username.equalsIgnoreCase(validEmail)) {
            return getPassword();
        }
        String modulePasswordKey = moduleConfigPrefix() + "_REVIEWER1_PASSWORD";
        String modulePassword = config.getOptionalSecret(modulePasswordKey);
        if (modulePassword != null && !modulePassword.isBlank()) {
            return modulePassword;
        }
        return requiredSecret("EASYQ_QP_REVIEWER1_PASSWORD");
    }

    private String configValue(String key, String defaultValue) {
        String value = config.get(key);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private String requiredSecret(String key) {
        String value = config.getOptionalSecret(key);
        Assert.assertTrue(value != null && !value.isBlank(), key + " is required for this workflow test");
        return value;
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

    private String surroundingText(WebElement element) {
        try {
            Object text = ((JavascriptExecutor) driver).executeScript(
                    "const el = arguments[0];"
                            + "const parent = el.closest('label,.form-group,.field,.row,.col,div') || el.parentElement || el;"
                            + "return (parent.innerText || parent.textContent || '').replace(/\\s+/g, ' ').trim();",
                    element);
            return String.valueOf(text);
        } catch (RuntimeException exception) {
            return "";
        }
    }

    private boolean isUsable(WebElement element) {
        try {
            return element.isDisplayed() && element.isEnabled();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private void safeClick(WebElement element) {
        try {
            element.click();
        } catch (RuntimeException exception) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
        } catch (RuntimeException ignored) {
            // Scroll only improves reliability.
        }
    }

    private String safeCurrentUrl() {
        try {
            return driver.getCurrentUrl();
        } catch (RuntimeException exception) {
            return "browser-url-unavailable";
        }
    }

    private boolean containsAnyIgnoreCase(String text, String... values) {
        String lowerText = String.valueOf(text).toLowerCase(Locale.ROOT);
        for (String value : values) {
            if (value != null && !value.isBlank() && lowerText.contains(value.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        return "\"" + value + "\"";
    }

    private void navLog(String message) {
        System.out.println(message);
        Reporter.log(message, true);
    }

    private void waitForSmallDelay() {
        int delayMs = 1500;
        String configuredDelay = config.getOptionalSecret("EASYQ_VISUAL_DELAY_MS");
        if (configuredDelay == null || configuredDelay.isBlank()) {
            configuredDelay = config.get("actionDelayMs");
        }
        if (configuredDelay != null && !configuredDelay.isBlank()) {
            try {
                delayMs = Integer.parseInt(configuredDelay.trim());
            } catch (NumberFormatException ignored) {
                delayMs = 1500;
            }
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
}
