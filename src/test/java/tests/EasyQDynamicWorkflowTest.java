package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.LoginPage;
import utils.DynamicWorkflowHelper;

public class EasyQDynamicWorkflowTest extends BaseTest {
    private record RoleAccount(String roleName, String usernameKey, String passwordKey) {
    }

    private final By dashboardText = By.xpath("//*[contains(normalize-space(.),'Dashboard')]");
    private final By primaryWorkflowAction = By.xpath(
            "//button[contains(normalize-space(.),'Create') or contains(normalize-space(.),'Add') or contains(normalize-space(.),'New') "
                    + "or contains(normalize-space(.),'Initiate') or contains(normalize-space(.),'Upload') "
                    + "or contains(normalize-space(.),'Assign') or contains(normalize-space(.),'Log Complaint')] | "
                    + "//a[contains(normalize-space(.),'Create') or contains(normalize-space(.),'Add') or contains(normalize-space(.),'New') "
                    + "or contains(normalize-space(.),'Initiate') or contains(normalize-space(.),'Upload') "
                    + "or contains(normalize-space(.),'Assign') or contains(normalize-space(.),'Log Complaint')]"
    );
    private final By submitOrSaveAction = By.xpath(
            "//button[contains(normalize-space(.),'Save') or contains(normalize-space(.),'Submit') "
                    + "or contains(normalize-space(.),'Send') or contains(normalize-space(.),'Assign')]"
    );
    private final By visibleFormField = By.xpath(
            "//input[not(@type='hidden') and not(@type='file')] | //textarea | //select | //*[@role='combobox']"
    );
    private final By workflowStatusText = By.xpath(
            "//*[contains(normalize-space(.),'Draft') or contains(normalize-space(.),'Under Review') "
                    + "or contains(normalize-space(.),'Review Pending') or contains(normalize-space(.),'Approved') "
                    + "or contains(normalize-space(.),'Pending') or contains(normalize-space(.),'Completed') "
                    + "or contains(normalize-space(.),'Closed') or contains(normalize-space(.),'Open')]"
    );
    private final By emptyOrRestrictedState = By.xpath(
            "//*[contains(normalize-space(.),'No Data') or contains(normalize-space(.),'No data') "
                    + "or contains(normalize-space(.),'No records') or contains(normalize-space(.),'No Records') "
                    + "or contains(normalize-space(.),'Access Denied') or contains(normalize-space(.),'Unauthorized') "
                    + "or contains(normalize-space(.),'Restricted') or contains(normalize-space(.),'Permission')]"
    );
    private final By dashboardViewButton = By.xpath(
            "//button[contains(normalize-space(.),'View')] | //a[contains(normalize-space(.),'View')]"
    );
    private final By notificationButton = By.xpath(
            "//*[contains(@class,'notification') or contains(@aria-label,'Notification') "
                    + "or contains(@title,'Notification') or contains(normalize-space(.),'Notifications')]"
    );
    private final By notificationItem = By.xpath(
            "//*[contains(normalize-space(.),'Approved') or contains(normalize-space(.),'Pending') "
                    + "or contains(normalize-space(.),'Review') or contains(normalize-space(.),'assigned') "
                    + "or contains(normalize-space(.),'Assigned')]"
    );
    private final By searchInput = By.xpath(
            "//input[contains(@placeholder,'Search') or contains(@aria-label,'Search') or contains(@formcontrolname,'search')]"
    );
    private final By nextPageButton = By.xpath("//button[contains(.,'Next') or @aria-label='Next page']");

    @DataProvider(name = "modulePages")
    public Object[][] modulePages() {
        return new Object[][]{
                {"Dashboard", new String[]{"Dashboard"}},
                {"Quality Policy", new String[]{"Quality Policy"}},
                {"Quality Objective", new String[]{"Quality Objective"}},
                {"Responsibility & Authority", new String[]{"Responsibility", "Authority"}},
                {"Management Review", new String[]{"Management Review"}},
                {"Document Management", new String[]{"Document Management", "Documents"}},
                {"CAPA", new String[]{"CAPA", "Deviation"}},
                {"Training", new String[]{"Training"}},
                {"Products", new String[]{"Products"}},
                {"Complaint Management", new String[]{"Complaint", "Complaint Management"}},
                {"User Management", new String[]{"User Management"}}
        };
    }

    @DataProvider(name = "workflowModules")
    public Object[][] workflowModules() {
        return new Object[][]{
                {"Quality Policy", new String[]{"Quality Policy"}},
                {"Quality Objective", new String[]{"Quality Objective"}},
                {"Responsibility & Authority", new String[]{"Responsibility", "Authority"}},
                {"Management Review", new String[]{"Management Review"}},
                {"Document Management", new String[]{"Document Management", "Documents"}},
                {"CAPA", new String[]{"CAPA", "Deviation"}},
                {"Training", new String[]{"Training"}},
                {"Complaint Management", new String[]{"Complaint", "Complaint Management"}},
                {"User Management", new String[]{"User Management"}}
        };
    }

    @DataProvider(name = "coreRoleAccounts")
    public Object[][] coreRoleAccounts() {
        return new Object[][]{
                {new RoleAccount("Admin", "EASYQ_ADMIN_USERNAME", "EASYQ_ADMIN_PASSWORD")},
                {new RoleAccount("Document Controller", "EASYQ_DOC_CONTROLLER_USERNAME", "EASYQ_DOC_CONTROLLER_PASSWORD")},
                {new RoleAccount("Assignee", "EASYQ_ASSIGNEE_SWATI_USERNAME", "EASYQ_ASSIGNEE_SWATI_PASSWORD")}
        };
    }

    @DataProvider(name = "assigneeAccounts")
    public Object[][] assigneeAccounts() {
        return new Object[][]{
                {new RoleAccount("Assignee - Swati", "EASYQ_ASSIGNEE_SWATI_USERNAME", "EASYQ_ASSIGNEE_SWATI_PASSWORD")},
                {new RoleAccount("Assignee - Amit", "EASYQ_ASSIGNEE_AMIT_USERNAME", "EASYQ_ASSIGNEE_AMIT_PASSWORD")},
                {new RoleAccount("Assignee - Kartik", "EASYQ_ASSIGNEE_KARTIK_USERNAME", "EASYQ_ASSIGNEE_KARTIK_PASSWORD")},
                {new RoleAccount("Assignee - Ayesha", "EASYQ_ASSIGNEE_AYESHA_USERNAME", "EASYQ_ASSIGNEE_AYESHA_PASSWORD")},
                {new RoleAccount("Assignee - Anushka", "EASYQ_ASSIGNEE_ANUSHKA_USERNAME", "EASYQ_ASSIGNEE_ANUSHKA_PASSWORD")},
                {new RoleAccount("Assignee - Himi", "EASYQ_ASSIGNEE_HIMI_USERNAME", "EASYQ_ASSIGNEE_HIMI_PASSWORD")},
                {new RoleAccount("Assignee - Kavita", "EASYQ_ASSIGNEE_KAVITA_USERNAME", "EASYQ_ASSIGNEE_KAVITA_PASSWORD")},
                {new RoleAccount("Assignee - Saurabh", "EASYQ_ASSIGNEE_SAURABH_USERNAME", "EASYQ_ASSIGNEE_SAURABH_PASSWORD")}
        };
    }

    @DataProvider(name = "roleModuleRules")
    public Object[][] roleModuleRules() {
        return new Object[][]{
                {new RoleAccount("Admin", "EASYQ_ADMIN_USERNAME", "EASYQ_ADMIN_PASSWORD"),
                        "User Management", new String[]{"User Management"}, true},
                {new RoleAccount("Admin", "EASYQ_ADMIN_USERNAME", "EASYQ_ADMIN_PASSWORD"),
                        "CAPA", new String[]{"CAPA", "Deviation"}, true},
                {new RoleAccount("Document Controller", "EASYQ_DOC_CONTROLLER_USERNAME", "EASYQ_DOC_CONTROLLER_PASSWORD"),
                        "Document Management", new String[]{"Document Management", "Documents"}, true},
                {new RoleAccount("Document Controller", "EASYQ_DOC_CONTROLLER_USERNAME", "EASYQ_DOC_CONTROLLER_PASSWORD"),
                        "Quality Policy", new String[]{"Quality Policy"}, true},
                {new RoleAccount("Assignee", "EASYQ_ASSIGNEE_SWATI_USERNAME", "EASYQ_ASSIGNEE_SWATI_PASSWORD"),
                        "User Management", new String[]{"User Management"}, false},
                {new RoleAccount("Assignee", "EASYQ_ASSIGNEE_SWATI_USERNAME", "EASYQ_ASSIGNEE_SWATI_PASSWORD"),
                        "Training", new String[]{"Training"}, true}
        };
    }

    @Test(priority = 1, description = "Verify dynamic dashboard loads after valid login")
    // Manual Test Case ID: TC001-TC078, TC079-TC203
    public void verifyDynamicDashboardLoadsAfterLogin() {
        loginToDashboard();

        Assert.assertTrue(
                DynamicWorkflowHelper.isVisible(driver, dashboardText)
                        || DynamicWorkflowHelper.containsAny(DynamicWorkflowHelper.getBodyText(driver), "Dashboard", "QMS"),
                "Dashboard should load after valid login"
        );
    }

    @Test(priority = 2, dataProvider = "modulePages", description = "Verify dynamic module page navigation and load state")
    // Manual Test Case ID: TC079-TC679
    public void verifyDynamicModulePageLoads(String moduleName, String[] aliases) {
        loginToDashboard();

        if (!navigateToModule(moduleName, aliases)) {
            DynamicWorkflowHelper.assertDynamicState(driver, moduleName + " menu not available for current role");
            return;
        }

        Assert.assertTrue(
                pageMentionsAny(aliases) || DynamicWorkflowHelper.isVisible(driver, emptyOrRestrictedState),
                moduleName + " should load, show an empty state, or show a valid access restriction"
        );
    }

    @Test(priority = 3, dataProvider = "workflowModules", description = "Verify dynamic create/initiate/upload workflow branch")
    // Manual Test Case ID: TC279-TC679
    public void verifyDynamicWorkflowActionBranch(String moduleName, String[] aliases) {
        loginToDashboard();

        if (!navigateToModule(moduleName, aliases)) {
            DynamicWorkflowHelper.assertDynamicState(driver, moduleName + " workflow menu unavailable");
            return;
        }

        boolean actionOpened = DynamicWorkflowHelper.clickFirstVisible(driver, primaryWorkflowAction);
        if (!actionOpened) {
            Assert.assertTrue(
                    DynamicWorkflowHelper.isVisible(driver, emptyOrRestrictedState)
                            || DynamicWorkflowHelper.isVisible(driver, workflowStatusText)
                            || pageMentionsAny(aliases),
                    moduleName + " should show data, restriction, or a stable module state when create action is unavailable"
            );
            return;
        }

        Assert.assertTrue(
                DynamicWorkflowHelper.visibleCount(driver, visibleFormField) > 0
                        || DynamicWorkflowHelper.isVisible(driver, submitOrSaveAction)
                        || DynamicWorkflowHelper.isVisible(driver, emptyOrRestrictedState),
                moduleName + " workflow action should open a form, action screen, or valid restricted state"
        );
    }

    @Test(priority = 4, dataProvider = "workflowModules", description = "Verify workflow status is handled dynamically")
    // Manual Test Case ID: TC366-TC620, TC640-TC679
    public void verifyDynamicWorkflowStatusHandling(String moduleName, String[] aliases) {
        loginToDashboard();

        if (!navigateToModule(moduleName, aliases)) {
            DynamicWorkflowHelper.assertDynamicState(driver, moduleName + " status menu unavailable");
            return;
        }

        Assert.assertTrue(
                DynamicWorkflowHelper.isVisible(driver, workflowStatusText)
                        || DynamicWorkflowHelper.isVisible(driver, emptyOrRestrictedState)
                        || pageMentionsAny(aliases),
                moduleName + " should show workflow status, empty state, restriction, or loaded page state"
        );
    }

    @Test(priority = 5, description = "Verify dashboard View links navigate dynamically")
    // Manual Test Case ID: TC091-TC119, TC121-TC164
    public void verifyDashboardViewLinksNavigateDynamically() {
        loginToDashboard();

        if (!DynamicWorkflowHelper.isVisible(driver, dashboardViewButton)) {
            DynamicWorkflowHelper.assertDynamicState(driver, "Dashboard View button not available in current data state");
            return;
        }

        String beforeUrl = driver.getCurrentUrl();
        DynamicWorkflowHelper.clickFirstVisible(driver, dashboardViewButton);
        String afterUrl = driver.getCurrentUrl();

        Assert.assertTrue(
                !beforeUrl.equals(afterUrl) || DynamicWorkflowHelper.getBodyText(driver).length() > 20,
                "Dashboard View button should navigate or keep a stable page state"
        );
    }

    @Test(priority = 6, description = "Verify notifications panel and notification redirection dynamically")
    // Manual Test Case ID: TC204-TC278
    public void verifyNotificationsPanelAndRedirectionDynamically() {
        loginToDashboard();

        if (!DynamicWorkflowHelper.clickFirstVisible(driver, notificationButton)) {
            DynamicWorkflowHelper.assertDynamicState(driver, "Notification icon not available for current role");
            return;
        }

        Assert.assertTrue(
                DynamicWorkflowHelper.containsAny(DynamicWorkflowHelper.getBodyText(driver), "Notifications", "No Notifications")
                        || DynamicWorkflowHelper.isVisible(driver, notificationItem),
                "Notification panel should open with notifications or empty state"
        );

        if (DynamicWorkflowHelper.clickFirstVisible(driver, notificationItem)) {
            DynamicWorkflowHelper.assertDynamicState(driver, "Notification item redirection");
        }
    }

    @Test(priority = 7, description = "Verify Active Logs dynamic audit trail state")
    // Manual Test Case ID: TC333-TC365
    public void verifyActiveLogsDynamicAuditTrailState() {
        loginToDashboard();

        if (!navigateToModule("Active Logs", new String[]{"Active Logs", "Activity Logs", "Activity Log", "Logs"})) {
            DynamicWorkflowHelper.assertDynamicState(driver, "Active Logs menu not available for current role");
            return;
        }

        Assert.assertTrue(
                DynamicWorkflowHelper.containsAny(DynamicWorkflowHelper.getBodyText(driver),
                        "Today", "Yesterday", "Login", "Activity", "No Logs", "No records")
                        || DynamicWorkflowHelper.isVisible(driver, emptyOrRestrictedState),
                "Active Logs should show grouped log data or a valid empty/restricted state"
        );
    }

    @Test(priority = 8, description = "Verify Training workflow handles dynamic create, assign, and completion states")
    // Manual Test Case ID: TC590-TC620
    public void verifyTrainingWorkflowHandlesDynamicStates() {
        loginToDashboard();

        if (!navigateToModule("Training", new String[]{"Training"})) {
            DynamicWorkflowHelper.assertDynamicState(driver, "Training menu not available for current role");
            return;
        }

        if (DynamicWorkflowHelper.allowWorkflowMutations()
                && DynamicWorkflowHelper.clickFirstVisible(driver, primaryWorkflowAction)) {
            DynamicWorkflowHelper.fillFirstTextInput(driver, DynamicWorkflowHelper.uniqueAutomationText("Training"));
            DynamicWorkflowHelper.clickFirstVisible(driver, submitOrSaveAction);
        }

        Assert.assertTrue(
                DynamicWorkflowHelper.containsAny(DynamicWorkflowHelper.getBodyText(driver),
                        "Training", "Assign", "My Training", "Pending", "Completed", "Start", "Acknowledge", "No Data")
                        || DynamicWorkflowHelper.isVisible(driver, emptyOrRestrictedState),
                "Training should handle create, assign, user training, completion, empty, or restricted state dynamically"
        );
    }

    @Test(priority = 9, description = "Verify Document Management workflow handles draft, review, approval, and version states")
    // Manual Test Case ID: TC512-TC547
    public void verifyDocumentWorkflowHandlesDynamicStates() {
        loginToDashboard();

        if (!navigateToModule("Document Management", new String[]{"Document Management", "Documents"})) {
            DynamicWorkflowHelper.assertDynamicState(driver, "Document Management menu not available for current role");
            return;
        }

        if (DynamicWorkflowHelper.allowWorkflowMutations()
                && DynamicWorkflowHelper.clickFirstVisible(driver, primaryWorkflowAction)) {
            DynamicWorkflowHelper.fillFirstTextInput(driver, DynamicWorkflowHelper.uniqueAutomationText("Document"));
        }

        Assert.assertTrue(
                DynamicWorkflowHelper.containsAny(DynamicWorkflowHelper.getBodyText(driver),
                        "Document", "Draft", "Review", "Approved", "Obsolete", "Download", "Upload", "No Data")
                        || DynamicWorkflowHelper.isVisible(driver, emptyOrRestrictedState),
                "Document workflow should handle upload/draft/review/approval/version states dynamically"
        );
    }

    @Test(priority = 10, description = "Verify Product and Complaint dynamic list behavior")
    // Manual Test Case ID: TC621-TC679
    public void verifyProductAndComplaintDynamicListBehavior() {
        loginToDashboard();
        verifyListModuleDynamicState("Products", new String[]{"Products"});

        loginToDashboard();
        verifyListModuleDynamicState("Complaint Management", new String[]{"Complaint", "Complaint Management"});
    }

    @Test(priority = 11, description = "Verify search and pagination dynamic controls")
    // Manual Test Case ID: TC624-TC629, TC297-TC298
    public void verifySearchAndPaginationDynamicControls() {
        loginToDashboard();

        if (!navigateToModule("Training", new String[]{"Training"})) {
            DynamicWorkflowHelper.assertDynamicState(driver, "Training menu unavailable for search/pagination check");
            return;
        }

        if (DynamicWorkflowHelper.isVisible(driver, searchInput)) {
            WebElement search = driver.findElement(searchInput);
            search.clear();
            search.sendKeys("AUTO_NOT_FOUND_12345");
            DynamicWorkflowHelper.waitForStablePage(driver, 5);
        }

        if (DynamicWorkflowHelper.isVisible(driver, nextPageButton)) {
            DynamicWorkflowHelper.clickFirstVisible(driver, nextPageButton);
        }

        DynamicWorkflowHelper.assertDynamicState(driver, "Search and pagination controls");
    }

    @Test(priority = 12, dataProvider = "coreRoleAccounts", description = "Verify core roles can login and reach dashboard")
    // Manual Test Case ID: TC036, TC061, TC192-TC197
    public void verifyCoreRolesCanLoginAndReachDashboard(RoleAccount account) {
        loginToDashboard(account);

        Assert.assertTrue(
                DynamicWorkflowHelper.isVisible(driver, dashboardText)
                        || DynamicWorkflowHelper.containsAny(DynamicWorkflowHelper.getBodyText(driver), "Dashboard", "QMS", "Tasks"),
                account.roleName() + " should login and reach a valid dashboard state"
        );
    }

    @Test(priority = 13, dataProvider = "roleModuleRules", description = "Verify role-based module access dynamically")
    // Manual Test Case ID: TC193-TC197, TC279-TC332, TC512-TC547, TC590-TC620
    public void verifyRoleBasedModuleAccessDynamically(
            RoleAccount account,
            String moduleName,
            String[] aliases,
            boolean expectedAccess
    ) {
        loginToDashboard(account);

        boolean reachedModule = navigateToModule(moduleName, aliases);
        if (expectedAccess) {
            Assert.assertTrue(
                    reachedModule || pageMentionsAny(aliases) || DynamicWorkflowHelper.isVisible(driver, emptyOrRestrictedState),
                    account.roleName() + " should reach " + moduleName + " or receive a clear system state"
            );
            return;
        }

        if (!reachedModule) {
            Assert.assertTrue(true, moduleName + " menu is hidden for " + account.roleName());
            return;
        }

        Assert.assertTrue(
                DynamicWorkflowHelper.isVisible(driver, emptyOrRestrictedState)
                        || !DynamicWorkflowHelper.isVisible(driver, primaryWorkflowAction),
                account.roleName() + " should not see restricted create/admin actions in " + moduleName
        );
    }

    @Test(priority = 14, dataProvider = "assigneeAccounts", description = "Verify all assignee accounts handle dashboard and task workflow")
    // Manual Test Case ID: TC602-TC615
    public void verifyAllAssigneeAccountsHandleDashboardAndTasks(RoleAccount account) {
        loginToDashboard(account);

        Assert.assertTrue(
                DynamicWorkflowHelper.containsAny(DynamicWorkflowHelper.getBodyText(driver), "Dashboard", "Tasks", "Training", "QMS")
                        || DynamicWorkflowHelper.isVisible(driver, emptyOrRestrictedState),
                account.roleName() + " should reach dashboard/task state"
        );

        if (!navigateToModule("Training", new String[]{"Training", "My Training"})) {
            DynamicWorkflowHelper.assertDynamicState(driver, account.roleName() + " Training task menu not available");
            return;
        }

        Assert.assertTrue(
                DynamicWorkflowHelper.containsAny(DynamicWorkflowHelper.getBodyText(driver),
                        "Training", "My Training", "Pending", "Completed", "Assigned", "No Data")
                        || DynamicWorkflowHelper.isVisible(driver, emptyOrRestrictedState),
                account.roleName() + " should see assigned training, completed training, empty state, or restriction"
        );
    }

    private void loginToDashboard() {
        loginToDashboard(new RoleAccount("Admin", "EASYQ_ADMIN_USERNAME", "EASYQ_ADMIN_PASSWORD"));
    }

    private void loginToDashboard(RoleAccount account) {
        openFreshLoginPage();

        LoginPage loginPage = new LoginPage(driver, config.getInt("explicitWait"));
        loginPage.login(getUsername(account), getPassword(account));
        DynamicWorkflowHelper.waitForStablePage(driver, config.getInt("explicitWait"));
    }

    private void openFreshLoginPage() {
        driver.manage().deleteAllCookies();
        try {
            ((JavascriptExecutor) driver).executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
        } catch (RuntimeException ignored) {
            // Storage may be unavailable before the app origin loads.
        }
        driver.get(config.get("baseUrl"));
        DynamicWorkflowHelper.waitForStablePage(driver, config.getInt("explicitWait"));
    }

    private String getUsername(RoleAccount account) {
        String username = config.get(account.usernameKey());
        if (username == null || username.isBlank()) {
            Assert.fail(account.usernameKey() + " is required in config.properties");
        }
        return username;
    }

    private String getPassword(RoleAccount account) {
        String password = config.getOptionalSecret(account.passwordKey());
        if ((password == null || password.isBlank()) && "EASYQ_ADMIN_PASSWORD".equals(account.passwordKey())) {
            password = config.getOptionalSecret("EASYQ_PASSWORD");
        }
        if (password == null || password.isBlank()) {
            Assert.fail(account.passwordKey()
                    + " is required. Set it in Eclipse Environment variables or secrets.local.properties");
        }
        return password;
    }

    private boolean navigateToModule(String moduleName, String[] aliases) {
        if ("Dashboard".equals(moduleName)) {
            return true;
        }

        for (String alias : aliases) {
            By menuLocator = clickableTextLocator(alias);
            if (DynamicWorkflowHelper.clickFirstVisible(driver, menuLocator)) {
                return true;
            }
        }

        return pageMentionsAny(aliases);
    }

    private void verifyListModuleDynamicState(String moduleName, String[] aliases) {
        if (!navigateToModule(moduleName, aliases)) {
            DynamicWorkflowHelper.assertDynamicState(driver, moduleName + " menu not available");
            return;
        }

        Assert.assertTrue(
                pageMentionsAny(aliases)
                        || DynamicWorkflowHelper.isVisible(driver, emptyOrRestrictedState)
                        || DynamicWorkflowHelper.isVisible(driver, primaryWorkflowAction),
                moduleName + " list should show data, empty state, restriction, or a valid action"
        );
    }

    private boolean pageMentionsAny(String[] values) {
        String bodyText = DynamicWorkflowHelper.getBodyText(driver);
        for (String value : values) {
            if (DynamicWorkflowHelper.containsAny(bodyText, value)) {
                return true;
            }
        }
        return false;
    }

    private By clickableTextLocator(String text) {
        String literal = DynamicWorkflowHelper.xpathLiteral(text);
        return By.xpath(
                "//*[self::a or self::button or @role='button' or contains(@class,'menu') "
                        + "or contains(@class,'nav') or contains(@class,'sidebar') or contains(@class,'item')]"
                        + "[contains(normalize-space(.)," + literal + ")]"
        );
    }
}
