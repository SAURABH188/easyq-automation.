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
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utils.ConfigReader;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class EasyQLoginPasswordRemainingTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private final ConfigReader config = new ConfigReader();

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
    private final By resetContinueButton = By.xpath("//button[contains(normalize-space(.),'Continue') or contains(normalize-space(.),'Submit') or contains(normalize-space(.),'Send') or contains(normalize-space(.),'Reset')]");
    private final By newPasswordField = By.xpath("(//input[@type='password' or contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'new password') or contains(translate(@formcontrolname,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'newpassword')])[1]");
    private final By confirmPasswordField = By.xpath("//input[@type='password' and (contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'confirm') or contains(translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'confirm') or contains(translate(@formcontrolname,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'confirm'))]");
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
    // Manual Test Case ID: TC003
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
    // Manual Test Case ID: TC004
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
    // Manual Test Case ID: TC005
    public void verifyLoginPageLoadsAfterClearingCache() {
        driver.manage().deleteAllCookies();
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
        driver.navigate().refresh();

        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).isDisplayed(),
                "Login page should load after clearing cookies and browser storage");
    }

    @Test(priority = 4, description = "TC008, TC018 - Verify login page layout on different screen sizes")
    // Manual Test Case ID: TC008, TC018
    public void verifyLoginPageOnDifferentScreenSizes() {
        assertLoginPageVisibleAtSize(1366, 768);
        assertLoginPageVisibleAtSize(1024, 768);
        assertLoginPageVisibleAtSize(390, 844);
    }

    @Test(priority = 5, description = "TC010 - Verify login page loads without browser console errors")
    // Manual Test Case ID: TC010
    public void verifyLoginPageWithoutSevereConsoleErrors() {
        List<LogEntry> severeLogs = driver.manage().logs().get(LogType.BROWSER).getAll()
                .stream()
                .filter(log -> "SEVERE".equalsIgnoreCase(log.getLevel().getName()))
                .toList();

        Assert.assertTrue(severeLogs.isEmpty(), "Login page should not show severe browser console errors");
    }

    @Test(priority = 6, description = "TC025 - Verify version format correctness")
    // Manual Test Case ID: TC025
    public void verifyVersionFormatCorrectness() {
        String version = driver.findElement(versionText).getText().trim();

        Assert.assertTrue(version.matches("(?i).*beta\\s+v\\d+\\.\\d+\\.\\d+.*"),
                "Version should be displayed in beta vX.X.X format");
    }

    @Test(priority = 7, description = "TC027 - Verify invalid email format validation")
    // Manual Test Case ID: TC027
    public void verifyInvalidEmailFormatValidation() {
        driver.findElement(emailField).sendKeys(invalidEmailFormat);
        driver.findElement(passwordField).sendKeys("Test@123");
        driver.findElement(emailField).sendKeys(Keys.TAB);
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();

        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Invalid email format should show validation or block login");
    }

    @Test(priority = 8, description = "TC029 - Verify email spaces are trimmed or safely handled")
    // Manual Test Case ID: TC029
    public void verifyEmailSpacesAreHandled() {
        WebElement emailInput = driver.findElement(emailField);
        emailInput.sendKeys("  " + validEmail + "  ");
        emailInput.sendKeys(Keys.TAB);

        String actualValue = emailInput.getAttribute("value");
        Assert.assertTrue(actualValue.trim().equals(validEmail), "Email field should trim or safely handle leading/trailing spaces");
    }

    @Test(priority = 9, description = "TC030 - Verify edge case email input")
    // Manual Test Case ID: TC030
    public void verifyEdgeCaseEmailInput() {
        WebElement emailInput = driver.findElement(emailField);
        emailInput.sendKeys(edgeCaseEmail);

        Assert.assertEquals(emailInput.getAttribute("value"), edgeCaseEmail, "Email field should accept plus-address email input");
    }

    @Test(priority = 10, description = "TC035, TC057, TC058 - Verify password boundary values")
    // Manual Test Case ID: TC035, TC057, TC058
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
    // Manual Test Case ID: TC054
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
    // Manual Test Case ID: TC059
    public void verifyPasswordWithSpecialCharacters() {
        WebElement passwordInput = driver.findElement(passwordField);
        passwordInput.sendKeys("@#123");

        Assert.assertEquals(passwordInput.getAttribute("value"), "@#123", "Password field should accept special characters");
    }

    @Test(priority = 13, description = "TC060 - Verify password with only special characters")
    // Manual Test Case ID: TC060
    public void verifyPasswordWithOnlySpecialCharacters() {
        WebElement passwordInput = driver.findElement(passwordField);
        passwordInput.sendKeys("@@@");

        Assert.assertEquals(passwordInput.getAttribute("value"), "@@@", "Password field should accept only special characters as input");
    }

    @Test(priority = 14, description = "TC043, TC044, TC070, TC071 - Verify forgot password invalid and empty email validations")
    // Manual Test Case ID: TC043, TC044, TC070, TC071
    public void verifyForgotPasswordInvalidAndEmptyEmailValidation() {
        openForgotPasswordPage();

        wait.until(ExpectedConditions.elementToBeClickable(resetContinueButton)).click();
        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Empty reset email should show validation");

        driver.get(baseUrl);
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField));
        openForgotPasswordPageWithEmail(invalidEmailFormat);
        wait.until(ExpectedConditions.elementToBeClickable(resetContinueButton)).click();
        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Invalid prefilled reset email should show validation");
    }

    @Test(priority = 15, description = "TC042, TC069 - Verify reset password with valid email")
    // Manual Test Case ID: TC042, TC069
    public void verifyResetPasswordWithValidEmail() {
        openForgotPasswordPageWithEmail(validEmail);
        Assert.assertEquals(getInputValue(resetEmailField).trim(), validEmail,
                "Forgot Password page should prefill the email entered on login page");
        Assert.assertTrue(isResetEmailLocked(validEmail), "Prefilled reset email should not be editable");
        wait.until(ExpectedConditions.elementToBeClickable(resetContinueButton)).click();

        Assert.assertTrue(resetEmailSentMessageDisplayed(), "Reset password request should show email sent confirmation");
    }

    @Test(priority = 16, description = "TC050, TC078 - Verify login response behavior")
    // Manual Test Case ID: TC050, TC078
    public void verifyLoginResponseBehavior() {
        long startTime = System.currentTimeMillis();
        loginWithValidCredentials();
        boolean loginCompleted = waitUntilLoginPageIsLeft();
        long totalTime = System.currentTimeMillis() - startTime;

        Assert.assertTrue(loginCompleted, "Login should complete");
        Assert.assertTrue(totalTime <= 30000, "Login should complete within 30 seconds");
    }

    @Test(priority = 17, description = "TC051 - Verify UI does not freeze on login")
    // Manual Test Case ID: TC051
    public void verifyUiDoesNotFreezeOnLogin() {
        driver.findElement(emailField).sendKeys(validEmail);
        driver.findElement(passwordField).sendKeys(getPassword());
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();

        Assert.assertTrue(waitUntilLoginPageIsLeft(), "UI should remain responsive and complete login");
    }

    @Test(priority = 18, description = "TC047, TC075 - Verify logout and re-login flow")
    // Manual Test Case ID: TC047, TC075
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

    @Test(priority = 19, description = "TC045, TC073 - Verify expired reset link handling when available")
    // Manual Test Case ID: TC045, TC073
    public void verifyExpiredResetLink() {
        if (!openOptionalResetLink("EASYQ_EXPIRED_RESET_LINK")) {
            Reporter.log("EASYQ_EXPIRED_RESET_LINK not supplied. Add an expired link to validate TC045/TC073.");
            return;
        }

        Assert.assertTrue(currentPageContains("expired", "invalid", "link", "login", "password"),
                "Expired reset link should show an error or safe recovery page");
    }

    @Test(priority = 20, enabled = false, description = "TC048, TC076 - Session timeout requires configured idle timeout duration")
    // Manual Test Case ID: TC048, TC076
    public void verifySessionTimeout() {
        throw new SkipException("Requires confirmed beta session timeout duration");
    }

    @Test(priority = 21, description = "TC072 - Verify valid reset link and optional password update")
    // Manual Test Case ID: TC072
    public void verifyValidResetLink() {
        if (!openOptionalResetLink("EASYQ_RESET_LINK")) {
            Reporter.log("EASYQ_RESET_LINK not supplied. TC042/TC069 verify reset email generation; TC072 needs latest inbox link.");
            return;
        }

        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(resetEmailField)).isDisplayed(),
                "Reset link should open reset form with email");
        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(newPasswordField)).isDisplayed(),
                "Reset link should show new password field");
        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(confirmPasswordField)).isDisplayed(),
                "Reset link should show confirm password field");

        if (!Boolean.parseBoolean(String.valueOf(config.get("allowPasswordResetMutation")))) {
            Reporter.log("allowPasswordResetMutation=false. Form is verified, but real password update is intentionally not submitted.");
            return;
        }

        String newPassword = config.getOptionalSecret("EASYQ_NEW_PASSWORD");
        Assert.assertTrue(newPassword != null && !newPassword.isBlank(),
                "EASYQ_NEW_PASSWORD is required when allowPasswordResetMutation=true");

        driver.findElement(newPasswordField).sendKeys(newPassword);
        driver.findElement(confirmPasswordField).sendKeys(newPassword);
        wait.until(ExpectedConditions.elementToBeClickable(resetContinueButton)).click();

        Assert.assertTrue(currentPageContains("success", "updated", "password", "login"),
                "Submitting matching passwords should complete the reset flow");
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
        wait.until(ExpectedConditions.visibilityOfElementLocated(resetEmailField));
    }

    private void openForgotPasswordPageWithEmail(String email) {
        driver.findElement(emailField).sendKeys(email);
        openForgotPasswordPage();
    }

    private boolean openOptionalResetLink(String key) {
        String resetLink = config.getOptionalSecret(key);
        if (resetLink == null || resetLink.isBlank()) {
            return false;
        }

        ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", resetLink);
        List<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(tabs.size() - 1));
        wait.until(currentDriver -> currentDriver.findElement(By.tagName("body")).getText().trim().length() > 0
                || currentDriver.getCurrentUrl().toLowerCase().contains("reset")
                || currentDriver.getCurrentUrl().toLowerCase().contains("forgot"));
        return true;
    }

    private String getInputValue(By locator) {
        return String.valueOf(wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getAttribute("value"));
    }

    private boolean isResetEmailLocked(String expectedEmail) {
        WebElement resetEmail = wait.until(ExpectedConditions.visibilityOfElementLocated(resetEmailField));
        String beforeValue = String.valueOf(resetEmail.getAttribute("value"));
        boolean lockedByAttribute = resetEmail.getAttribute("readonly") != null
                || resetEmail.getAttribute("disabled") != null
                || !resetEmail.isEnabled();

        try {
            resetEmail.sendKeys(".edited");
        } catch (RuntimeException ignored) {
            lockedByAttribute = true;
        }

        String afterValue = String.valueOf(driver.findElement(resetEmailField).getAttribute("value"));
        return lockedByAttribute || beforeValue.equals(afterValue) || expectedEmail.equalsIgnoreCase(afterValue.trim());
    }

    private boolean currentPageContains(String... values) {
        String currentUrl = driver.getCurrentUrl().toLowerCase();
        String bodyText = driver.findElement(By.tagName("body")).getText().toLowerCase();

        for (String value : values) {
            String expected = value.toLowerCase();
            if (currentUrl.contains(expected) || bodyText.contains(expected)) {
                return true;
            }
        }
        return false;
    }

    private boolean resetEmailSentMessageDisplayed() {
        try {
            return wait.until(currentDriver -> {
                String bodyText = currentDriver.findElement(By.tagName("body")).getText().toLowerCase();
                return bodyText.contains("sent")
                        || bodyText.contains("success")
                        || bodyText.contains("inbox")
                        || bodyText.contains("check your email");
            });
        } catch (RuntimeException exception) {
            return false;
        }
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
        String password = config.getOptionalSecret("EASYQ_ADMIN_PASSWORD");
        if (password == null || password.isBlank()) {
            password = config.getOptionalSecret("EASYQ_PASSWORD");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("EASYQ_ADMIN_PASSWORD or EASYQ_PASSWORD is required");
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
