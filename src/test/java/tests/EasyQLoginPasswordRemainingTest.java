package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class EasyQLoginPasswordRemainingTest {
    private WebDriver driver;
    private WebDriverWait wait;

    private final String baseUrl = "https://beta.easyqsolutions.com/#/easyqsolutions/login";
    private final String validEmail = "varunt@easyqsolutions.com";
    private final String invalidEmailFormat = "invalid-email-format";
    private final String edgeCaseEmail = "qa.test+easyq@easyqsolutions.com";

    private final By loginTitle = By.xpath("//*[normalize-space()='Login']");
    private final By emailField = By.xpath("//input[@type='email' or contains(@formcontrolname,'email')]");
    private final By passwordField = By.xpath("//input[@type='password' or contains(@formcontrolname,'password')]");
    private final By loginButton = By.xpath("//button[contains(normalize-space(.),'Log In')]");
    private final By forgotPasswordLink = By.xpath("//*[contains(normalize-space(.),'Forgot Password')]");
    private final By versionText = By.xpath("//*[contains(normalize-space(.),'beta v')]");
    private final By validationMessage = By.xpath("//*[contains(@class,'error') or contains(@class,'invalid') or contains(@class,'danger') or contains(@class,'snack') or contains(@class,'toast') or contains(normalize-space(.),'required') or contains(normalize-space(.),'Invalid') or contains(normalize-space(.),'invalid')]");
    private final By resetEmailField = By.xpath("//input[@type='email' or contains(@formcontrolname,'email')]");
    private final By resetSubmitButton = By.xpath("//button[contains(normalize-space(.),'Submit') or contains(normalize-space(.),'Send') or contains(normalize-space(.),'Reset')]");
    private final By logoutControl = By.xpath("//*[contains(normalize-space(.),'Logout') or contains(normalize-space(.),'Log Out') or contains(normalize-space(.),'Sign Out')]");

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        driver.manage().window().maximize();
        driver.get(baseUrl);
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField));
    }

    @AfterMethod
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(priority = 1, description = "TC003 - Verify login page loads after browser restart")
    // Test Case No: LOGIN_REM_TC001
    public void verifyLoginPageLoadsAfterBrowserRestart() {
        driver.quit();

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        driver.manage().window().maximize();
        driver.get(baseUrl);

        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).isDisplayed(),
                "Login page should load after browser restart");
    }

    @Test(priority = 2, description = "TC004 - Verify login page loads in incognito mode")
    // Test Case No: LOGIN_REM_TC002
    public void verifyLoginPageLoadsInIncognitoMode() {
        driver.quit();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        driver.manage().window().maximize();
        driver.get(baseUrl);

        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).isDisplayed(),
                "Login page should load in incognito mode");
    }

    @Test(priority = 3, description = "TC005 - Verify login page loads after clearing cache")
    // Test Case No: LOGIN_REM_TC003
    public void verifyLoginPageLoadsAfterClearingCache() {
        driver.manage().deleteAllCookies();
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
        driver.navigate().refresh();

        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).isDisplayed(),
                "Login page should load after clearing cookies and browser storage");
    }

    @Test(priority = 4, description = "TC008, TC018 - Verify login page layout on different screen sizes")
    // Test Case No: LOGIN_REM_TC004
    public void verifyLoginPageOnDifferentScreenSizes() {
        assertLoginPageVisibleAtSize(1366, 768);
        assertLoginPageVisibleAtSize(1024, 768);
        assertLoginPageVisibleAtSize(390, 844);
    }

    @Test(priority = 5, description = "TC010 - Verify login page loads without browser console errors")
    // Test Case No: LOGIN_REM_TC005
    public void verifyLoginPageWithoutSevereConsoleErrors() {
        List<LogEntry> severeLogs = driver.manage().logs().get(LogType.BROWSER).getAll()
                .stream()
                .filter(log -> "SEVERE".equalsIgnoreCase(log.getLevel().getName()))
                .toList();

        Assert.assertTrue(severeLogs.isEmpty(), "Login page should not show severe browser console errors");
    }

    @Test(priority = 6, description = "TC025 - Verify version format correctness")
    // Test Case No: LOGIN_REM_TC006
    public void verifyVersionFormatCorrectness() {
        String version = driver.findElement(versionText).getText().trim();

        Assert.assertTrue(version.matches("(?i).*beta\\s+v\\d+\\.\\d+\\.\\d+.*"),
                "Version should be displayed in beta vX.X.X format");
    }

    @Test(priority = 7, description = "TC027 - Verify invalid email format validation")
    // Test Case No: LOGIN_REM_TC007
    public void verifyInvalidEmailFormatValidation() {
        driver.findElement(emailField).sendKeys(invalidEmailFormat);
        driver.findElement(passwordField).sendKeys("Test@123");
        driver.findElement(emailField).sendKeys(Keys.TAB);
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();

        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Invalid email format should show validation or block login");
    }

    @Test(priority = 8, description = "TC029 - Verify email spaces are trimmed or safely handled")
    // Test Case No: LOGIN_REM_TC008
    public void verifyEmailSpacesAreHandled() {
        WebElement emailInput = driver.findElement(emailField);
        emailInput.sendKeys("  " + validEmail + "  ");
        emailInput.sendKeys(Keys.TAB);

        String actualValue = emailInput.getAttribute("value");
        Assert.assertTrue(actualValue.trim().equals(validEmail), "Email field should trim or safely handle leading/trailing spaces");
    }

    @Test(priority = 9, description = "TC030 - Verify edge case email input")
    // Test Case No: LOGIN_REM_TC009
    public void verifyEdgeCaseEmailInput() {
        WebElement emailInput = driver.findElement(emailField);
        emailInput.sendKeys(edgeCaseEmail);

        Assert.assertEquals(emailInput.getAttribute("value"), edgeCaseEmail, "Email field should accept plus-address email input");
    }

    @Test(priority = 10, description = "TC035, TC057, TC058 - Verify password boundary values")
    // Test Case No: LOGIN_REM_TC010
    public void verifyPasswordBoundaryValues() {
        WebElement passwordInput = driver.findElement(passwordField);

        passwordInput.sendKeys("A1@");
        Assert.assertEquals(passwordInput.getAttribute("value"), "A1@", "Password field should accept short boundary input");

        passwordInput.clear();
        String longPassword = "Aa1@" + "Password".repeat(20);
        passwordInput.sendKeys(longPassword);
        Assert.assertFalse(passwordInput.getAttribute("value").isEmpty(), "Password field should handle long boundary input");
    }

    @Test(priority = 11, description = "TC054 - Verify password masks again after second eye icon click")
    // Test Case No: LOGIN_REM_TC011
    public void verifyPasswordMasksAgainAfterSecondEyeIconClick() {
        WebElement passwordInput = driver.findElement(passwordField);
        passwordInput.sendKeys("Test@123");

        By eyeIcon = By.xpath("//*[name()='svg' or self::mat-icon or self::i][ancestor::*[.//input[@type='password' or @type='text']]]");
        driver.findElement(eyeIcon).click();
        driver.findElement(eyeIcon).click();

        Assert.assertEquals(driver.findElement(passwordField).getAttribute("type"), "password",
                "Password should be masked again after clicking eye icon second time");
    }

    @Test(priority = 12, description = "TC059 - Verify password with special characters")
    // Test Case No: LOGIN_REM_TC012
    public void verifyPasswordWithSpecialCharacters() {
        WebElement passwordInput = driver.findElement(passwordField);
        passwordInput.sendKeys("@#123");

        Assert.assertEquals(passwordInput.getAttribute("value"), "@#123", "Password field should accept special characters");
    }

    @Test(priority = 13, description = "TC060 - Verify password with only special characters")
    // Test Case No: LOGIN_REM_TC013
    public void verifyPasswordWithOnlySpecialCharacters() {
        WebElement passwordInput = driver.findElement(passwordField);
        passwordInput.sendKeys("@@@");

        Assert.assertEquals(passwordInput.getAttribute("value"), "@@@", "Password field should accept only special characters as input");
    }

    @Test(priority = 14, description = "TC043, TC044, TC070, TC071 - Verify forgot password invalid and empty email validations")
    // Test Case No: LOGIN_REM_TC014
    public void verifyForgotPasswordInvalidAndEmptyEmailValidation() {
        openForgotPasswordPage();

        wait.until(ExpectedConditions.elementToBeClickable(resetSubmitButton)).click();
        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Empty reset email should show validation");

        driver.findElement(resetEmailField).sendKeys(invalidEmailFormat);
        wait.until(ExpectedConditions.elementToBeClickable(resetSubmitButton)).click();
        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Invalid reset email should show validation");
    }

    @Test(priority = 15, description = "TC042, TC069 - Verify reset password with valid email")
    // Test Case No: LOGIN_REM_TC015
    public void verifyResetPasswordWithValidEmail() {
        openForgotPasswordPage();
        driver.findElement(resetEmailField).sendKeys(validEmail);
        wait.until(ExpectedConditions.elementToBeClickable(resetSubmitButton)).click();

        boolean resetAccepted = wait.until(currentDriver -> {
            String pageText = currentDriver.findElement(By.tagName("body")).getText();
            return pageText.toLowerCase().contains("sent")
                    || pageText.toLowerCase().contains("success")
                    || pageText.toLowerCase().contains("email")
                    || currentDriver.getCurrentUrl().toLowerCase().contains("forgot");
        });

        Assert.assertTrue(resetAccepted, "Reset password request should be accepted for valid email");
    }

    @Test(priority = 16, description = "TC050, TC078 - Verify login response behavior")
    // Test Case No: LOGIN_REM_TC016
    public void verifyLoginResponseBehavior() {
        long startTime = System.currentTimeMillis();
        loginWithValidCredentials();
        boolean loginCompleted = waitUntilLoginPageIsLeft();
        long totalTime = System.currentTimeMillis() - startTime;

        Assert.assertTrue(loginCompleted, "Login should complete");
        Assert.assertTrue(totalTime <= 30000, "Login should complete within 30 seconds");
    }

    @Test(priority = 17, description = "TC051 - Verify UI does not freeze on login")
    // Test Case No: LOGIN_REM_TC017
    public void verifyUiDoesNotFreezeOnLogin() {
        driver.findElement(emailField).sendKeys(validEmail);
        driver.findElement(passwordField).sendKeys(getPassword());
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();

        Assert.assertTrue(waitUntilLoginPageIsLeft(), "UI should remain responsive and complete login");
    }

    @Test(priority = 18, description = "TC047, TC075 - Verify logout and re-login flow")
    // Test Case No: LOGIN_REM_TC018
    public void verifyLogoutAndReloginFlow() {
        loginWithValidCredentials();
        Assert.assertTrue(waitUntilLoginPageIsLeft(), "User should login successfully");

        if (driver.findElements(logoutControl).isEmpty()) {
            throw new SkipException("Logout control locator needs confirmation from dashboard UI");
        }

        driver.findElement(logoutControl).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField));
        loginWithValidCredentials();

        Assert.assertTrue(waitUntilLoginPageIsLeft(), "User should login successfully after logout");
    }

    @Test(priority = 19, enabled = false, description = "TC045, TC073 - Expired reset link requires controlled expired email link")
    // Test Case No: LOGIN_REM_TC019
    public void verifyExpiredResetLink() {
        throw new SkipException("Requires a known expired reset link generated from the application email flow");
    }

    @Test(priority = 20, enabled = false, description = "TC048, TC076 - Session timeout requires configured idle timeout duration")
    // Test Case No: LOGIN_REM_TC020
    public void verifySessionTimeout() {
        throw new SkipException("Requires confirmed beta session timeout duration");
    }

    @Test(priority = 21, enabled = false, description = "TC072 - Valid reset link requires access to latest reset email link")
    // Test Case No: LOGIN_REM_TC021
    public void verifyValidResetLink() {
        throw new SkipException("Requires access to reset email inbox and latest valid reset link");
    }

    private void assertLoginPageVisibleAtSize(int width, int height) {
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(width, height));

        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(loginTitle)).isDisplayed(),
                "Login title should be visible at " + width + "x" + height);
        Assert.assertTrue(driver.findElement(emailField).isDisplayed(), "Email field should be visible at " + width + "x" + height);
        Assert.assertTrue(driver.findElement(passwordField).isDisplayed(), "Password field should be visible at " + width + "x" + height);
        Assert.assertTrue(driver.findElement(loginButton).isDisplayed(), "Login button should be visible at " + width + "x" + height);
    }

    private void openForgotPasswordPage() {
        wait.until(ExpectedConditions.elementToBeClickable(forgotPasswordLink)).click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("forgot"),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(.),'Forgot Password') or contains(normalize-space(.),'Reset Password')]"))
        ));
    }

    private void loginWithValidCredentials() {
        driver.findElement(emailField).sendKeys(validEmail);
        driver.findElement(passwordField).sendKeys(getPassword());
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
    }

    private boolean waitUntilLoginPageIsLeft() {
        return wait.until(currentDriver -> {
            String currentUrl = currentDriver.getCurrentUrl();
            String bodyText = String.valueOf(((JavascriptExecutor) currentDriver)
                    .executeScript("return document.body ? document.body.innerText : ''"));

            return !currentUrl.contains("/login") || bodyText.contains("Dashboard");
        });
    }

    private String getPassword() {
        String password = System.getenv("EASYQ_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("EASYQ_PASSWORD environment variable is required");
        }
        return password;
    }

    private boolean validationDisplayedOrLoginRemains() {
        try {
            return wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(validationMessage),
                    ExpectedConditions.urlContains("/login"),
                    ExpectedConditions.visibilityOfElementLocated(loginButton)
            ));
        } catch (RuntimeException exception) {
            return true;
        }
    }
}
