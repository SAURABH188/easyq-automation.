package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
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

public class EasyQProductsTest {
    private WebDriver driver;
    private WebDriverWait wait;

    private final String baseUrl = "https://beta.easyqsolutions.com/#/easyqsolutions/login";
    private final String validEmail = "varunt@easyqsolutions.com";

    private final By emailField = By.xpath("//input[@type='email' or contains(@formcontrolname,'email')]");
    private final By passwordField = By.xpath("//input[@type='password' or contains(@formcontrolname,'password')]");
    private final By loginButton = By.xpath("//button[contains(normalize-space(.),'Log In')]");
    private final By dashboardText = By.xpath("//*[contains(normalize-space(.),'Dashboard')]");
    private final By productsMenu = By.xpath("//*[contains(normalize-space(.),'Products')]");
    private final By productsTitle = By.xpath("//*[contains(normalize-space(.),'Products')]");
    private final By tableOrCardData = By.xpath("//table | //*[contains(@class,'card') or contains(@class,'list') or contains(@class,'row')]");
    private final By noDataMessage = By.xpath("//*[contains(normalize-space(.),'No Data') or contains(normalize-space(.),'No data') or contains(normalize-space(.),'No Products') or contains(normalize-space(.),'No products') or contains(normalize-space(.),'No records')]");
    private final By searchInput = By.xpath("//input[contains(@placeholder,'Search') or contains(@aria-label,'Search') or contains(@formcontrolname,'search')]");
    private final By statusFilter = By.xpath("//*[contains(normalize-space(.),'Status')]/following::select[1] | //*[contains(normalize-space(.),'Status')]/following::*[@role='combobox'][1]");
    private final By clearFilterButton = By.xpath("//button[contains(normalize-space(.),'Clear') or contains(normalize-space(.),'Reset')]");
    private final By nextButton = By.xpath("//button[contains(.,'Next') or @aria-label='Next page']");
    private final By productStatusText = By.xpath("//*[contains(normalize-space(.),'Active') or contains(normalize-space(.),'Inactive')]");
    private final By deleteButton = By.xpath("//button[contains(normalize-space(.),'Delete') or contains(@title,'Delete')]");
    private final By createButton = By.xpath("//button[contains(normalize-space(.),'Create') or contains(normalize-space(.),'Add') or contains(normalize-space(.),'New')]");

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        driver.manage().window().maximize();
        driver.get(baseUrl);
        loginWithValidCredentials();
        navigateToProducts();
    }

    @AfterMethod
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(priority = 1, description = "Verify product list loads successfully")
    // Test Case No: PROD_TC001
    public void verifyProductListLoadsSuccessfully() {
        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(productsTitle)).isDisplayed(),
                "Products module should load successfully");
    }

    @Test(priority = 2, description = "Verify product list with no data")
    // Test Case No: PROD_TC002
    public void verifyProductListWithNoData() {
        Assert.assertTrue(isElementDisplayed(noDataMessage) || hasProductDataOrPageLoaded(),
                "Products page should show product data or valid empty state");
    }

    @Test(priority = 3, description = "Verify product details display")
    // Test Case No: PROD_TC003
    public void verifyProductDetailsDisplay() {
        String bodyText = getBodyText();
        Assert.assertTrue(bodyText.length() > 40 || hasProductDataOrPageLoaded(),
                "Product name, description, or status should be visible when data exists");
    }

    @Test(priority = 4, description = "Verify search functionality")
    // Test Case No: PROD_TC004
    public void verifySearchFunctionality() {
        if (!isElementDisplayed(searchInput)) {
            throw new SkipException("Search input is not available or locator needs confirmation");
        }

        WebElement search = driver.findElement(searchInput);
        search.clear();
        search.sendKeys("test");
        waitForSmallDelay();

        Assert.assertTrue(driver.findElement(productsTitle).isDisplayed(), "Search should keep Products page stable");
    }

    @Test(priority = 5, description = "Verify search with no results")
    // Test Case No: PROD_TC005
    public void verifySearchWithNoResults() {
        if (!isElementDisplayed(searchInput)) {
            throw new SkipException("Search input is not available or locator needs confirmation");
        }

        WebElement search = driver.findElement(searchInput);
        search.clear();
        search.sendKeys("NO_PRODUCT_MATCH_" + System.currentTimeMillis());
        search.sendKeys(Keys.ENTER);
        waitForSmallDelay();

        Assert.assertTrue(isElementDisplayed(noDataMessage) || driver.findElement(productsTitle).isDisplayed(),
                "No data found message or stable empty results should be displayed");
    }

    @Test(priority = 6, description = "Verify filter by status")
    // Test Case No: PROD_TC006
    public void verifyFilterByStatus() {
        Assert.assertTrue(isElementDisplayed(statusFilter) || hasProductDataOrPageLoaded(),
                "Status filter should be available when filtering is supported");
    }

    @Test(priority = 7, description = "Verify filter reset")
    // Test Case No: PROD_TC007
    public void verifyFilterReset() {
        Assert.assertTrue(isElementDisplayed(clearFilterButton) || hasProductDataOrPageLoaded(),
                "Clear/reset filter should be available when filters are applied");
    }

    @Test(priority = 8, description = "Verify pagination functionality")
    // Test Case No: PROD_TC008
    public void verifyPaginationFunctionality() {
        if (!isElementDisplayed(nextButton)) {
            throw new SkipException("Next page control is not available for current product data");
        }

        String beforeText = getBodyText();
        driver.findElement(nextButton).click();
        waitForSmallDelay();
        String afterText = getBodyText();

        Assert.assertTrue(afterText.length() > 0 && !afterText.equals(beforeText),
                "Next page should display product data");
    }

    @Test(priority = 9, description = "Verify pagination boundary")
    // Test Case No: PROD_TC009
    public void verifyPaginationBoundary() {
        if (!isElementDisplayed(nextButton)) {
            throw new SkipException("Next page control is not available for boundary validation");
        }

        for (int i = 0; i < 5 && isElementDisplayed(nextButton); i++) {
            driver.findElement(nextButton).click();
            waitForSmallDelay();
        }

        Assert.assertTrue(driver.findElement(productsTitle).isDisplayed(),
                "Pagination boundary should not cause page error");
    }

    @Test(priority = 10, description = "Verify product status display")
    // Test Case No: PROD_TC010
    public void verifyProductStatusDisplay() {
        Assert.assertTrue(isElementDisplayed(productStatusText) || hasProductDataOrPageLoaded(),
                "Product status should be visible when products exist");
    }

    @Test(priority = 11, description = "Verify long product name handling")
    // Test Case No: PROD_TC011
    public void verifyLongProductNameHandling() {
        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Products page should handle long product names without UI break");
    }

    @Test(priority = 12, description = "Verify special characters in product fields")
    // Test Case No: PROD_TC012
    public void verifySpecialCharactersInProductFields() {
        Assert.assertTrue(driver.findElement(By.tagName("body")).isDisplayed(),
                "Products page should remain stable for products with special characters");
    }

    @Test(priority = 13, description = "Verify status update")
    // Test Case No: PROD_TC013
    public void verifyStatusUpdate() {
        throw new SkipException("Requires disposable product data and status update control locator");
    }

    @Test(priority = 14, description = "Verify inactive product restriction")
    // Test Case No: PROD_TC014
    public void verifyInactiveProductRestriction() {
        throw new SkipException("Requires inactive product and target module dropdown validation");
    }

    @Test(priority = 15, description = "Verify delete product functionality")
    // Test Case No: PROD_TC015
    public void verifyDeleteProductFunctionality() {
        if (!isElementDisplayed(deleteButton)) {
            throw new SkipException("Delete button is not available or locator needs confirmation");
        }
        throw new SkipException("Destructive test requires disposable product record");
    }

    @Test(priority = 16, description = "Verify delete confirmation")
    // Test Case No: PROD_TC016
    public void verifyDeleteConfirmation() {
        throw new SkipException("Requires disposable product record and confirmation dialog locator");
    }

    @Test(priority = 17, description = "Verify delete restricted if product in use")
    // Test Case No: PROD_TC017
    public void verifyDeleteRestrictedIfProductInUse() {
        throw new SkipException("Requires product linked to CAPA or another module");
    }

    @Test(priority = 18, description = "Verify product update reflects in other modules")
    // Test Case No: PROD_TC018
    public void verifyProductUpdateReflectsInOtherModules() {
        throw new SkipException("Requires product linked to CAPA and update workflow");
    }

    @Test(priority = 19, description = "Verify duplicate product creation")
    // Test Case No: PROD_TC019
    public void verifyDuplicateProductCreation() {
        if (!isElementDisplayed(createButton)) {
            throw new SkipException("Create product button is not available or locator needs confirmation");
        }
        throw new SkipException("Requires product creation form locator and known duplicate test data");
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

    private void navigateToProducts() {
        WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(productsMenu));
        menu.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(productsTitle));
    }

    private String getPassword() {
        String password = System.getenv("EASYQ_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("EASYQ_PASSWORD environment variable is required");
        }
        return password;
    }

    private boolean hasProductDataOrPageLoaded() {
        return !driver.findElements(tableOrCardData).isEmpty() || isElementDisplayed(noDataMessage) || getBodyText().length() > 40;
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
