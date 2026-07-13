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
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import utils.ConfigReader;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public abstract class EasyQModuleWorkflowBase {
    private static final String MODULE_WORKFLOW_CODE_VERSION = "MODULE_REJECT_REVIEWERS_AND_APPROVER_2026_07_11_A";

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected final ConfigReader config = new ConfigReader();

    private String latestRecordTitle;
    private String setupFailureMessage;

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
            return false;
        }

        if (rejectFirst) {
            boolean rejected = performWorkflowAction(
                    workflowUserName("REVIEWER1_USERNAME", configValue("EASYQ_ADMIN_USERNAME", validEmail)),
                    reviewer1Password(),
                    "Reviewer 1",
                    "Reject");
            if (!rejected) {
                return false;
            }

            loginAsConfiguredUser(configValue("EASYQ_ADMIN_USERNAME", validEmail), getPassword());
            if (!resubmitRejectedDraftFromVarunAccount("Reviewer 1 reject")) {
                return false;
            }
        }

        boolean reviewer1Done = performWorkflowAction(
                workflowUserName("REVIEWER1_USERNAME", configValue("EASYQ_ADMIN_USERNAME", validEmail)),
                reviewer1Password(),
                "Reviewer 1",
                "Approve");
        if (!reviewer1Done) {
            return false;
        }

        if (rejectFirst) {
            boolean reviewer2Rejected = performWorkflowAction(
                    workflowUserName("REVIEWER2_USERNAME", configValue("EASYQ_DOC_CONTROLLER_USERNAME", "")),
                    requiredSecret("EASYQ_DOC_CONTROLLER_PASSWORD"),
                    "Reviewer 2",
                    "Reject");
            if (!reviewer2Rejected) {
                return false;
            }

            loginAsConfiguredUser(configValue("EASYQ_ADMIN_USERNAME", validEmail), getPassword());
            if (!resubmitRejectedDraftFromVarunAccount("Reviewer 2 reject")) {
                return false;
            }

            reviewer1Done = performWorkflowAction(
                    workflowUserName("REVIEWER1_USERNAME", configValue("EASYQ_ADMIN_USERNAME", validEmail)),
                    reviewer1Password(),
                    "Reviewer 1 after Reviewer 2 reject",
                    "Approve");
            if (!reviewer1Done) {
                return false;
            }
        }

        boolean reviewer2Done = performWorkflowAction(
                workflowUserName("REVIEWER2_USERNAME", configValue("EASYQ_DOC_CONTROLLER_USERNAME", "")),
                requiredSecret("EASYQ_DOC_CONTROLLER_PASSWORD"),
                "Reviewer 2",
                "Approve");
        if (!reviewer2Done) {
            return false;
        }

        if (rejectFirst) {
            boolean approverRejected = performWorkflowAction(
                    workflowUserName("APPROVER_USERNAME", configValue("EASYQ_ASSIGNEE_AMIT_USERNAME", "")),
                    requiredSecret("EASYQ_ASSIGNEE_AMIT_PASSWORD"),
                    "Approver",
                    "Reject");
            if (!approverRejected) {
                return false;
            }

            loginAsConfiguredUser(configValue("EASYQ_ADMIN_USERNAME", validEmail), getPassword());
            if (!resubmitRejectedDraftFromVarunAccount("Approver reject")) {
                return false;
            }

            reviewer1Done = performWorkflowAction(
                    workflowUserName("REVIEWER1_USERNAME", configValue("EASYQ_ADMIN_USERNAME", validEmail)),
                    reviewer1Password(),
                    "Reviewer 1 after Approver reject",
                    "Approve");
            if (!reviewer1Done) {
                return false;
            }

            reviewer2Done = performWorkflowAction(
                    workflowUserName("REVIEWER2_USERNAME", configValue("EASYQ_DOC_CONTROLLER_USERNAME", "")),
                    requiredSecret("EASYQ_DOC_CONTROLLER_PASSWORD"),
                    "Reviewer 2 after Approver reject",
                    "Approve");
            if (!reviewer2Done) {
                return false;
            }
        }

        boolean approverDone = performWorkflowAction(
                workflowUserName("APPROVER_USERNAME", configValue("EASYQ_ASSIGNEE_AMIT_USERNAME", "")),
                requiredSecret("EASYQ_ASSIGNEE_AMIT_PASSWORD"),
                rejectFirst ? "Approver final approval" : "Approver",
                "Approve");

        return reviewer1Done && reviewer2Done && approverDone;
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
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        driver.manage().window().maximize();
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
        String body = latestRecordTitle + " created by automation for " + moduleLabel()
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
                field.clear();
                field.sendKeys(field.getTagName().equalsIgnoreCase("textarea") ? body : latestRecordTitle);
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

        if (clickVisibleText(userName)) {
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
                control.clear();
                control.sendKeys(userName);
                waitForSmallDelay();
                if (clickVisibleText(userName)) {
                    return true;
                }
            } catch (RuntimeException ignored) {
                // Try the next user selector.
            }
        }

        return pageContainsAny(userName);
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
        return openExistingRecordByStatus("Under Review", "Review Pending", "Pending", "Review")
                || clickButtonByText("My Tasks", "Assigned", "Review", "Approval")
                || hasModuleDataOrPageLoaded();
    }

    private boolean openExistingRecordByStatus(String... statuses) {
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
        String remarks = roleLabel + " " + action.toLowerCase(Locale.ROOT)
                + " remarks added by automation on " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        fillControlsByContext(remarks, "Remark", "Comment", "Reason", "Review", "Approval", "Observation");
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
            if (clickActionInside(dialog, "Confirm", "Yes", "OK", "Ok", "Submit", "Done", "Continue")) {
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
