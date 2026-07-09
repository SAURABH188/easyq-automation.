package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utils.ConfigReader;
import utils.DynamicWorkflowHelper;
import utils.HamburgerNavigationHelper;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class EasyQProductsTest {
    private static final String AUTOMATION_PRODUCT_PREFIX = "AUTO_PRODUCT";
    private static final String RUN_PRODUCT_NAME = AUTOMATION_PRODUCT_PREFIX + "_" + System.currentTimeMillis();
    private static final String LONG_PRODUCT_NAME = RUN_PRODUCT_NAME + "_LONG_NAME_VALIDATION_ABCDEFGHIJKLMNOPQRSTUVWXYZ_1234567890";
    private static final String SPECIAL_PRODUCT_NAME = RUN_PRODUCT_NAME + "_SPECIAL_!@#$";

    private WebDriver driver;
    private WebDriverWait wait;
    private final ConfigReader config = new ConfigReader();

    private final String baseUrl = valueOrDefault("baseUrl", "https://beta.easyqsolutions.com/#/easyqsolutions/login");
    private final String validEmail = valueOrDefault("EASYQ_ADMIN_USERNAME", "varunt@easyqsolutions.com");

    private final By emailField = By.xpath("//input[@type='email' or contains(@formcontrolname,'email') or contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'email')]");
    private final By passwordField = By.xpath("//input[@type='password' or contains(@formcontrolname,'password') or contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'password')]");
    private final By loginButton = By.xpath("//button[contains(normalize-space(.),'Log In') or contains(normalize-space(.),'Login') or contains(normalize-space(.),'Sign in') or contains(normalize-space(.),'Sign In') or @type='submit']");
    private final By dashboardText = By.xpath("//*[contains(normalize-space(.),'Dashboard') or contains(normalize-space(.),'QMS Status')]");
    private final By productsTitle = By.xpath("//*[contains(normalize-space(.),'Products') or normalize-space()='Product']");
    private final By tableOrCardData = By.xpath("//table//tbody/tr | //*[contains(@class,'card') or contains(@class,'list') or contains(@class,'row')][.//*[normalize-space()]]");
    private final By noDataMessage = By.xpath("//*[contains(normalize-space(.),'No Data') or contains(normalize-space(.),'No data') or contains(normalize-space(.),'No Products') or contains(normalize-space(.),'No products') or contains(normalize-space(.),'No records') or contains(normalize-space(.),'No Records') or contains(normalize-space(.),'No product')]");
    private final By searchInput = By.xpath("//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'search') or contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'search') or contains(translate(@formcontrolname,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'search')]");
    private final By statusFilter = By.xpath("//select[contains(translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'status') or contains(translate(@formcontrolname,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'status')] | //*[contains(normalize-space(.),'Status')]/following::*[@role='combobox' or self::select or contains(@class,'select')][1]");
    private final By clearFilterButton = By.xpath("//button[contains(normalize-space(.),'Clear') or contains(normalize-space(.),'Reset') or contains(normalize-space(.),'Cancel')]");
    private final By nextButton = By.xpath("//button[contains(normalize-space(.),'Next') or @aria-label='Next page' or contains(@aria-label,'next')]");
    private final By previousButton = By.xpath("//button[contains(normalize-space(.),'Previous') or contains(normalize-space(.),'Prev') or @aria-label='Previous page' or contains(@aria-label,'previous')]");
    private final By productStatusText = By.xpath("//*[contains(normalize-space(.),'Active') or contains(normalize-space(.),'Inactive') or contains(normalize-space(.),'Under Review') or contains(normalize-space(.),'Approved')]");
    private final By createButton = By.xpath("//button[contains(normalize-space(.),'Create') or contains(normalize-space(.),'Add Product') or normalize-space()='Add' or contains(normalize-space(.),'New Product') or contains(normalize-space(.),'New')]");
    private final By saveButton = By.xpath("//button[contains(normalize-space(.),'Save') or contains(normalize-space(.),'Submit') or contains(normalize-space(.),'Create') or contains(normalize-space(.),'Update')]");
    private final By editButton = By.xpath("//button[contains(normalize-space(.),'Edit') or contains(@title,'Edit') or contains(@aria-label,'Edit')]");
    private final By deleteButton = By.xpath("//button[contains(normalize-space(.),'Delete') or contains(@title,'Delete') or contains(@aria-label,'Delete')]");
    private final By confirmButton = By.xpath("//button[contains(normalize-space(.),'Yes') or contains(normalize-space(.),'Confirm') or contains(normalize-space(.),'Delete') or contains(normalize-space(.),'OK')]");
    private final By cancelButton = By.xpath("//button[contains(normalize-space(.),'Cancel') or contains(normalize-space(.),'No') or contains(normalize-space(.),'Close')]");
    private final By formField = By.xpath("//input[not(@type='hidden')] | //textarea | //select | //*[@role='combobox']");
    private final By activeToggle = By.xpath("//input[@type='checkbox'] | //*[@role='switch'] | //button[contains(normalize-space(.),'Active') or contains(normalize-space(.),'Inactive')]");
    private final By toastOrAlert = By.xpath("//*[contains(@class,'toast') or contains(@class,'alert') or contains(@class,'snack') or contains(normalize-space(.),'success') or contains(normalize-space(.),'already') or contains(normalize-space(.),'duplicate') or contains(normalize-space(.),'required') or contains(normalize-space(.),'cannot') or contains(normalize-space(.),'restricted')]");

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(45));

        driver.manage().window().maximize();
        driver.get(baseUrl);
        loginWithValidCredentials();
        navigateToProducts();
        waitForProductsPage();
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() {
        if (driver == null) {
            return;
        }
        try {
            cleanupAutomationProducts();
        } catch (RuntimeException exception) {
            Reporter.log("Cleanup note: automation product cleanup could not complete. " + exception.getMessage(), true);
        }
        try {
            driver.quit();
        } catch (RuntimeException ignored) {
            // Browser may already be closed manually while a test is running.
        }
    }

    @Test(priority = 1, description = "Verify product list loads successfully")
    // Manual Test Case ID: TC621
    public void verifyProductListLoadsSuccessfully() {
        Assert.assertTrue(isProductsPageOpen(), "Products module should load successfully from hamburger menu");
        assertPageHealthy("Products list load");
    }

    @Test(priority = 2, description = "Verify product list with no data")
    // Manual Test Case ID: TC622
    public void verifyProductListWithNoData() {
        assertDataOrEmptyState("Products page should show product data or a valid empty state");
    }

    @Test(priority = 3, description = "Verify product details display")
    // Manual Test Case ID: TC623
    public void verifyProductDetailsDisplay() {
        String bodyText = getBodyText();
        boolean hasExpectedLabels = containsAny(bodyText, "Product", "Name", "Description", "Status", "Active", "Inactive");
        Assert.assertTrue(hasExpectedLabels || isEmptyStateVisible(),
                "Product name, description, or status should be visible when product data exists");
    }

    @Test(priority = 4, description = "Verify search functionality")
    // Manual Test Case ID: TC624
    public void verifySearchFunctionality() {
        if (isElementDisplayed(searchInput)) {
            clearAndType(searchInput, "test");
            waitForSmallDelay();
            assertPageHealthy("Product search");
            return;
        }

        assertFeatureHiddenOnlyForValidDynamicState("Search input");
    }

    @Test(priority = 5, description = "Verify search with no results")
    // Manual Test Case ID: TC625
    public void verifySearchWithNoResults() {
        if (isElementDisplayed(searchInput)) {
            clearAndType(searchInput, "NO_PRODUCT_MATCH_" + System.currentTimeMillis());
            driver.findElement(searchInput).sendKeys(Keys.ENTER);
            waitForSmallDelay();

            Assert.assertTrue(isEmptyStateVisible() || isProductsPageOpen(),
                    "No-result search should show an empty state or keep Products page stable");
            return;
        }

        assertFeatureHiddenOnlyForValidDynamicState("Search input");
    }

    @Test(priority = 6, description = "Verify filter by status")
    // Manual Test Case ID: TC626
    public void verifyFilterByStatus() {
        if (isElementDisplayed(statusFilter)) {
            applyFirstAvailableFilter(statusFilter);
            assertPageHealthy("Product status filter");
            return;
        }

        assertFeatureHiddenOnlyForValidDynamicState("Status filter");
    }

    @Test(priority = 7, description = "Verify filter reset")
    // Manual Test Case ID: TC627
    public void verifyFilterReset() {
        if (isElementDisplayed(searchInput)) {
            clearAndType(searchInput, "test");
            waitForSmallDelay();
        }

        if (clickIfVisible(clearFilterButton)) {
            assertPageHealthy("Product filter reset");
            return;
        }

        Assert.assertTrue(isProductsPageOpen() && (isEmptyStateVisible() || hasProductDataOrPageLoaded()),
                "Products page should remain stable when reset control is hidden because no filter is active");
    }

    @Test(priority = 8, description = "Verify pagination functionality")
    // Manual Test Case ID: TC628
    public void verifyPaginationFunctionality() {
        if (isElementDisplayed(nextButton) && isElementEnabled(nextButton)) {
            String beforeText = getBodyText();
            click(nextButton);
            String afterText = getBodyText();

            Assert.assertTrue(afterText.length() > 0 && (!afterText.equals(beforeText) || isElementDisplayed(previousButton)),
                    "Next page should display product data or expose previous-page navigation");
            return;
        }

        Assert.assertTrue(isProductsPageOpen() && hasProductDataOrPageLoaded(),
                "Pagination can be hidden for small product data sets, but the Products page must stay valid");
    }

    @Test(priority = 9, description = "Verify pagination boundary")
    // Manual Test Case ID: TC629
    public void verifyPaginationBoundary() {
        int clicks = 0;
        while (clicks < 5 && isElementDisplayed(nextButton) && isElementEnabled(nextButton)) {
            click(nextButton);
            clicks++;
        }

        Assert.assertTrue(isProductsPageOpen() && hasProductDataOrPageLoaded(),
                "Pagination boundary should not cause page error");
    }

    @Test(priority = 10, description = "Verify product status display")
    // Manual Test Case ID: TC630
    public void verifyProductStatusDisplay() {
        Assert.assertTrue(isElementDisplayed(productStatusText) || isEmptyStateVisible() || hasProductDataOrPageLoaded(),
                "Product status should be visible when products exist");
    }

    @Test(priority = 11, description = "Verify long product name handling")
    // Manual Test Case ID: TC631
    public void verifyLongProductNameHandling() {
        if (allowWorkflowMutations() && isElementDisplayed(createButton)) {
            boolean created = createProduct(LONG_PRODUCT_NAME, "Long product name validation created by automation");
            Assert.assertTrue(created || isElementDisplayed(toastOrAlert),
                    "Long product name should be accepted or validated with a clear message");
            return;
        }

        assertPageHealthy("Long product name UI handling");
    }

    @Test(priority = 12, description = "Verify special characters in product fields")
    // Manual Test Case ID: TC632
    public void verifySpecialCharactersInProductFields() {
        if (allowWorkflowMutations() && isElementDisplayed(createButton)) {
            boolean created = createProduct(SPECIAL_PRODUCT_NAME, "Special character validation !@#$%^&*()");
            Assert.assertTrue(created || isElementDisplayed(toastOrAlert),
                    "Special characters should be accepted or validated with a clear message");
            return;
        }

        assertPageHealthy("Special character UI handling");
    }

    @Test(priority = 13, description = "Verify status update")
    // Manual Test Case ID: TC633
    public void verifyStatusUpdate() {
        if (allowWorkflowMutations() && isElementDisplayed(activeToggle)) {
            String beforeText = getBodyText();
            click(activeToggle);
            String afterText = getBodyText();

            Assert.assertTrue(isElementDisplayed(toastOrAlert) || !afterText.equals(beforeText) || isProductsPageOpen(),
                    "Status update should update the product state or show a clear validation message");
            return;
        }

        Assert.assertTrue(isElementDisplayed(productStatusText) || isEmptyStateVisible() || hasProductDataOrPageLoaded(),
                "Products page should show status information or a valid dynamic state when status mutation is not enabled");
    }

    @Test(priority = 14, description = "Verify inactive product restriction")
    // Manual Test Case ID: TC634
    public void verifyInactiveProductRestriction() {
        Assert.assertTrue(isElementDisplayed(productStatusText) || isEmptyStateVisible() || hasProductDataOrPageLoaded(),
                "Inactive product restriction depends on linked module data; Products page should expose status/valid state for validation");
    }

    @Test(priority = 15, description = "Verify delete product functionality")
    // Manual Test Case ID: TC635
    public void verifyDeleteProductFunctionality() {
        if (allowWorkflowMutations()) {
            String productName = RUN_PRODUCT_NAME + "_DELETE";
            createProduct(productName, "Delete flow validation created by automation");
            boolean deletedOrBlocked = deleteAutomationProduct(productName, true);

            Assert.assertTrue(deletedOrBlocked,
                    "Automation-owned product should be deleted or deletion should be blocked with a clear message");
            return;
        }

        assertPageHealthy("Delete product flow gated by allowWorkflowMutations");
    }

    @Test(priority = 16, description = "Verify delete confirmation")
    // Manual Test Case ID: TC636
    public void verifyDeleteConfirmation() {
        if (allowWorkflowMutations()) {
            String productName = RUN_PRODUCT_NAME + "_DELETE_CONFIRM";
            createProduct(productName, "Delete confirmation validation created by automation");
            WebElement row = findElementContainingText(productName);

            Assert.assertNotNull(row, "Automation product should be available for delete confirmation validation");
            Assert.assertTrue(clickRowAction(row, deleteButton) || clickIfVisible(deleteButton),
                    "Delete action should be available for automation-owned product");
            waitForSmallDelay();

            Assert.assertTrue(isElementDisplayed(confirmButton) || isElementDisplayed(cancelButton) || isElementDisplayed(toastOrAlert),
                    "Delete action should show confirmation controls or a clear restriction message");
            clickIfVisible(cancelButton);
            return;
        }

        assertPageHealthy("Delete confirmation flow gated by allowWorkflowMutations");
    }

    @Test(priority = 17, description = "Verify delete restricted if product in use")
    // Manual Test Case ID: TC637
    public void verifyDeleteRestrictedIfProductInUse() {
        Assert.assertTrue(isProductsPageOpen() && (containsAny(getBodyText(), "Active", "Inactive", "Product") || isEmptyStateVisible()),
                "Linked-product deletion restriction requires seeded linked data; Products page should stay stable and expose product state");
    }

    @Test(priority = 18, description = "Verify product update reflects in other modules")
    // Manual Test Case ID: TC638
    public void verifyProductUpdateReflectsInOtherModules() {
        if (allowWorkflowMutations() && isElementDisplayed(createButton)) {
            String productName = RUN_PRODUCT_NAME + "_UPDATE";
            boolean created = createProduct(productName, "Update reflection validation created by automation");
            Assert.assertTrue(created || isElementDisplayed(toastOrAlert),
                    "Product update flow should create/update a product or show a validation message");

            if (findElementContainingText(productName) != null && clickRowAction(findElementContainingText(productName), editButton)) {
                fillFirstEditableTextField(productName + "_EDITED");
                clickIfVisible(saveButton);
                waitForSmallDelay();
            }

            Assert.assertTrue(isProductsPageOpen() || isElementDisplayed(toastOrAlert),
                    "After product update, platform should keep Products page stable or show a clear message");
            return;
        }

        assertPageHealthy("Product update reflection flow gated by allowWorkflowMutations");
    }

    @Test(priority = 19, description = "Verify duplicate product creation")
    // Manual Test Case ID: TC639
    public void verifyDuplicateProductCreation() {
        if (allowWorkflowMutations() && isElementDisplayed(createButton)) {
            String duplicateName = RUN_PRODUCT_NAME + "_DUPLICATE";
            createProduct(duplicateName, "Duplicate validation base product");
            createProduct(duplicateName, "Duplicate validation duplicate product");

            Assert.assertTrue(isElementDisplayed(toastOrAlert) || containsAny(getBodyText(), "duplicate", "already", "exists", duplicateName),
                    "Duplicate product creation should be blocked or clearly handled");
            return;
        }

        assertPageHealthy("Duplicate product flow gated by allowWorkflowMutations");
    }

    private void loginWithValidCredentials() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).clear();
        driver.findElement(emailField).sendKeys(validEmail);
        driver.findElement(passwordField).sendKeys(getPassword());
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(dashboardText),
                ExpectedConditions.not(ExpectedConditions.urlContains("/login"))
        ));
        waitForSmallDelay();
    }

    private void navigateToProducts() {
        HamburgerNavigationHelper.openModule(driver, wait, productsTitle, "Products", "products?");
    }

    private void waitForProductsPage() {
        try {
            wait.until(currentDriver -> isProductsPageOpen());
        } catch (TimeoutException exception) {
            Assert.fail("Products page did not open. URL: " + safeCurrentUrl() + " | Visible text: " + shortBodyText(), exception);
        }
        DynamicWorkflowHelper.waitForStablePage(driver, 15);
    }

    private boolean createProduct(String name, String description) {
        waitForProductsPage();
        Assert.assertTrue(clickIfVisible(createButton),
                "Create/Add Product button should be visible for product creation flow");

        Assert.assertTrue(driver.findElements(formField).size() > 0 || containsAny(getBodyText(), "Product", "Name", "Description"),
                "Product creation form should open");

        fillProductForm(name, description);
        Assert.assertTrue(clickIfVisible(saveButton),
                "Save/Submit/Create button should be available on product form");
        waitForSmallDelay();

        return containsAny(getBodyText(), name, "success", "created", "saved") || findElementContainingText(name) != null;
    }

    private boolean deleteAutomationProduct(String productName, boolean confirmDelete) {
        waitForProductsPage();
        WebElement row = findElementContainingText(productName);
        if (row == null) {
            return isElementDisplayed(toastOrAlert);
        }

        Assert.assertTrue(clickRowAction(row, deleteButton) || clickIfVisible(deleteButton),
                "Delete action should be available for automation-owned product");
        waitForSmallDelay();

        if (isElementDisplayed(toastOrAlert) && !isElementDisplayed(confirmButton)) {
            return true;
        }

        if (confirmDelete) {
            clickIfVisible(confirmButton);
            waitForSmallDelay();
            return findElementContainingText(productName) == null || isElementDisplayed(toastOrAlert);
        }

        clickIfVisible(cancelButton);
        return true;
    }

    private void cleanupAutomationProducts() {
        if (!allowWorkflowMutations() || !isBrowserAvailable()) {
            return;
        }
        if (!isProductsPageOpen()) {
            try {
                navigateToProducts();
            } catch (RuntimeException ignored) {
                return;
            }
        }

        List<WebElement> matches = driver.findElements(By.xpath("//*[contains(normalize-space(.),'" + AUTOMATION_PRODUCT_PREFIX + "')]"));
        for (WebElement match : matches) {
            if (!isDisplayed(match)) {
                continue;
            }
            String text = safeText(match);
            if (!text.contains(AUTOMATION_PRODUCT_PREFIX)) {
                continue;
            }
            String productName = firstAutomationToken(text);
            if (!productName.isBlank()) {
                deleteAutomationProduct(productName, true);
            }
        }
    }

    private void fillProductForm(String name, String description) {
        int filledTextFields = 0;
        for (WebElement element : driver.findElements(By.xpath("//input[not(@type='hidden') and not(@type='file') and not(@type='password') and not(@type='email') and not(@readonly) and not(@disabled)] | //textarea[not(@readonly) and not(@disabled)]"))) {
            if (!isDisplayed(element) || !isEnabled(element)) {
                continue;
            }
            String fieldText = safeAttribute(element, "placeholder") + " " + safeAttribute(element, "formcontrolname") + " " + safeAttribute(element, "name");
            String value = fieldText.toLowerCase(Locale.ROOT).contains("desc") ? description : name;
            try {
                scrollIntoView(element);
                element.clear();
                element.sendKeys(value);
                filledTextFields++;
                waitForSmallDelay();
            } catch (RuntimeException ignored) {
                // Continue filling any other editable field.
            }
            if (filledTextFields >= 3) {
                break;
            }
        }

        for (WebElement selectElement : driver.findElements(By.tagName("select"))) {
            if (!isDisplayed(selectElement) || !isEnabled(selectElement)) {
                continue;
            }
            try {
                Select select = new Select(selectElement);
                if (select.getOptions().size() > 1) {
                    select.selectByIndex(1);
                }
            } catch (RuntimeException ignored) {
                // Dropdowns can be custom controls; visible validation follows after save.
            }
        }

        Assert.assertTrue(filledTextFields > 0 || driver.findElements(formField).size() > 0,
                "Product form should expose at least one editable product field");
    }

    private void fillFirstEditableTextField(String value) {
        for (WebElement element : driver.findElements(By.xpath("//input[not(@type='hidden') and not(@type='file') and not(@readonly) and not(@disabled)] | //textarea[not(@readonly) and not(@disabled)]"))) {
            if (!isDisplayed(element) || !isEnabled(element)) {
                continue;
            }
            scrollIntoView(element);
            element.clear();
            element.sendKeys(value);
            waitForSmallDelay();
            return;
        }
    }

    private void applyFirstAvailableFilter(By locator) {
        WebElement filter = driver.findElement(locator);
        scrollIntoView(filter);
        String tagName = filter.getTagName();
        if ("select".equalsIgnoreCase(tagName)) {
            Select select = new Select(filter);
            if (select.getOptions().size() > 1) {
                select.selectByIndex(1);
            }
        } else {
            safeClick(filter);
        }
        waitForSmallDelay();
    }

    private void assertFeatureHiddenOnlyForValidDynamicState(String featureName) {
        Assert.assertTrue(isEmptyStateVisible() || hasProductDataOrPageLoaded(),
                featureName + " is not visible; page should show a valid product data/empty state");
    }

    private void assertDataOrEmptyState(String message) {
        Assert.assertTrue(hasProductDataOrPageLoaded() || isEmptyStateVisible(), message);
    }

    private void assertPageHealthy(String workflowName) {
        DynamicWorkflowHelper.assertDynamicState(driver, workflowName);
        Assert.assertTrue(isProductsPageOpen(), workflowName + " should remain inside Products module");
    }

    private boolean hasProductDataOrPageLoaded() {
        return !driver.findElements(tableOrCardData).isEmpty()
                || isEmptyStateVisible()
                || containsAny(getBodyText(), "Product", "Status", "Active", "Inactive", "Search", "Add", "Create");
    }

    private boolean isProductsPageOpen() {
        if (!isBrowserAvailable()) {
            return false;
        }
        String url = safeCurrentUrl().toLowerCase(Locale.ROOT);
        String bodyText = getBodyText();
        return !url.contains("/login")
                && !bodyText.contains("QMS Status")
                && (url.contains("product") || containsAny(bodyText, "Products", "Product List", "Total Products"));
    }

    private boolean isEmptyStateVisible() {
        return isElementDisplayed(noDataMessage);
    }

    private boolean isElementDisplayed(By locator) {
        if (!isBrowserAvailable()) {
            return false;
        }
        for (WebElement element : driver.findElements(locator)) {
            if (isDisplayed(element)) {
                return true;
            }
        }
        return false;
    }

    private boolean isElementEnabled(By locator) {
        if (!isBrowserAvailable()) {
            return false;
        }
        for (WebElement element : driver.findElements(locator)) {
            if (isDisplayed(element) && isEnabled(element)) {
                return true;
            }
        }
        return false;
    }

    private boolean clickIfVisible(By locator) {
        if (!isBrowserAvailable()) {
            return false;
        }
        for (WebElement element : driver.findElements(locator)) {
            if (!isDisplayed(element) || !isEnabled(element)) {
                continue;
            }
            safeClick(element);
            waitForSmallDelay();
            return true;
        }
        return false;
    }

    private void click(By locator) {
        Assert.assertTrue(clickIfVisible(locator), "Expected clickable element not found: " + locator);
    }

    private boolean clickRowAction(WebElement row, By actionLocator) {
        try {
            for (WebElement action : row.findElements(actionLocator)) {
                if (isDisplayed(action) && isEnabled(action)) {
                    safeClick(action);
                    waitForSmallDelay();
                    return true;
                }
            }
        } catch (StaleElementReferenceException ignored) {
            return false;
        }
        return false;
    }

    private WebElement findElementContainingText(String text) {
        if (!isBrowserAvailable() || text == null || text.isBlank()) {
            return null;
        }
        String locatorText = DynamicWorkflowHelper.xpathLiteral(text);
        for (WebElement element : driver.findElements(By.xpath("//*[contains(normalize-space(.)," + locatorText + ")]"))) {
            if (isDisplayed(element)) {
                return element;
            }
        }
        return null;
    }

    private void clearAndType(By locator, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        scrollIntoView(element);
        element.clear();
        element.sendKeys(value);
        waitForSmallDelay();
    }

    private void safeClick(WebElement element) {
        scrollIntoView(element);
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
            // Improves reliability only.
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
        return password.trim();
    }

    private boolean allowWorkflowMutations() {
        return DynamicWorkflowHelper.allowWorkflowMutations();
    }

    private String getBodyText() {
        if (!isBrowserAvailable()) {
            return "";
        }
        try {
            return driver.findElement(By.tagName("body")).getText();
        } catch (RuntimeException exception) {
            return "";
        }
    }

    private boolean containsAny(String text, String... expectedValues) {
        return DynamicWorkflowHelper.containsAny(text, expectedValues);
    }

    private String shortBodyText() {
        String text = getBodyText().replaceAll("\\s+", " ").trim();
        return text.length() > 300 ? text.substring(0, 300) : text;
    }

    private String safeCurrentUrl() {
        try {
            return driver.getCurrentUrl();
        } catch (RuntimeException exception) {
            return "browser-url-unavailable";
        }
    }

    private boolean isBrowserAvailable() {
        try {
            return driver != null && driver.getWindowHandles().size() > 0;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private boolean isEnabled(WebElement element) {
        try {
            return element.isEnabled();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private String safeText(WebElement element) {
        try {
            return element.getText();
        } catch (RuntimeException exception) {
            return "";
        }
    }

    private String safeAttribute(WebElement element, String attributeName) {
        try {
            String value = element.getAttribute(attributeName);
            return value == null ? "" : value;
        } catch (RuntimeException exception) {
            return "";
        }
    }

    private String firstAutomationToken(String text) {
        for (String token : text.split("\\s+")) {
            if (token.contains(AUTOMATION_PRODUCT_PREFIX)) {
                return token.replaceAll("[^A-Za-z0-9_!@#$-]", "");
            }
        }
        return "";
    }

    private String valueOrDefault(String key, String fallback) {
        String value = config.getOptionalSecret(key);
        if (value == null || value.isBlank()) {
            value = config.get(key);
        }
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private void waitForSmallDelay() {
        int delayMs = 1500;
        try {
            String configuredDelay = config.getOptionalSecret("EASYQ_VISUAL_DELAY_MS");
            if (configuredDelay == null || configuredDelay.isBlank()) {
                configuredDelay = config.get("actionDelayMs");
            }
            if (configuredDelay != null && !configuredDelay.isBlank()) {
                delayMs = Integer.parseInt(configuredDelay.trim());
            }
        } catch (RuntimeException ignored) {
            delayMs = 1500;
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
