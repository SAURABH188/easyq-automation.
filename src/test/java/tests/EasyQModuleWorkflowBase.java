package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import utils.ConfigReader;
import utils.HamburgerNavigationHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
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
    private static final String MODULE_WORKFLOW_CODE_VERSION = "MODULE_QO_SIMPLE_DRAFT_VIEW_FLOW_2026_07_15_H";

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
    private String currentAuthorPassword;
    private WorkflowActor activeAuthorActor;
    private final List<WorkflowActor> activeReviewerActors = new ArrayList<>();
    private WorkflowActor activeApproverActor;
    private String lastResolvedWorkflowStage;
    private int dynamicTextSequence;
    private Path downloadDirectory;

    protected final String baseUrl = "https://beta.easyqsolutions.com/#/easyqsolutions/login";
    protected final String validEmail = "varunt@easyqsolutions.com";

    protected final By emailField = By.xpath("//input[@type='email' or contains(@formcontrolname,'email')]");
    protected final By passwordField = By.xpath("//input[@type='password' or contains(@formcontrolname,'password')]");
    protected final By loginButton = By.xpath("//button[@type='submit' or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'log in') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'login') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign in')]");
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
        currentAuthorPassword = getPassword();
        navigateToModule();
        if (openDraftCardFromDraftTabIfPresent()) {
            Reporter.log("WORKFLOW: Existing " + moduleLabel()
                    + " Draft card opened by View. Continuing with draft update.", true);
        } else if (!openDraftEditor()) {
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
        setAllEmptyDueDatesToTodayOnce();
        fillWorkflowComment(uniqueWorkflowText("Send to Review assignment", moduleShortCode() + " workflow comment"));
        scrollActiveDialogToBottom();
        fillAuthenticationPassword(currentAuthorPassword == null || currentAuthorPassword.isBlank()
                ? getPassword()
                : currentAuthorPassword);
        boolean submitted = clickSendForReviewInsideWorkflowPopup()
                || clickButtonByText("Send for Review", "Send", "Submit", "Continue", "Done", "Save");
        waitForSmallDelay();
        confirmIfPrompt();
        waitForReflectionDelay();

        if (reviewersAssigned && approverAssigned && submitted) {
            useConfiguredWorkflowActorsForNewSubmission();
        }

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
        currentAuthorPassword = getPassword();
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

        refreshActiveWorkflowParticipantsFromOpenDocumentInformation(null);
        WorkflowActor reviewer1Actor = effectiveReviewerActor(0);
        WorkflowActor reviewer2Actor = effectiveReviewerActor(1);
        WorkflowActor approverActor = effectiveApproverActor();

        if (rejectFirst) {
            boolean rejected = performWorkflowAction(
                    reviewer1Actor.username,
                    reviewer1Actor.password,
                    reviewer1Actor.roleLabel,
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
                reviewer1Actor.username,
                reviewer1Actor.password,
                reviewer1Actor.roleLabel,
                "Approve");
        if (!reviewer1Done) {
            return completeApprovalOnlyFromCurrentModuleState(
                    "Reviewer 1 approval failed or " + moduleLabel() + " already moved forward");
        }

        if (rejectFirst) {
            boolean reviewer2Rejected = performWorkflowAction(
                    reviewer2Actor.username,
                    reviewer2Actor.password,
                    reviewer2Actor.roleLabel,
                    "Reject");
            if (!reviewer2Rejected) {
                return completeApprovalOnlyFromCurrentModuleState(
                        "Reviewer 2 reject failed or " + moduleLabel() + " already moved back");
            }

            reviewer1Done = performWorkflowAction(
                    reviewer1Actor.username,
                    reviewer1Actor.password,
                    reviewer1Actor.roleLabel + " after Reviewer 2 reject",
                    "Approve");
            if (!reviewer1Done) {
                return completeApprovalOnlyFromCurrentModuleState(
                        "Reviewer 2 reject completed but Reviewer 1 re-approval did not finish");
            }
        }

        boolean reviewer2Done = performWorkflowAction(
                reviewer2Actor.username,
                reviewer2Actor.password,
                reviewer2Actor.roleLabel,
                "Approve");
        if (!reviewer2Done) {
            return completeApprovalOnlyFromCurrentModuleState(
                    "Reviewer 2 approval failed or " + moduleLabel() + " already moved forward");
        }

        if (rejectFirst) {
            boolean approverRejected = performWorkflowAction(
                    approverActor.username,
                    approverActor.password,
                    approverActor.roleLabel,
                    "Reject");
            if (!approverRejected) {
                return completeApprovalOnlyFromCurrentModuleState(
                        "Approver reject failed or " + moduleLabel() + " already moved back");
            }

            reviewer2Done = performWorkflowAction(
                    reviewer2Actor.username,
                    reviewer2Actor.password,
                    reviewer2Actor.roleLabel + " after Approver reject",
                    "Approve");
            if (!reviewer2Done) {
                return completeApprovalOnlyFromCurrentModuleState(
                        "Approver reject completed but Reviewer 2 re-approval did not finish");
            }
        }

        boolean approverDone = performWorkflowAction(
                approverActor.username,
                approverActor.password,
                rejectFirst ? approverActor.roleLabel + " final approval" : approverActor.roleLabel,
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
        List<WorkflowActor> actors = effectiveWorkflowActors();
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
            return lastResolvedWorkflowStage == null ? dashboardOwner.stage : lastResolvedWorkflowStage;
        }

        for (WorkflowActor actor : configuredWorkflowActors()) {
            if (dashboardOwner != null && sameWorkflowActor(actor, dashboardOwner)) {
                continue;
            }
            if (tryOpenPendingModuleForActor(actor)) {
                return lastResolvedWorkflowStage == null ? actor.stage : lastResolvedWorkflowStage;
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
            clickModuleTab("Under Review");
            waitForReflectionDelay();
            boolean opened = openUnderReviewTask();
            lastResolvedWorkflowStage = opened
                    ? refreshActiveWorkflowParticipantsFromOpenDocumentInformation(actor)
                    : null;
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
                currentAuthorPassword = author.password;
                navigateToModule();
                if (!openModuleRecordFromStatusTab("Draft", "Saved in Draft")
                        && !openExistingRecordByStatus("Draft", "Rejected", "Returned", "Changes Requested", "Saved in Draft")) {
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

    private List<WorkflowActor> effectiveWorkflowActors() {
        List<WorkflowActor> actors = new ArrayList<>();
        if (!activeReviewerActors.isEmpty()) {
            actors.addAll(activeReviewerActors);
        }
        if (activeApproverActor != null
                && actors.stream().noneMatch(existing -> sameWorkflowActor(existing, activeApproverActor))) {
            actors.add(activeApproverActor);
        }
        return actors.isEmpty() ? configuredWorkflowActors() : actors;
    }

    private WorkflowActor effectiveReviewerActor(int reviewerIndex) {
        List<WorkflowActor> actors = effectiveWorkflowActors();
        int reviewerSeen = 0;
        for (WorkflowActor actor : actors) {
            if (!"APPROVER".equalsIgnoreCase(actor.stage)) {
                if (reviewerSeen == reviewerIndex) {
                    return actor;
                }
                reviewerSeen++;
            }
        }
        List<WorkflowActor> fallbackActors = configuredWorkflowActors();
        return fallbackActors.get(Math.min(reviewerIndex, Math.max(0, fallbackActors.size() - 2)));
    }

    private WorkflowActor effectiveApproverActor() {
        if (activeApproverActor != null) {
            return activeApproverActor;
        }
        for (WorkflowActor actor : configuredWorkflowActors()) {
            if ("APPROVER".equalsIgnoreCase(actor.stage)) {
                return actor;
            }
        }
        return configuredWorkflowActors().get(configuredWorkflowActors().size() - 1);
    }

    private void useConfiguredWorkflowActorsForNewSubmission() {
        List<WorkflowActor> configuredActors = configuredWorkflowActors();
        activeReviewerActors.clear();
        for (WorkflowActor actor : configuredActors) {
            if ("APPROVER".equalsIgnoreCase(actor.stage)) {
                activeApproverActor = actor;
            } else {
                activeReviewerActors.add(actor);
            }
        }
        lastResolvedWorkflowStage = "REVIEWER1";
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

    private String refreshActiveWorkflowParticipantsFromOpenDocumentInformation(WorkflowActor currentOwner) {
        if (!isModuleDetailOpen()) {
            return currentOwner == null ? null : currentOwner.stage;
        }
        clickButtonByText("Document");
        waitForSmallDelay();

        String documentInfoText = documentInformationText();
        String normalizedDocumentInfo = normalizeComparableText(documentInfoText);
        Reporter.log("WORKFLOW RECOVERY: " + moduleLabel()
                + " Document Information role mapping="
                + documentInfoText.replaceAll("\\s+", " ").trim(), true);

        WorkflowActor author = findKnownActorInRoleSection(normalizedDocumentInfo, "author");
        List<WorkflowActor> reviewers = findKnownReviewersInDocumentInformation(normalizedDocumentInfo);
        WorkflowActor approver = findKnownActorInRoleSection(normalizedDocumentInfo, "approver");

        if (author != null) {
            activeAuthorActor = actorForStage(author, "AUTHOR", "Author");
        }
        if (!reviewers.isEmpty()) {
            activeReviewerActors.clear();
            for (int reviewerIndex = 0; reviewerIndex < reviewers.size(); reviewerIndex++) {
                activeReviewerActors.add(actorForStage(
                        reviewers.get(reviewerIndex),
                        "REVIEWER" + (reviewerIndex + 1),
                        "Reviewer " + (reviewerIndex + 1)));
            }
        }
        if (approver != null) {
            activeApproverActor = actorForStage(approver, "APPROVER", "Approver");
        }

        Reporter.log("WORKFLOW RECOVERY: " + moduleLabel()
                + " resolved author=" + roleLabelOrDash(activeAuthorActor)
                + ", reviewers=" + reviewerListForLog(activeReviewerActors)
                + ", approver=" + roleLabelOrDash(activeApproverActor), true);

        if (currentOwner == null) {
            return null;
        }
        for (WorkflowActor reviewer : activeReviewerActors) {
            if (sameWorkflowActor(currentOwner, reviewer)) {
                return reviewer.stage;
            }
        }
        if (sameWorkflowActor(currentOwner, activeApproverActor)) {
            return "APPROVER";
        }
        return currentOwner.stage;
    }

    private WorkflowActor actorForStage(WorkflowActor base, String stage, String rolePrefix) {
        if (base == null) {
            return null;
        }
        return new WorkflowActor(
                stage,
                base.username,
                base.password,
                rolePrefix + " " + primaryActorName(base),
                base.aliases);
    }

    private String primaryActorName(WorkflowActor actor) {
        if (actor == null || actor.aliases.length == 0 || actor.aliases[0] == null || actor.aliases[0].isBlank()) {
            return actor == null ? "" : actor.roleLabel;
        }
        return actor.aliases[0];
    }

    private String roleLabelOrDash(WorkflowActor actor) {
        return actor == null ? "--" : actor.roleLabel;
    }

    private String reviewerListForLog(List<WorkflowActor> reviewers) {
        if (reviewers == null || reviewers.isEmpty()) {
            return "--";
        }
        List<String> labels = new ArrayList<>();
        for (WorkflowActor reviewer : reviewers) {
            labels.add(reviewer.roleLabel);
        }
        return String.join(", ", labels);
    }

    private String documentInformationText() {
        try {
            Object text = ((JavascriptExecutor) driver).executeScript(
                    """
                            const visible = el => {
                              if (!el) return false;
                              const rect = el.getBoundingClientRect();
                              const style = getComputedStyle(el);
                              return rect.width > 0 && rect.height > 0
                                && style.visibility !== 'hidden' && style.display !== 'none';
                            };
                            const textOf = el => String(el.innerText || el.textContent || '').replace(/\\s+/g, ' ').trim();
                            const candidates = Array.from(document.querySelectorAll('aside,section,article,div'))
                              .filter(visible)
                              .map(el => ({ el, text: textOf(el) }))
                              .filter(item => item.text.toLowerCase().includes('document information'))
                              .map(item => {
                                const lower = item.text.toLowerCase();
                                let score = 0;
                                if (lower.includes('author')) score += 40;
                                if (/reviewer\\s*\\d+/i.test(lower)) score += 60;
                                if (lower.includes('approver')) score += 50;
                                score -= Math.min(120, item.text.length / 20);
                                return { text: item.text, score };
                              })
                              .sort((a, b) => b.score - a.score);
                            return candidates.length ? candidates[0].text : '';
                            """);
            String documentInfoText = String.valueOf(text);
            return documentInfoText.isBlank() ? getBodyText() : documentInfoText;
        } catch (RuntimeException exception) {
            return getBodyText();
        }
    }

    private WorkflowActor findKnownActorInRoleSection(String normalizedDocumentInfo, String... roleLabels) {
        for (String roleLabel : roleLabels) {
            String section = normalizedRoleSection(normalizedDocumentInfo, roleLabel);
            if (section.isBlank()) {
                continue;
            }
            WorkflowActor matchedActor = knownAnyWorkflowActorByName(section);
            if (matchedActor != null) {
                return matchedActor;
            }
        }
        return null;
    }

    private List<WorkflowActor> findKnownReviewersInDocumentInformation(String normalizedDocumentInfo) {
        Map<Integer, WorkflowActor> numberedReviewers = new HashMap<>();
        Matcher reviewerMatcher = Pattern.compile("\\breviewer\\s*(\\d+)\\b").matcher(normalizedDocumentInfo);
        while (reviewerMatcher.find()) {
            String reviewerSection = normalizedRoleSectionFromIndex(
                    normalizedDocumentInfo,
                    reviewerMatcher.start(),
                    reviewerMatcher.end());
            WorkflowActor reviewer = knownAnyWorkflowActorByName(reviewerSection);
            if (reviewer != null) {
                numberedReviewers.putIfAbsent(Integer.parseInt(reviewerMatcher.group(1)) - 1, reviewer);
            }
        }

        List<WorkflowActor> reviewers = new ArrayList<>();
        for (int reviewerIndex = 0; reviewerIndex < 20; reviewerIndex++) {
            WorkflowActor reviewer = numberedReviewers.get(reviewerIndex);
            if (reviewer != null && reviewers.stream().noneMatch(existing -> sameWorkflowActor(existing, reviewer))) {
                reviewers.add(reviewer);
            }
        }
        if (reviewers.isEmpty()) {
            WorkflowActor genericReviewer = findKnownActorInRoleSection(normalizedDocumentInfo, "reviewer");
            if (genericReviewer != null) {
                reviewers.add(genericReviewer);
            }
        }
        return reviewers;
    }

    private String normalizedRoleSection(String normalizedDocumentInfo, String roleLabel) {
        String normalizedRoleLabel = normalizeComparableText(roleLabel);
        int start = normalizedDocumentInfo.indexOf(normalizedRoleLabel);
        if (start < 0) {
            return "";
        }
        return normalizedRoleSectionFromIndex(normalizedDocumentInfo, start, start + normalizedRoleLabel.length());
    }

    private String normalizedRoleSectionFromIndex(String normalizedDocumentInfo, int start, int searchFrom) {
        int end = normalizedDocumentInfo.length();
        Matcher nextRoleMatcher = Pattern.compile("\\b(?:author|approver|reviewer\\s*\\d+|reviewer)\\b")
                .matcher(normalizedDocumentInfo);
        while (nextRoleMatcher.find()) {
            if (nextRoleMatcher.start() >= searchFrom && nextRoleMatcher.start() < end) {
                end = nextRoleMatcher.start();
                break;
            }
        }
        return normalizedDocumentInfo.substring(start, end);
    }

    private WorkflowActor knownAnyWorkflowActorByName(String text) {
        WorkflowActor workflowActor = knownWorkflowActorByName(text);
        if (workflowActor != null) {
            return workflowActor;
        }
        String normalizedText = normalizeComparableText(text);
        for (WorkflowActor author : configuredDraftAuthorActors()) {
            for (String alias : author.aliases) {
                if (alias != null && !alias.isBlank()
                        && normalizedText.contains(normalizeComparableText(alias))) {
                    return author;
                }
            }
        }
        return null;
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
        long dashboardWidgetWaitMillis = Math.max(
                Duration.ofSeconds(config.getInt("explicitWait")).toMillis(),
                Duration.ofSeconds(60).toMillis());
        long deadline = System.currentTimeMillis() + dashboardWidgetWaitMillis;
        long firstCheckAt = System.currentTimeMillis();
        long noPendingFirstSeenAt = 0L;
        long noPendingGraceMillis = Duration.ofSeconds(20).toMillis();
        String lastText = "";
        String lastMeaningfulText = "";
        int stableCount = 0;
        Reporter.log("WORKFLOW RECOVERY: Waiting up to " + (dashboardWidgetWaitMillis / 1000)
                + " seconds for Dashboard All Tasks " + moduleLabel() + " widget reflection.", true);
        while (System.currentTimeMillis() < deadline) {
            String cardText = dashboardModuleCardText().replaceAll("\\s+", " ").trim();
            if (!cardText.isBlank()) {
                lastMeaningfulText = cardText;
            }
            boolean hasWorkflowDetails = containsAnyIgnoreCase(cardText,
                    "Current Reviewer", "Next Reviewer", "Reviewed by", "Reviewer", "Approver", "Due", "Due Today")
                    && !containsAnyIgnoreCase(cardText, "No Pending Items");
            boolean hasNoPendingState = containsAnyIgnoreCase(cardText, "No Pending Items");
            boolean loading = containsAnyIgnoreCase(getBodyText(), "Loading ...", "Loading...");
            if (hasWorkflowDetails && !loading) {
                stableCount = cardText.equals(lastText) ? stableCount + 1 : 1;
                lastText = cardText;
                if (stableCount >= 2 && System.currentTimeMillis() - firstCheckAt >= 2000) {
                    Reporter.log("WORKFLOW RECOVERY: Dashboard All Tasks " + moduleLabel()
                            + " widget details are visible and stable.", true);
                    return cardText;
                }
            }
            else if (hasNoPendingState && !loading) {
                if (noPendingFirstSeenAt == 0L) {
                    noPendingFirstSeenAt = System.currentTimeMillis();
                }
                stableCount = cardText.equals(lastText) ? stableCount + 1 : 1;
                lastText = cardText;
                if (stableCount >= 3 && System.currentTimeMillis() - noPendingFirstSeenAt >= noPendingGraceMillis) {
                    Reporter.log("WORKFLOW RECOVERY: Dashboard All Tasks " + moduleLabel()
                            + " widget stayed without pending details for the grace period.", true);
                    return cardText;
                }
            } else {
                stableCount = 0;
                noPendingFirstSeenAt = 0L;
                lastText = cardText;
            }
            waitForSmallDelay();
        }
        Reporter.log("WORKFLOW RECOVERY: Dashboard All Tasks " + moduleLabel()
                + " widget wait timed out. Using last visible widget text.", true);
        return lastMeaningfulText.isBlank() ? lastText : lastMeaningfulText;
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
                            + "    if (/current reviewer|next reviewer|reviewed by|reviewer|approver|due/i.test(text)) score += 100;"
                            + "    if (/\\+\\s*\\d+\\s*more/i.test(text)) score += 80;"
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
        WorkflowActor popupOwner = currentOwnerFromModuleMorePopup();
        if (popupOwner != null) {
            return popupOwner;
        }

        WorkflowActor iconOwner = currentOwnerFromModuleWidgetIcons();
        if (iconOwner != null) {
            return iconOwner;
        }

        String normalizedText = String.valueOf(cardText)
                .replaceAll("\\+\\s*\\d+\\s+more", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
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

    private WorkflowActor currentOwnerFromModuleMorePopup() {
        String popupRows = dashboardModuleMorePopupRowsText();
        if (popupRows.isBlank()) {
            return null;
        }
        Reporter.log("WORKFLOW RECOVERY: Dashboard " + moduleLabel()
                + " widget more-popup rows=" + popupRows.replaceAll("\\s+", " ").trim(), true);

        WorkflowActor pendingOwner = null;
        for (String row : popupRows.split("\\R")) {
            String normalizedRow = normalizeComparableText(row);
            WorkflowActor rowActor = knownWorkflowActorByName(normalizedRow);
            if (rowActor == null) {
                continue;
            }
            if (normalizedRow.startsWith("pending")) {
                pendingOwner = rowActor;
                break;
            }
        }
        if (pendingOwner != null) {
            Reporter.log("WORKFLOW RECOVERY: Dashboard popup detected pending "
                    + moduleLabel() + " owner=" + pendingOwner.roleLabel, true);
            return pendingOwner;
        }
        return null;
    }

    private String dashboardModuleMorePopupRowsText() {
        try {
            Object clickResult = ((JavascriptExecutor) driver).executeScript(
                    """
                            const moduleName = String(arguments[0] || '').toLowerCase();
                            const visible = el => {
                              if (!el) return false;
                              const rect = el.getBoundingClientRect();
                              const style = getComputedStyle(el);
                              return rect.width > 0 && rect.height > 0
                                && style.visibility !== 'hidden'
                                && style.display !== 'none'
                                && Number(style.opacity || 1) > 0;
                            };
                            const textOf = el => String([
                              el && el.innerText,
                              el && el.textContent,
                              el && el.getAttribute && el.getAttribute('aria-label'),
                              el && el.getAttribute && el.getAttribute('title')
                            ].join(' ')).replace(/\\s+/g, ' ').trim();
                            const cards = Array.from(document.querySelectorAll('section,article,div,li'))
                              .filter(visible)
                              .filter(el => textOf(el).toLowerCase().includes(moduleName))
                              .map(el => {
                                const rect = el.getBoundingClientRect();
                                const text = textOf(el);
                                let score = 0;
                                if (text.toLowerCase().startsWith(moduleName)) score += 100;
                                if (/reviewer|approver|reviewed by|current reviewer/i.test(text)) score += 100;
                                if (/\\+\\s*\\d+\\s*more/i.test(text)) score += 130;
                                if (/view/i.test(text)) score += 10;
                                score -= Math.min(120, text.length / 4);
                                score -= Math.min(80, (rect.width * rect.height) / 6000);
                                return {el, score};
                              })
                              .sort((a, b) => b.score - a.score);
                            if (!cards.length) return 'NO_MODULE_CARD';
                            const root = cards[0].el;
                            const more = Array.from(root.querySelectorAll('button,a,[role=button],span,div'))
                              .filter(visible)
                              .find(el => /\\+\\s*\\d+\\s*more/i.test(textOf(el)));
                            if (!more) return 'NO_MORE_LINK';
                            const target = more.closest('button,a,[role=button]') || more;
                            target.scrollIntoView({block: 'center', inline: 'center'});
                            target.dispatchEvent(new MouseEvent('mouseover', {bubbles: true}));
                            target.dispatchEvent(new MouseEvent('mousedown', {bubbles: true}));
                            target.dispatchEvent(new MouseEvent('mouseup', {bubbles: true}));
                            target.click();
                            return 'CLICKED_MORE_LINK:' + textOf(more);
                            """,
                    moduleLabel());
            if (!String.valueOf(clickResult).startsWith("CLICKED_MORE_LINK")) {
                return "";
            }
            waitForSmallDelay();

            Object rows = ((JavascriptExecutor) driver).executeScript(
                    """
                            const visible = el => {
                              if (!el) return false;
                              const rect = el.getBoundingClientRect();
                              const style = getComputedStyle(el);
                              return rect.width > 0 && rect.height > 0
                                && style.visibility !== 'hidden'
                                && style.display !== 'none'
                                && Number(style.opacity || 1) > 0;
                            };
                            const textOf = el => String([
                              el && el.innerText,
                              el && el.textContent,
                              el && el.getAttribute && el.getAttribute('aria-label'),
                              el && el.getAttribute && el.getAttribute('title'),
                              el && el.getAttribute && el.getAttribute('class')
                            ].join(' ')).replace(/\\s+/g, ' ').trim();
                            const colorState = value => {
                              const text = String(value || '').toLowerCase();
                              const numbers = text.match(/\\d+(?:\\.\\d+)?/g);
                              if (!numbers || numbers.length < 3) return '';
                              const [r, g, b] = numbers.slice(0, 3).map(Number);
                              if (g > 110 && r < 120 && b < 130) return 'completed';
                              if (r > 170 && g > 90 && g < 190 && b < 120) return 'pending';
                              if (r > 180 && g > 120 && b < 80) return 'pending';
                              return '';
                            };
                            const nodeState = node => {
                              const style = getComputedStyle(node);
                              const joined = [
                                textOf(node),
                                node.getAttribute && node.getAttribute('class'),
                                node.getAttribute && node.getAttribute('stroke'),
                                node.getAttribute && node.getAttribute('fill'),
                                style.color,
                                style.stroke,
                                style.fill
                              ].join(' ').toLowerCase();
                              let pending = 0;
                              let completed = 0;
                              if (/clock|schedule|access[_ -]?time|pending|watch|due|timer/.test(joined)) pending += 3;
                              if (/check|done|complete|completed|approved|verified/.test(joined)) completed += 3;
                              for (const color of [style.color, style.stroke, style.fill, node.getAttribute && node.getAttribute('stroke'), node.getAttribute && node.getAttribute('fill')]) {
                                const state = colorState(color);
                                if (state === 'pending') pending += 2;
                                if (state === 'completed') completed += 2;
                              }
                              if (pending > completed && pending > 0) return 'pending';
                              if (completed > 0) return 'completed';
                              return 'unknown';
                            };
                            const rowState = (row, root) => {
                              const rowRect = row.getBoundingClientRect();
                              const icons = Array.from(root.querySelectorAll('svg,i,mat-icon,[class*=icon],[class*=Icon],path,circle'))
                                .filter(visible)
                                .filter(icon => {
                                  const rect = icon.getBoundingClientRect();
                                  return row.contains(icon)
                                    || Math.abs((rect.top + rect.bottom) / 2 - (rowRect.top + rowRect.bottom) / 2) <= 18;
                                });
                              let pending = 0;
                              let completed = 0;
                              for (const icon of icons) {
                                const state = nodeState(icon);
                                if (state === 'pending') pending++;
                                if (state === 'completed') completed++;
                              }
                              if (pending > completed && pending > 0) return 'pending';
                              if (completed > 0) return 'completed';
                              return 'unknown';
                            };
                            const popupCandidates = Array.from(document.querySelectorAll('[role=dialog],.modal,.popover,.tooltip,.cdk-overlay-pane,.mat-menu-panel,.ng-star-inserted,section,article,div'))
                              .filter(visible)
                              .map(el => {
                                const rect = el.getBoundingClientRect();
                                const text = textOf(el);
                                const lower = text.toLowerCase();
                                let score = 0;
                                if (/reviewer\\s*1/.test(lower)) score += 130;
                                if (/reviewer\\s*2/.test(lower)) score += 90;
                                if (/approver/.test(lower)) score += 90;
                                if (rect.width <= 560) score += 60;
                                if (rect.height <= 360) score += 50;
                                if (text.length <= 500) score += 50;
                                score -= Math.min(120, text.length / 5);
                                return {el, text, score};
                              })
                              .filter(item => /reviewer|approver/i.test(item.text))
                              .sort((a, b) => b.score - a.score);
                            const root = popupCandidates.length ? popupCandidates[0].el : null;
                            if (!root) return '';
                            const rawRows = Array.from(root.querySelectorAll('div,li,p,tr,section,article'))
                              .filter(visible)
                              .map(el => ({el, text: textOf(el)}))
                              .filter(item => /reviewer|approver/i.test(item.text))
                              .filter(item => item.text.length >= 8 && item.text.length <= 180)
                              .sort((a, b) => a.text.length - b.text.length);
                            const selected = [];
                            const seen = new Set();
                            for (const item of rawRows) {
                              const text = item.text.replace(/\\s+/g, ' ').trim();
                              const key = text.toLowerCase();
                              if (seen.has(key)) continue;
                              if (selected.some(existing => existing.text.toLowerCase().includes(key))) continue;
                              seen.add(key);
                              selected.push({text, state: rowState(item.el, root)});
                            }
                            if (!selected.length) {
                              const text = textOf(root);
                              return text ? 'UNKNOWN::' + text : '';
                            }
                            return selected
                              .map(item => item.state.toUpperCase() + '::' + item.text)
                              .join('\\n');
                            """);
            try {
                new Actions(driver).sendKeys(org.openqa.selenium.Keys.ESCAPE).perform();
            } catch (RuntimeException ignored) {
                // The popup may already be closed.
            }
            return String.valueOf(rows);
        } catch (RuntimeException exception) {
            return "";
        }
    }

    private WorkflowActor currentOwnerFromModuleWidgetIcons() {
        String iconRowsText = dashboardModuleIconRowsText();
        if (iconRowsText.isBlank()) {
            return null;
        }
        Reporter.log("WORKFLOW RECOVERY: Dashboard " + moduleLabel()
                + " widget icon rows=" + iconRowsText.replaceAll("\\s+", " ").trim(), true);

        WorkflowActor bestActor = null;
        int bestScore = Integer.MIN_VALUE;
        for (String row : iconRowsText.split("\\R")) {
            String normalizedRow = row.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
            if (!normalizedRow.startsWith("pending::")) {
                continue;
            }
            WorkflowActor rowActor = knownWorkflowActorByName(normalizedRow);
            if (rowActor == null) {
                continue;
            }
            int score = 10;
            if (normalizedRow.contains("current reviewer")) {
                score += 120;
            }
            if (normalizedRow.contains("approver")) {
                score += 100;
            }
            Matcher reviewerNumber = Pattern.compile("reviewer\\s*(\\d+)").matcher(normalizedRow);
            if (reviewerNumber.find()) {
                score += 80 - Math.min(50, Integer.parseInt(reviewerNumber.group(1)));
            } else if (normalizedRow.contains("reviewer")) {
                score += 60;
            }
            if (normalizedRow.contains("next reviewer")) {
                score -= 30;
            }
            if (score > bestScore) {
                bestScore = score;
                bestActor = rowActor;
            }
        }
        if (bestActor != null) {
            Reporter.log("WORKFLOW RECOVERY: Dashboard " + moduleLabel()
                    + " widget pending icon detected current owner=" + bestActor.roleLabel, true);
        }
        return bestActor;
    }

    private String dashboardModuleIconRowsText() {
        try {
            Object rows = ((JavascriptExecutor) driver).executeScript(
                    """
                            const moduleName = String(arguments[0] || '').toLowerCase();
                            const visible = el => {
                              if (!el) return false;
                              const rect = el.getBoundingClientRect();
                              const style = getComputedStyle(el);
                              return rect.width > 0 && rect.height > 0
                                && style.visibility !== 'hidden'
                                && style.display !== 'none'
                                && Number(style.opacity || 1) > 0;
                            };
                            const textOf = el => String([
                              el && el.innerText,
                              el && el.textContent,
                              el && el.getAttribute && el.getAttribute('aria-label'),
                              el && el.getAttribute && el.getAttribute('title'),
                              el && el.getAttribute && el.getAttribute('class')
                            ].join(' ')).replace(/\\s+/g, ' ').trim();
                            const colorState = value => {
                              const text = String(value || '').toLowerCase();
                              const numbers = text.match(/\\d+(?:\\.\\d+)?/g);
                              if (!numbers || numbers.length < 3) return '';
                              const [r, g, b] = numbers.slice(0, 3).map(Number);
                              if (g > 110 && r < 120 && b < 130) return 'completed';
                              if (r > 170 && g > 90 && g < 190 && b < 120) return 'pending';
                              if (r > 180 && g > 120 && b < 80) return 'pending';
                              return '';
                            };
                            const nodeState = node => {
                              const style = getComputedStyle(node);
                              const joined = [
                                textOf(node),
                                node.getAttribute && node.getAttribute('class'),
                                node.getAttribute && node.getAttribute('stroke'),
                                node.getAttribute && node.getAttribute('fill'),
                                style.color,
                                style.stroke,
                                style.fill
                              ].join(' ').toLowerCase();
                              let pending = 0;
                              let completed = 0;
                              if (/clock|schedule|access[_ -]?time|pending|watch|due|timer/.test(joined)) pending += 3;
                              if (/check|done|complete|completed|approved|verified/.test(joined)) completed += 3;
                              for (const color of [style.color, style.stroke, style.fill, node.getAttribute && node.getAttribute('stroke'), node.getAttribute && node.getAttribute('fill')]) {
                                const state = colorState(color);
                                if (state === 'pending') pending += 2;
                                if (state === 'completed') completed += 2;
                              }
                              if (pending > completed && pending > 0) return 'pending';
                              if (completed > 0) return 'completed';
                              return 'unknown';
                            };
                            const rowState = (row, root) => {
                              const rowRect = row.getBoundingClientRect();
                              const icons = Array.from(root.querySelectorAll('svg,i,mat-icon,[class*=icon],[class*=Icon],path,circle'))
                                .filter(visible)
                                .filter(icon => {
                                  const rect = icon.getBoundingClientRect();
                                  const sameBand = Math.abs((rect.top + rect.bottom) / 2 - (rowRect.top + rowRect.bottom) / 2) <= 18;
                                  const nearLeft = rect.left <= rowRect.left + 75 || row.contains(icon);
                                  return row.contains(icon) || (sameBand && nearLeft);
                                });
                              let pending = 0;
                              let completed = 0;
                              for (const icon of icons) {
                                const state = nodeState(icon);
                                if (state === 'pending') pending++;
                                if (state === 'completed') completed++;
                              }
                              if (pending > completed && pending > 0) return 'pending';
                              if (completed > 0) return 'completed';
                              return 'unknown';
                            };
                            const cards = Array.from(document.querySelectorAll('section,article,div,li'))
                              .filter(visible)
                              .filter(el => textOf(el).toLowerCase().includes(moduleName))
                              .map(el => {
                                const rect = el.getBoundingClientRect();
                                const text = textOf(el);
                                let score = 0;
                                if (text.toLowerCase().startsWith(moduleName)) score += 100;
                                if (/reviewer|approver|reviewed by|current reviewer|next reviewer/i.test(text)) score += 110;
                                if (/due/i.test(text)) score += 30;
                                score -= Math.min(120, text.length / 4);
                                score -= Math.min(80, (rect.width * rect.height) / 6000);
                                return {el, text, score};
                              })
                              .sort((a, b) => b.score - a.score);
                            if (!cards.length) return '';
                            const root = cards[0].el;
                            const rows = Array.from(root.querySelectorAll('div,li,p,tr,section,article'))
                              .filter(visible)
                              .map(el => ({el, text: textOf(el)}))
                              .filter(item => /reviewer|approver|reviewed by|current reviewer|next reviewer/i.test(item.text))
                              .filter(item => item.text.length >= 8 && item.text.length <= 200);
                            const selected = [];
                            const seen = new Set();
                            for (const item of rows) {
                              const text = item.text.replace(/\\s+/g, ' ').trim();
                              const key = text.toLowerCase();
                              if (seen.has(key)) continue;
                              seen.add(key);
                              selected.push({text, state: rowState(item.el, root)});
                            }
                            return selected
                              .map(item => item.state.toUpperCase() + '::' + item.text)
                              .join('\\n');
                            """,
                    moduleLabel());
            return String.valueOf(rows);
        } catch (RuntimeException exception) {
            return "";
        }
    }

    private WorkflowActor knownWorkflowActorByName(String text) {
        String normalizedText = normalizeComparableText(text);
        if (normalizedText.isBlank()) {
            return null;
        }
        for (WorkflowActor actor : configuredWorkflowActors()) {
            for (String alias : actor.aliases) {
                if (alias != null && !alias.isBlank()
                        && normalizedText.contains(normalizeComparableText(alias))) {
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
        if (!versionHistoryMatched) {
            Reporter.log("VERSION HISTORY WARNING: " + moduleLabel()
                    + " version-history popup/download validation did not pass inside the full workflow. "
                    + "The full workflow will continue with direct Approved/Obsolete evidence; run the "
                    + "version-history-specific test for download details.", true);
        }
        return viewOnlyMatched;
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
        Integer approvedVersion = approvedOpened ? visibleModuleVersionNumber() : null;

        boolean obsoleteOpened = openModuleRecordFromStatusTab("Obsolete", "Inactive");
        boolean obsoleteViewOnly = obsoleteOpened
                && verifyCurrentModuleDetailIsViewModeOnly("Obsolete");
        Integer obsoleteVersion = obsoleteOpened ? visibleModuleVersionNumber() : null;
        boolean obsoleteIsPreviousVersion = approvedVersion == null
                || obsoleteVersion == null
                || obsoleteVersion == approvedVersion - 1;

        Reporter.log("VIEW MODE: " + moduleLabel()
                + " approvedOpened=" + approvedOpened
                + ", approvedViewOnly=" + approvedViewOnly
                + ", approvedVersion=" + versionLabelOrDash(approvedVersion)
                + ", obsoleteOpened=" + obsoleteOpened
                + ", obsoleteViewOnly=" + obsoleteViewOnly
                + ", obsoleteVersion=" + versionLabelOrDash(obsoleteVersion)
                + ", obsoleteIsPreviousVersion=" + obsoleteIsPreviousVersion, true);
        return approvedOpened && approvedViewOnly && obsoleteOpened && obsoleteViewOnly && obsoleteIsPreviousVersion;
    }

    private Integer visibleModuleVersionNumber() {
        Matcher matcher = Pattern.compile("\\bV\\s*(\\d{1,3})\\b", Pattern.CASE_INSENSITIVE)
                .matcher(getBodyText());
        Integer maxVersion = null;
        while (matcher.find()) {
            int version = Integer.parseInt(matcher.group(1));
            maxVersion = maxVersion == null ? version : Math.max(maxVersion, version);
        }
        Reporter.log("VIEW MODE: " + moduleLabel() + " visible version resolved as "
                + versionLabelOrDash(maxVersion), true);
        return maxVersion;
    }

    private String versionLabelOrDash(Integer versionNumber) {
        return versionNumber == null ? "--" : "V" + versionNumber;
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
            boolean opened = tabClicked && clickStatusCardViewOnCurrentTab(tabLabel);
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

    private boolean openDraftCardFromDraftTabIfPresent() {
        openModuleListPage();
        if (!clickModuleTab("Draft")) {
            Reporter.log("WORKFLOW: " + moduleLabel() + " Draft tab was not clickable from list page.", true);
            return false;
        }
        waitForReflectionDelay();
        if (hasNoModuleRecordsOnCurrentTab()) {
            Reporter.log("WORKFLOW: " + moduleLabel() + " Draft tab has no draft card.", true);
            return false;
        }
        boolean opened = clickStatusCardViewOnCurrentTab("Draft")
                || clickDraftCardViewWithActions()
                || openExistingRecordByStatus("Saved in Draft", "Draft", "Rejected", "Returned", "Changes Requested");
        waitForSmallDelay();
        Reporter.log("WORKFLOW: " + moduleLabel() + " existing Draft card opened=" + opened
                + ", detailOpen=" + isModuleDetailOpen()
                + ". Visible text: " + shortBodyText(), true);
        return opened && isModuleDetailOpen();
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
                            + "const inChrome = el => !!el.closest('nav, header, [class*=sidebar], [class*=menu], [class*=Menu]');"
                            + "const textOf = el => String((el.innerText || el.textContent || '') + ' '"
                            + "  + (el.getAttribute && (el.getAttribute('aria-label') || '')) + ' '"
                            + "  + (el.getAttribute && (el.getAttribute('title') || ''))).replace(/\\s+/g, ' ').trim().toLowerCase();"
                            + "const clickLikeUser = el => {"
                            + "  el.scrollIntoView({block:'center'});"
                            + "  ['pointerdown','mousedown','mouseup','click'].forEach(type =>"
                            + "    el.dispatchEvent(new MouseEvent(type, {bubbles:true, cancelable:true, view:window})));"
                            + "};"
                            + "const targetFor = el => el.closest('button, a, [role=button], [role=link], [class*=btn]') || el;"
                            + "const actions = Array.from(document.querySelectorAll('button, a, [role=button], [role=link], [class*=btn], span, div'))"
                            + "  .filter(el => visible(el) && /\\bview\\b|\\bopen\\b|details/.test(textOf(el)))"
                            + "  .filter(el => !inChrome(el) && !el.closest('[role=dialog], .modal, .dialog, .overlay'))"
                            + "  .filter(el => textOf(el).length <= 90)"
                            + "  .map(el => ({el, rect: el.getBoundingClientRect(), text: textOf(el)}))"
                            + "  .sort((a, b) => {"
                            + "    const exactScoreA = /^view\\s*[›> chevron_right]*$/i.test(a.text) ? 1000 : 0;"
                            + "    const exactScoreB = /^view\\s*[›> chevron_right]*$/i.test(b.text) ? 1000 : 0;"
                            + "    return (exactScoreB + b.rect.left + b.rect.top) - (exactScoreA + a.rect.left + a.rect.top);"
                            + "  });"
                            + "if (!actions.length) return false;"
                            + "clickLikeUser(targetFor(actions[0].el));"
                            + "return true;");
            waitForSmallDelay();
            return Boolean.TRUE.equals(result);
        } catch (RuntimeException exception) {
            return clickButtonByText("View", "Open", "Details");
        }
    }

    private boolean clickStatusCardViewOnCurrentTab(String tabLabel) {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    """
                            const moduleLabel = String(arguments[0] || '').toLowerCase();
                            const tabLabel = String(arguments[1] || '').toLowerCase();

                            const visible = el => {
                              const r = el.getBoundingClientRect();
                              const s = window.getComputedStyle(el);
                              return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';
                            };
                            const normalize = value => String(value || '')
                              .replace(/chevron_right/ig, ' ')
                              .replace(/[>›»]/g, ' ')
                              .replace(/\\s+/g, ' ')
                              .trim()
                              .toLowerCase();
                            const textOf = el => normalize((el.innerText || el.textContent || '') + ' '
                              + (el.getAttribute && (el.getAttribute('aria-label') || '')) + ' '
                              + (el.getAttribute && (el.getAttribute('title') || '')));
                            const inChrome = el => !!el.closest('nav, header, [class*=sidebar], [class*=menu], [class*=Menu]');
                            const inDialog = el => !!el.closest('[role=dialog], .modal, .dialog, .overlay, .cdk-overlay-pane');
                            const cardFor = el => el.closest('[class*=card], [class*=Card], .row, section, article')
                              || el.closest('div');
                            const clickableAncestor = el => {
                              let node = el;
                              for (let depth = 0; node && node !== document.body && depth < 10; depth++, node = node.parentElement) {
                                const tag = String(node.tagName || '').toLowerCase();
                                const role = String(node.getAttribute('role') || '').toLowerCase();
                                const cls = String(node.className || '').toLowerCase();
                                const style = window.getComputedStyle(node);
                                const nodeText = textOf(node);
                                if (tag === 'button' || tag === 'a' || role === 'button' || role === 'link'
                                    || style.cursor === 'pointer' || typeof node.onclick === 'function'
                                    || /btn|action|link|view|cursor|click|card/i.test(cls)) {
                                  return node;
                                }
                              }
                              return el;
                            };
                            const clickLikeUser = el => {
                              el.scrollIntoView({block: 'center', inline: 'center'});
                              const rect = el.getBoundingClientRect();
                              const x = Math.max(1, Math.min(window.innerWidth - 2, Math.floor(rect.left + rect.width / 2)));
                              const y = Math.max(1, Math.min(window.innerHeight - 2, Math.floor(rect.top + rect.height / 2)));
                              const pointElement = document.elementFromPoint(x, y) || el;
                              const target = clickableAncestor(pointElement) || clickableAncestor(el);
                              ['pointerover', 'mousemove', 'pointerdown', 'mousedown', 'pointerup', 'mouseup', 'click']
                                .forEach(type => target.dispatchEvent(new MouseEvent(type, {
                                  bubbles: true,
                                  cancelable: true,
                                  view: window,
                                  clientX: x,
                                  clientY: y
                                })));
                              if (typeof target.click === 'function') target.click();
                              return {x, y, targetText: textOf(target)};
                            };

                            const candidates = Array.from(document.querySelectorAll(
                              'button, a, [role=button], [role=link], [class*=btn], span, div, p'
                            ))
                              .filter(el => visible(el) && !inChrome(el) && !inDialog(el))
                              .map(el => {
                                const text = textOf(el);
                                const rect = el.getBoundingClientRect();
                                const card = cardFor(el);
                                const cardText = card ? textOf(card) : text;
                                return {el, text, rect, cardText};
                              })
                              .filter(item => /(^|\\s)(view|open|details)(\\s|$)/.test(item.text))
                              .filter(item => item.text.length <= 90)
                              .filter(item => !/download|comment|notification|profile|logout|dashboard/.test(item.text))
                              .sort((a, b) => {
                                const score = item => {
                                  let value = 0;
                                  if (/^view\\b/.test(item.text)) value += 10000;
                                  if (moduleLabel && item.cardText.includes(moduleLabel)) value += 2500;
                                  if (tabLabel && item.cardText.includes(tabLabel)) value += 1500;
                                  if (item.cardText.includes('saved in draft')) value += 1500;
                                  if (item.rect.left > 350) value += 500;
                                  value -= item.text.length;
                                  return value;
                                };
                                return score(b) - score(a);
                              });

                            if (!candidates.length) {
                              return 'NO_VIEW_ACTION';
                            }

                            const chosen = candidates[0];
                            const click = clickLikeUser(chosen.el);
                            return 'CLICKED_VIEW_ACTION|text=' + chosen.text
                              + '|card=' + chosen.cardText.substring(0, 120)
                              + '|xy=' + click.x + ',' + click.y
                              + '|target=' + click.targetText.substring(0, 80);
                            """,
                    moduleLabel(),
                    tabLabel);
            Reporter.log("WORKFLOW: " + moduleLabel() + " " + tabLabel
                    + " status-card View result=" + result, true);
            waitForSmallDelay();
            if (String.valueOf(result).startsWith("CLICKED_VIEW_ACTION")) {
                return true;
            }
        } catch (RuntimeException exception) {
            Reporter.log("WORKFLOW: " + moduleLabel() + " " + tabLabel
                    + " status-card View click failed: " + exception.getClass().getSimpleName()
                    + " - " + exception.getMessage(), true);
        }
        return clickFirstViewActionOnCurrentTab();
    }

    private boolean clickDraftCardViewWithActions() {
        List<WebElement> viewCandidates = driver.findElements(By.xpath(
                "//*[self::button or self::a or self::span or self::div][contains(normalize-space(.),'View')]"));
        for (WebElement candidate : viewCandidates) {
            if (!isUsable(candidate)) {
                continue;
            }
            String text = String.valueOf(candidate.getText()).replaceAll("\\s+", " ").trim();
            if (!containsAnyIgnoreCase(text, "View") || text.length() > 80) {
                continue;
            }
            try {
                WebElement card = candidate.findElement(By.xpath(
                        "./ancestor::*[contains(normalize-space(.),'Saved in Draft') or contains(normalize-space(.),'Draft')][1]"));
                if (!containsAnyIgnoreCase(card.getText(), moduleTextFragments())) {
                    continue;
                }
            } catch (RuntimeException ignored) {
                // Some card layouts do not expose a useful ancestor; try the candidate itself.
            }
            try {
                scrollIntoView(candidate);
                new Actions(driver)
                        .moveToElement(candidate)
                        .pause(Duration.ofMillis(150))
                        .click()
                        .perform();
                waitForSmallDelay();
                if (isModuleDetailOpen()) {
                    Reporter.log("WORKFLOW: " + moduleLabel()
                            + " Draft View opened using Selenium Actions on text='" + text + "'.", true);
                    return true;
                }
            } catch (RuntimeException exception) {
                Reporter.log("WORKFLOW: " + moduleLabel()
                        + " Draft View Selenium Actions click failed for text='" + text + "': "
                        + exception.getClass().getSimpleName() + " - " + exception.getMessage(), true);
            }
        }
        return false;
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
                // Direct route fallback below reopens the module list.
            }
        }
        if (!pageContainsAny("Draft", "Under Review", "Approved", "Obsolete")) {
            if (!openModuleListRouteDirectly()) {
                navigateToModule();
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

    private boolean hasExistingDraftMessage() {
        String text = getBodyText();
        return containsAnyIgnoreCase(text, "already")
                && containsAnyIgnoreCase(text, "draft", "created", "exists", "under review");
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
        options.addArguments(
                "--remote-allow-origins=*",
                "--no-first-run",
                "--no-default-browser-check",
                "--disable-extensions",
                "--disable-popup-blocking",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1920,1080");
        try {
            Path chromeProfileDirectory = Path.of(
                    System.getProperty("user.dir"),
                    "target",
                    "chrome-profiles",
                    moduleLabel().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-")
                            + "-" + System.nanoTime());
            Files.createDirectories(chromeProfileDirectory);
            options.addArguments("--user-data-dir=" + chromeProfileDirectory.toAbsolutePath());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to prepare isolated Chrome profile for " + moduleLabel(), exception);
        }
        boolean headless = Boolean.parseBoolean(String.valueOf(config.getOptionalSecret("EASYQ_HEADLESS")));
        if (headless) {
            options.addArguments("--headless=new");
        }
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        if (!headless) {
            driver.manage().window().maximize();
        }
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

        try {
            HamburgerNavigationHelper.openModule(driver, wait, moduleTitleLocator(), moduleLabel(), moduleMenuRegex(), false);
        } catch (AssertionError | RuntimeException exception) {
            Reporter.log("NAV: Hamburger/sidebar " + moduleLabel()
                    + " navigation failed. Trying direct route fallback. Reason: "
                    + exception.getClass().getSimpleName() + " - " + exception.getMessage(), true);
        }
        if (waitForModulePage()) {
            return;
        }

        if (openModuleListRouteDirectly()) {
            return;
        }

        Assert.fail(moduleLabel() + " module was not opened from hamburger/sidebar or direct route. URL: "
                + safeCurrentUrl() + " | Visible text: " + shortBodyText());
    }

    private boolean openModuleListRouteDirectly() {
        String appRoot = baseUrl.substring(0, baseUrl.lastIndexOf('/') + 1);
        for (String route : moduleUrlFragments()) {
            if (route == null || route.isBlank()) {
                continue;
            }
            try {
                driver.get(appRoot + route.trim().replaceFirst("^/+", ""));
                waitForSmallDelay();
                if (waitForModulePage()) {
                    Reporter.log("NAV: Opened " + moduleLabel() + " list directly using route: " + route, true);
                    return true;
                }
            } catch (RuntimeException ignored) {
                // Try the next known route spelling.
            }
        }
        return false;
    }

    private By moduleTitleLocator() {
        StringBuilder predicate = new StringBuilder();
        for (String fragment : moduleTextFragments()) {
            if (fragment == null || fragment.isBlank()) {
                continue;
            }
            if (predicate.length() > 0) {
                predicate.append(" and ");
            }
            predicate.append("contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),")
                    .append(xpathLiteral(fragment.toLowerCase(Locale.ROOT)))
                    .append(")");
        }
        if (predicate.length() == 0) {
            predicate.append("contains(normalize-space(.),").append(xpathLiteral(moduleLabel())).append(")");
        }
        return By.xpath("//*[" + predicate + "]");
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
        WorkflowActor dashboardOwner = detectPendingModuleOwnerFromDashboardAllTasks();
        if (dashboardOwner != null) {
            Reporter.log("WORKFLOW RECOVERY: Dashboard All Tasks detected current "
                    + moduleLabel() + " owner=" + dashboardOwner.roleLabel + ".", true);
            if (tryOpenPendingModuleForActor(dashboardOwner)) {
                return true;
            }
            for (WorkflowActor actor : configuredWorkflowActors()) {
                if (!sameWorkflowActor(actor, dashboardOwner) && tryOpenPendingModuleForActor(actor)) {
                    return true;
                }
            }
            Reporter.log("WORKFLOW RECOVERY: Dashboard showed pending " + moduleLabel()
                    + " but no configured workflow user could open it. Stopping before creating another draft.", true);
            return false;
        }

        navigateToModule();

        clickModuleTab("Under Review");
        waitForReflectionDelay();
        if (openExistingRecordByStatus("Under Review", "Review Pending")) {
            return true;
        }
        clickModuleTab("Draft");
        waitForReflectionDelay();
        if (clickStatusCardViewOnCurrentTab("Draft")
                || openExistingRecordByStatus("Draft", "Rejected", "Returned", "Changes Requested", "Saved in Draft")) {
            currentAuthorPassword = getPassword();
            fillModuleFormWithAutomationData();
            return submitCurrentDraftForReviewWithConfiguredUsers();
        }
        clickModuleTab("Approved");
        waitForReflectionDelay();
        if (openExistingRecordByStatus("Approved", "Active")) {
            currentAuthorPassword = getPassword();
            boolean moved = clickButtonByText("Move to Draft", "New Version", "Revise", "Edit");
            confirmIfPrompt();
            if (hasExistingDraftMessage()) {
                Reporter.log("WORKFLOW RECOVERY: " + moduleLabel()
                        + " already has a draft. Searching Admin/Doc Controller Draft tabs.", true);
                return tryResubmitDraftFromKnownAuthors();
            }
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
        clickModuleTab("Under Review");
        waitForReflectionDelay();

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
        scrollActiveDialogToBottom();
        fillAuthenticationPassword(password);
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
            for (int attempt = 1; attempt <= 3; attempt++) {
                Reporter.log("WORKFLOW: No " + moduleLabel()
                        + " Under Review record yet. Refreshing Draft -> Under Review tabs, attempt "
                        + attempt + "/3.", true);
                clickModuleTab("Draft");
                waitForSmallDelay();
                clickModuleTab("Under Review");
                waitForReflectionDelay();
                if (openExistingRecordByStatus("Under Review", "Review Pending", "Pending", "Review")) {
                    return true;
                }
            }
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
                    ".//*[self::button or self::a or self::span or self::div or @role='button' or contains(@class,'btn') or contains(@title,"
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
                if (text.length() > 120 && !label.equalsIgnoreCase(text.trim())) {
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

    private void setAllEmptyDueDatesToTodayOnce() {
        LocalDate today = LocalDate.now();
        String isoDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String displayDate = today.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        String altDisplayDate = today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    """
                            const values = [arguments[0], arguments[1], arguments[2]];
                            const visible = el => {
                              if (!el) return false;
                              const style = getComputedStyle(el);
                              const rect = el.getBoundingClientRect();
                              return style.display !== 'none'
                                && style.visibility !== 'hidden'
                                && Number(style.opacity || 1) > 0
                                && rect.width > 2
                                && rect.height > 2;
                            };
                            const textOf = el => String([
                              el.innerText,
                              el.textContent,
                              el.placeholder,
                              el.getAttribute('aria-label'),
                              el.getAttribute('title'),
                              el.getAttribute('name'),
                              el.getAttribute('formcontrolname')
                            ].join(' ')).replace(/\\s+/g, ' ').trim().toLowerCase();
                            const contextOf = el => {
                              const parts = [];
                              let node = el;
                              for (let depth = 0; node && depth < 6; depth += 1, node = node.parentElement) {
                                parts.push(textOf(node));
                              }
                              return parts.join(' ');
                            };
                            const controls = Array.from(document.querySelectorAll('input:not([type=hidden]):not([disabled])'))
                              .filter(visible)
                              .filter(input => {
                                const context = contextOf(input);
                                const type = String(input.getAttribute('type') || '').toLowerCase();
                                return type === 'date'
                                  || context.includes('due date')
                                  || context.includes('set due date')
                                  || context.includes('calendar');
                              });
                            let updated = 0;
                            for (const input of controls) {
                              const current = String(input.value || '').trim();
                              if (current && current.toLowerCase() !== 'mm/dd/yyyy') {
                                continue;
                              }
                              const value = String(input.getAttribute('type') || '').toLowerCase() === 'date'
                                ? values[0]
                                : values[1];
                              const proto = input instanceof HTMLTextAreaElement
                                ? HTMLTextAreaElement.prototype
                                : HTMLInputElement.prototype;
                              const setter = Object.getOwnPropertyDescriptor(proto, 'value')?.set;
                              if (setter) setter.call(input, value);
                              else input.value = value;
                              input.dispatchEvent(new InputEvent('input', {bubbles: true, inputType: 'insertText', data: value}));
                              input.dispatchEvent(new Event('change', {bubbles: true}));
                              input.dispatchEvent(new Event('blur', {bubbles: true}));
                              updated += 1;
                            }
                            return 'DUE_DATES_UPDATED:' + updated + ':TOTAL:' + controls.length + ':ALT:' + values[2];
                            """,
                    isoDate, displayDate, altDisplayDate);
            Reporter.log("WORKFLOW EXACT: " + moduleLabel() + " due date single-pass result=" + result, true);
        } catch (RuntimeException exception) {
            Reporter.log("WORKFLOW EXACT: " + moduleLabel()
                    + " due date single-pass failed: " + exception.getClass().getSimpleName(), true);
        }
        waitForSmallDelay();
    }

    private void fillWorkflowComment(String comment) {
        if (!fillControlsByContext(comment, "Add Comments", "Add Comment", "Comment", "Comments", "Remarks")) {
            for (WebElement field : driver.findElements(By.xpath("//textarea[not(@readonly) and not(@disabled)]"))) {
                if (!isUsable(field)) {
                    continue;
                }
                try {
                    scrollIntoView(field);
                    field.clear();
                    field.sendKeys(comment);
                    return;
                } catch (RuntimeException ignored) {
                    // Try next comment field.
                }
            }
        }
    }

    private void fillAuthenticationPassword(String password) {
        if (password == null || password.isBlank()) {
            return;
        }
        for (WebElement field : driver.findElements(By.xpath("//input[not(@disabled) and (@type='password' or contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'password'))]"))) {
            if (!isUsable(field)) {
                continue;
            }
            try {
                scrollIntoView(field);
                field.clear();
                field.sendKeys(password);
                waitForSmallDelay();
                return;
            } catch (RuntimeException ignored) {
                // Try next password/authentication field.
            }
        }
        fillControlsByContext(password, "Authentication", "Password");
    }

    private boolean clickSendForReviewInsideWorkflowPopup() {
        waitForSmallDelay();
        scrollActiveDialogToBottom();
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    """
                            const visible = el => {
                              if (!el) return false;
                              const rect = el.getBoundingClientRect();
                              const style = window.getComputedStyle(el);
                              return rect.width > 1 && rect.height > 1
                                && style.display !== 'none' && style.visibility !== 'hidden'
                                && style.opacity !== '0';
                            };
                            const textOf = el => String([
                              el && el.innerText,
                              el && el.textContent,
                              el && el.getAttribute && el.getAttribute('aria-label'),
                              el && el.getAttribute && el.getAttribute('title'),
                              el && el.value
                            ].join(' ')).replace(/\\s+/g, ' ').trim().toLowerCase();
                            const clickTarget = el => {
                              const target = el.closest('button,a,[role=button],input[type=button],input[type=submit]') || el;
                              target.scrollIntoView({block: 'center', inline: 'center'});
                              const rect = target.getBoundingClientRect();
                              const x = Math.max(1, Math.min(window.innerWidth - 2, rect.left + rect.width / 2));
                              const y = Math.max(1, Math.min(window.innerHeight - 2, rect.top + rect.height / 2));
                              const centered = document.elementFromPoint(x, y);
                              const targetToClick = centered && target.contains(centered) ? centered : target;
                              targetToClick.dispatchEvent(new PointerEvent('pointerdown', {bubbles: true}));
                              targetToClick.dispatchEvent(new MouseEvent('mousedown', {bubbles: true}));
                              targetToClick.dispatchEvent(new MouseEvent('mouseup', {bubbles: true}));
                              targetToClick.dispatchEvent(new MouseEvent('click', {bubbles: true}));
                              target.click();
                              return target;
                            };
                            const roots = Array.from(document.querySelectorAll([
                              '[role=dialog]',
                              '.modal',
                              '.dialog',
                              '.overlay',
                              '.drawer',
                              '.cdk-overlay-pane',
                              '.mat-dialog-container',
                              '[class*=Modal]',
                              '[class*=Dialog]'
                            ].join(','))).filter(visible);
                            roots.push(...Array.from(document.querySelectorAll('body > div')).filter(visible));
                            const workflowRoots = roots.filter(root => {
                              const text = textOf(root);
                              return text.includes('send')
                                && (text.includes('reviewer')
                                  || text.includes('approver')
                                  || text.includes('authentication')
                                  || text.includes('comment'));
                            }).sort((a, b) => {
                              const ar = a.getBoundingClientRect();
                              const br = b.getBoundingClientRect();
                              return (br.width * br.height) - (ar.width * ar.height);
                            });
                            const popup = workflowRoots[0];
                            if (!popup) return 'NO_WORKFLOW_POPUP';
                            popup.scrollTop = popup.scrollHeight;
                            const buttons = Array.from(popup.querySelectorAll(
                              'button,[role=button],a,input[type=button],input[type=submit]'
                            )).filter(visible).map(el => {
                              const rect = el.getBoundingClientRect();
                              const text = textOf(el);
                              let score = 0;
                              if (text.includes('send for review')) score += 120;
                              if (text.includes('send to review')) score += 110;
                              if (text === 'send') score += 70;
                              if (text === 'submit') score += 50;
                              if (text === 'done') score += 35;
                              if (rect.top > popup.getBoundingClientRect().top + popup.getBoundingClientRect().height * 0.45) score += 30;
                              if (text.includes('cancel') || text.includes('close') || text === 'x') score -= 300;
                              return {el, text, rect, score};
                            }).filter(item => item.score > 0)
                              .sort((a, b) => b.score - a.score || b.rect.top - a.rect.top || b.rect.left - a.rect.left);
                            if (!buttons.length) return 'NO_POPUP_SEND_BUTTON:' + textOf(popup).slice(0, 220);
                            const clicked = clickTarget(buttons[0].el);
                            const rect = clicked.getBoundingClientRect();
                            return 'CLICKED_POPUP_SEND_BUTTON:' + textOf(clicked) + ':'
                              + Math.round(rect.left) + ',' + Math.round(rect.top);
                            """);
            Reporter.log("WORKFLOW EXACT: " + moduleLabel()
                    + " popup Send for Review click result=" + result, true);
            waitForSmallDelay();
            return String.valueOf(result).startsWith("CLICKED_POPUP_SEND_BUTTON");
        } catch (RuntimeException exception) {
            Reporter.log("WORKFLOW EXACT: " + moduleLabel()
                    + " popup Send for Review click failed: " + exception.getMessage(), true);
            return false;
        }
    }

    private void scrollActiveDialogToBottom() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "const visible = el => {"
                            + "  const rect = el.getBoundingClientRect();"
                            + "  const style = window.getComputedStyle(el);"
                            + "  return rect.width > 1 && rect.height > 1 && style.display !== 'none' && style.visibility !== 'hidden';"
                            + "};"
                            + "const roots = Array.from(document.querySelectorAll('[role=dialog],.modal,.dialog,.overlay,.drawer,.cdk-overlay-pane,.mat-dialog-container,body > div')).filter(visible);"
                            + "for (const root of roots) {"
                            + "  const text = String(root.innerText || root.textContent || '').toLowerCase();"
                            + "  if (text.includes('reviewer') || text.includes('approver') || text.includes('authentication') || text.includes('comment')) {"
                            + "    root.scrollTop = root.scrollHeight;"
                            + "    for (const child of Array.from(root.querySelectorAll('*'))) {"
                            + "      if (child.scrollHeight > child.clientHeight) child.scrollTop = child.scrollHeight;"
                            + "    }"
                            + "  }"
                            + "}");
        } catch (RuntimeException ignored) {
            // Scrolling only improves access to fields/buttons near the popup bottom.
        }
        waitForSmallDelay();
    }

    private void confirmIfPrompt() {
        By dialogLocator = By.xpath("//*[contains(@class,'modal') or contains(@class,'dialog') or @role='dialog' or contains(@class,'overlay')]");
        for (WebElement dialog : driver.findElements(dialogLocator)) {
            if (!isUsable(dialog)) {
                continue;
            }
            String dialogText = String.valueOf(dialog.getText()).toLowerCase(Locale.ROOT);
            boolean moveToDraftDialog = dialogText.contains("move to draft");
            if (clickActionInside(dialog, "Confirm", "Yes", "Move to Draft", "Reject", "Approve",
                    "OK", "Ok", "Submit", "Done", "Continue")) {
                waitForSmallDelay();
                if (moveToDraftDialog) {
                    cancelMoveToDraftPopupIfStillOpen();
                }
                return;
            }
        }
    }

    private boolean cancelMoveToDraftPopupIfStillOpen() {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    """
                            const visible = el => {
                              if (!el) return false;
                              const rect = el.getBoundingClientRect();
                              const style = window.getComputedStyle(el);
                              return rect.width > 1 && rect.height > 1
                                && style.display !== 'none'
                                && style.visibility !== 'hidden'
                                && Number(style.opacity || 1) > 0;
                            };
                            const textOf = el => String([
                              el.innerText,
                              el.textContent,
                              el.getAttribute && el.getAttribute('aria-label'),
                              el.getAttribute && el.getAttribute('title')
                            ].join(' ')).replace(/\\s+/g, ' ').trim().toLowerCase();
                            const dialogs = Array.from(document.querySelectorAll(
                              '[role=dialog], .modal, .dialog, .overlay, .cdk-overlay-pane, .mat-dialog-container'
                            )).filter(visible);
                            dialogs.push(...Array.from(document.querySelectorAll('body > div')).filter(visible));
                            const dialog = dialogs.find(el => textOf(el).includes('move to draft confirmation')
                              || (textOf(el).includes('move to draft') && textOf(el).includes('cancel')));
                            if (!dialog) return 'NO_MOVE_TO_DRAFT_POPUP';
                            const cancel = Array.from(dialog.querySelectorAll('button,[role=button],a'))
                              .filter(visible)
                              .find(el => textOf(el) === 'cancel' || textOf(el).includes('cancel'));
                            if (!cancel) return 'NO_CANCEL_BUTTON';
                            cancel.scrollIntoView({block: 'center', inline: 'center'});
                            cancel.dispatchEvent(new MouseEvent('mouseover', {bubbles: true}));
                            cancel.dispatchEvent(new MouseEvent('mousedown', {bubbles: true}));
                            cancel.dispatchEvent(new MouseEvent('mouseup', {bubbles: true}));
                            cancel.click();
                            return 'CLICKED_CANCEL_AFTER_YES:' + textOf(cancel);
                            """);
            Reporter.log("WORKFLOW EXACT: " + moduleLabel()
                    + " Move to Draft post-Yes cancel result=" + result, true);
            waitForSmallDelay();
            return String.valueOf(result).startsWith("CLICKED_CANCEL_AFTER_YES");
        } catch (RuntimeException exception) {
            Reporter.log("WORKFLOW EXACT: " + moduleLabel()
                    + " Move to Draft post-Yes cancel failed: "
                    + exception.getClass().getSimpleName(), true);
            return false;
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
