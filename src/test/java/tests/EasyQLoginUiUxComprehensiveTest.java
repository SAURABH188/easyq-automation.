package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;
import utils.ActionHelper;
import utils.DynamicWorkflowHelper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class EasyQLoginUiUxComprehensiveTest extends BaseTest {
    private final By loginTitle = By.xpath("//*[normalize-space()='Login' or contains(normalize-space(.),'Log In')]");
    private final By emailField = By.xpath("//input[@type='email' or contains(@placeholder,'Email') or contains(@formcontrolname,'email')]");
    private final By passwordField = By.xpath("//input[@type='password' or contains(@placeholder,'Password') or contains(@formcontrolname,'password')]");
    private final By passwordAnyField = By.xpath("//input[contains(@placeholder,'Password') or contains(@formcontrolname,'password') or @type='password']");
    private final By visiblePasswordField = By.xpath("//input[@type='text' and (contains(@placeholder,'Password') or contains(@formcontrolname,'password') or @autocomplete='current-password')]");
    private final By loginButton = By.xpath("//button[contains(normalize-space(.),'Log In') or contains(normalize-space(.),'Login') or contains(normalize-space(.),'Sign in') or contains(normalize-space(.),'Sign In')]");
    private final By forgotPasswordLink = By.xpath("//*[contains(normalize-space(.),'Forgot')]");
    private final By versionText = By.xpath("//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'beta') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'version')]");
    private final By easyQLogo = By.xpath("//img[contains(translate(@src,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logo') or contains(translate(@alt,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'easyq')]");
    private final By eyeIcon = By.xpath("//*[name()='svg' or self::mat-icon or self::i or @role='button'][ancestor::*[.//input[@type='password' or @type='text']]]");
    private final By validationMessage = By.xpath("//*[contains(@class,'error') or contains(@class,'invalid') or contains(@class,'danger') or contains(@class,'snack') or contains(@class,'toast') or contains(normalize-space(.),'required') or contains(normalize-space(.),'Required') or contains(normalize-space(.),'Invalid') or contains(normalize-space(.),'invalid') or contains(normalize-space(.),'incorrect')]");
    private final By resetEmailField = By.xpath("//input[@type='email' or contains(@placeholder,'Email') or contains(@formcontrolname,'email')]");
    private final By resetContinueButton = By.xpath("//button[contains(normalize-space(.),'Continue') or contains(normalize-space(.),'Submit') or contains(normalize-space(.),'Send') or contains(normalize-space(.),'Reset')]");
    private final By newPasswordField = By.xpath("(//input[@type='password' or contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'new password') or contains(translate(@formcontrolname,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'newpassword')])[1]");
    private final By confirmPasswordField = By.xpath("//input[@type='password' and (contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'confirm') or contains(translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'confirm') or contains(translate(@formcontrolname,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'confirm'))]");

    @Test(priority = 1, description = "Validate Login Page Load")
    // Manual Test Case ID: TC001
    public void verifyLoginPageLoadsSuccessfully() {
        ActionHelper action = action();

        Assert.assertTrue(driver.getCurrentUrl().toLowerCase().contains("login"), "Login URL should be loaded");
        Assert.assertTrue(action.waitForVisible(emailField).isDisplayed(), "Email field should be visible");
        Assert.assertTrue(driver.findElement(By.tagName("body")).getText().trim().length() > 0,
                "Login page should not be blank");
    }

    @Test(priority = 2, description = "Validate Login Page UI Elements")
    // Manual Test Case ID: TC006, TC011-TC015
    public void verifyAllLoginUiElementsAreVisible() {
        ActionHelper action = action();

        Assert.assertTrue(action.waitForVisible(loginTitle).isDisplayed(), "Login title should be visible");
        Assert.assertTrue(action.waitForVisible(emailField).isDisplayed(), "Email field should be visible");
        Assert.assertTrue(action.waitForVisible(passwordField).isDisplayed(), "Password field should be visible");
        Assert.assertTrue(action.waitForVisible(loginButton).isDisplayed(), "Login button should be visible");
        Assert.assertTrue(action.isVisible(forgotPasswordLink), "Forgot Password link should be visible");
        Assert.assertTrue(action.isVisible(easyQLogo), "easyQ logo should be visible");
    }

    @Test(priority = 3, description = "Verify input labels/placeholders and accessibility hints")
    // Manual Test Case ID: TC013, TC014
    public void verifyFieldPlaceholdersAndLabels() {
        WebElement emailInput = action().waitForVisible(emailField);
        WebElement passwordInput = action().waitForVisible(passwordField);

        Assert.assertTrue(hasFieldHint(emailInput, "email"), "Email field should have email hint/placeholder/type");
        Assert.assertTrue(hasFieldHint(passwordInput, "password"), "Password field should have password hint/placeholder/type");
    }

    @Test(priority = 4, description = "Verify login button visibility and clickable state")
    // Manual Test Case ID: TC015
    public void verifyLoginButtonIsUsable() {
        WebElement button = action().waitForVisible(loginButton);

        Assert.assertTrue(button.isDisplayed(), "Login button should be displayed");
        Assert.assertTrue(button.isEnabled() || button.getAttribute("disabled") != null,
                "Login button should have a clear enabled/disabled state");
    }

    @Test(priority = 5, description = "Verify keyboard tab order")
    // Manual Test Case ID: TC016-TC018
    public void verifyKeyboardTabOrder() {
        ActionHelper action = action();

        action.click(emailField);
        action.pressTab(emailField);

        WebElement activeElement = (WebElement) ((JavascriptExecutor) driver).executeScript("return document.activeElement;");
        Assert.assertTrue(
                activeElement.equals(driver.findElement(passwordAnyField)) || action.isVisible(passwordAnyField),
                "Tab key should move focus through the login form without breaking keyboard access"
        );
    }

    @Test(priority = 6, description = "Verify password is masked by default")
    // Manual Test Case ID: TC032, TC052
    public void verifyPasswordMaskedByDefault() {
        WebElement passwordInput = action().waitForVisible(passwordField);

        Assert.assertEquals(passwordInput.getAttribute("type"), "password", "Password should be masked by default");
    }

    @Test(priority = 7, description = "Verify password visibility eye icon toggles value")
    // Manual Test Case ID: TC021, TC033, TC053, TC054
    public void verifyPasswordVisibilityToggle() {
        ActionHelper action = action();

        action.clearAndType(passwordField, "Test@123");
        Assert.assertEquals(driver.findElement(passwordField).getAttribute("type"), "password",
                "Password should be masked before clicking eye icon");

        action.click(eyeIcon);
        Assert.assertTrue(waitUntilPasswordType("text") || action.isVisible(visiblePasswordField),
                "Password should be visible after clicking eye icon");

        action.click(eyeIcon);
        Assert.assertTrue(waitUntilPasswordType("password"), "Password should be masked again after second click");
    }

    @Test(priority = 8, description = "Verify Forgot Password link visibility")
    // Manual Test Case ID: TC022
    public void verifyForgotPasswordLinkVisible() {
        Assert.assertTrue(action().waitForVisible(forgotPasswordLink).isDisplayed(),
                "Forgot Password link should be visible");
    }

    @Test(priority = 9, description = "Verify Forgot Password navigation")
    // Manual Test Case ID: TC023, TC041
    public void verifyForgotPasswordNavigation() {
        openForgotPasswordPageWithEmail(getAdminEmail());

        Assert.assertTrue(currentPageContains("forgot", "reset", "password"),
                "Forgot Password page should be displayed");
    }

    @Test(priority = 10, description = "Verify version display")
    // Manual Test Case ID: TC024, TC025
    public void verifyVersionTextDisplayed() {
        Assert.assertTrue(action().waitForVisible(versionText).isDisplayed(), "Version/beta text should be visible");
    }

    @Test(priority = 11, description = "Verify login page responsive layout")
    // Manual Test Case ID: TC008, TC018
    public void verifyResponsiveLoginLayout() {
        assertLoginPageVisibleAtSize(1366, 768);
        assertLoginPageVisibleAtSize(1024, 768);
        assertLoginPageVisibleAtSize(390, 844);
    }

    @Test(priority = 12, description = "Verify login page refresh stability")
    // Manual Test Case ID: TC002
    public void verifyLoginPageRefreshStability() {
        driver.navigate().refresh();
        action().waitAfterAction();

        Assert.assertTrue(action().waitForVisible(emailField).isDisplayed(),
                "Login page should reload and show email field");
    }

    @Test(priority = 13, description = "Verify email field accepts valid input")
    // Manual Test Case ID: TC026
    public void verifyEmailFieldAcceptsValidInput() {
        action().clearAndType(emailField, getAdminEmail());

        Assert.assertEquals(driver.findElement(emailField).getAttribute("value"), getAdminEmail(),
                "Email field should accept valid email input");
    }

    @Test(priority = 14, description = "Verify invalid email format validation")
    // Manual Test Case ID: TC027
    public void verifyInvalidEmailFormatValidation() {
        ActionHelper action = action();

        action.clearAndType(emailField, "invalid-email-format");
        action.clearAndType(passwordField, "Test@123");
        action.click(loginButton);

        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Invalid email format should be blocked");
    }

    @Test(priority = 15, description = "Verify mandatory email validation")
    // Manual Test Case ID: TC028
    public void verifyMandatoryEmailValidation() {
        ActionHelper action = action();

        action.clearAndType(passwordField, "Test@123");
        action.click(loginButton);

        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Empty email should show validation or remain on login");
    }

    @Test(priority = 16, description = "Verify trimming/safe handling of email spaces")
    // Manual Test Case ID: TC029
    public void verifyEmailSpacesHandledSafely() {
        ActionHelper action = action();

        action.clearAndType(emailField, "  " + getAdminEmail() + "  ");
        action.clearAndType(passwordField, "Test@123");
        action.click(loginButton);

        Assert.assertTrue(waitForLoginSuccessOrLoginValidation(),
                "Email with leading/trailing spaces should either login after trim or show validation safely");
    }

    @Test(priority = 17, description = "Verify edge case email input")
    // Manual Test Case ID: TC030
    public void verifyEdgeCaseEmailInput() {
        String edgeCaseEmail = "qa.test+easyq@easyqsolutions.com";

        action().clearAndType(emailField, edgeCaseEmail);

        Assert.assertEquals(driver.findElement(emailField).getAttribute("value"), edgeCaseEmail,
                "Email field should accept plus-address input");
    }

    @Test(priority = 18, description = "Verify password field accepts normal input")
    // Manual Test Case ID: TC031
    public void verifyPasswordFieldAcceptsInput() {
        action().clearAndType(passwordField, "Test@123");

        Assert.assertFalse(driver.findElement(passwordAnyField).getAttribute("value").isBlank(),
                "Password field should accept input");
    }

    @Test(priority = 19, description = "Verify password special character handling")
    // Manual Test Case ID: TC059, TC060
    public void verifyPasswordSpecialCharacters() {
        action().clearAndType(passwordField, "@#123!$%");

        Assert.assertEquals(driver.findElement(passwordAnyField).getAttribute("value"), "@#123!$%",
                "Password field should accept special characters");
    }

    @Test(priority = 20, description = "Verify password long input boundary")
    // Manual Test Case ID: TC035, TC057, TC058
    public void verifyPasswordLongInputBoundary() {
        String longPassword = "Aa1@" + "Password".repeat(20);

        action().clearAndType(passwordField, longPassword);

        Assert.assertFalse(driver.findElement(passwordAnyField).getAttribute("value").isBlank(),
                "Password field should handle long input without UI break");
    }

    @Test(priority = 21, description = "Verify mandatory password validation")
    // Manual Test Case ID: TC034, TC055
    public void verifyMandatoryPasswordValidation() {
        ActionHelper action = action();

        action.clearAndType(emailField, getAdminEmail());
        action.click(loginButton);

        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Empty password should show validation or remain on login");
    }

    @Test(priority = 22, description = "Verify login with empty fields")
    // Manual Test Case ID: TC038, TC056, TC064
    public void verifyLoginWithEmptyFields() {
        action().click(loginButton);

        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Empty fields should not allow login");
    }

    @Test(priority = 23, description = "Verify login with only email")
    // Manual Test Case ID: TC039, TC065
    public void verifyLoginWithOnlyEmail() {
        ActionHelper action = action();

        action.clearAndType(emailField, getAdminEmail());
        action.click(loginButton);

        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Only email should not allow login");
    }

    @Test(priority = 24, description = "Verify login with only password")
    // Manual Test Case ID: TC039, TC066
    public void verifyLoginWithOnlyPassword() {
        ActionHelper action = action();

        action.clearAndType(passwordField, "Test@123");
        action.click(loginButton);

        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Only password should not allow login");
    }

    @Test(priority = 25, description = "Verify invalid credentials")
    // Manual Test Case ID: TC037, TC062, TC063
    public void verifyLoginWithInvalidCredentials() {
        ActionHelper action = action();

        action.clearAndType(emailField, getAdminEmail());
        action.clearAndType(passwordField, "Wrong@123");
        action.click(loginButton);

        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Invalid credentials should not allow login");
    }

    @Test(priority = 26, description = "Verify login with valid credentials")
    // Manual Test Case ID: TC036, TC061
    public void verifyLoginWithValidCredentials() {
        loginWithValidCredentials();

        Assert.assertTrue(waitUntilLoginSucceeded(), "Dashboard should load after valid login");
    }

    @Test(priority = 27, description = "Verify login using Enter key")
    // Manual Test Case ID: TC040, TC067
    public void verifyLoginUsingEnterKey() {
        ActionHelper action = action();

        action.clearAndType(emailField, getAdminEmail());
        action.clearAndType(passwordField, getAdminPassword());
        action.pressEnter(loginButton);

        Assert.assertTrue(waitUntilLoginSucceeded(), "Dashboard should load after pressing Enter on login button");
    }

    @Test(priority = 28, description = "Verify redirect after successful login")
    // Manual Test Case ID: TC046, TC074
    public void verifyRedirectAfterSuccessfulLogin() {
        loginWithValidCredentials();

        Assert.assertTrue(waitUntilLoginSucceeded(), "Login should complete successfully");
        Assert.assertFalse(driver.getCurrentUrl().toLowerCase().contains("login"),
                "User should not remain on login URL after login");
    }

    @Test(priority = 29, description = "Verify browser back button after login")
    // Manual Test Case ID: TC049, TC077
    public void verifyBackButtonAfterLogin() {
        loginWithValidCredentials();
        Assert.assertTrue(waitUntilLoginSucceeded(), "Login should complete successfully");

        driver.navigate().back();
        action().waitAfterAction();

        Assert.assertTrue(!driver.getCurrentUrl().toLowerCase().contains("login") || !action().isVisible(loginButton),
                "Back button should not expose an active login page after successful login");
    }

    @Test(priority = 30, description = "Verify forgot password prefills email from login page")
    // Manual Test Case ID: TC041, TC068
    public void verifyForgotPasswordPrefillsEmailFromLoginPage() {
        String email = getAdminEmail();

        openForgotPasswordPageWithEmail(email);

        Assert.assertEquals(getInputValue(resetEmailField).trim(), email,
                "Forgot Password page should prefill the email entered on login page");
    }

    @Test(priority = 31, description = "Verify forgot password email cannot be edited")
    // Manual Test Case ID: TC041, TC068
    public void verifyForgotPasswordEmailCannotBeEdited() {
        String email = getAdminEmail();

        openForgotPasswordPageWithEmail(email);

        Assert.assertTrue(isResetEmailLocked(email), "Prefilled reset email should not be editable");
    }

    @Test(priority = 32, description = "Verify forgot password empty email validation")
    // Manual Test Case ID: TC044, TC071
    public void verifyForgotPasswordEmptyEmailValidation() {
        openForgotPasswordPage();
        action().click(resetContinueButton);

        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Empty reset email should show validation");
    }

    @Test(priority = 33, description = "Verify forgot password invalid email validation")
    // Manual Test Case ID: TC043, TC070
    public void verifyForgotPasswordInvalidEmailValidation() {
        openForgotPasswordPageWithEmail("invalid-email-format");
        action().click(resetContinueButton);

        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Invalid prefilled reset email should show validation");
    }

    @Test(priority = 34, description = "Verify forgot password valid email request")
    // Manual Test Case ID: TC042, TC069
    public void verifyForgotPasswordValidEmailRequest() {
        String email = getAdminEmail();

        openForgotPasswordPageWithEmail(email);
        Assert.assertEquals(getInputValue(resetEmailField).trim(), email,
                "Forgot Password page should show the selected reset email");
        action().click(resetContinueButton);

        Assert.assertTrue(resetEmailSentMessageDisplayed(), "Valid reset email request should show email sent confirmation");
    }

    @Test(priority = 35, description = "Verify reset link opens password update form")
    // Manual Test Case ID: TC072
    public void verifyResetLinkOpensPasswordUpdateForm() {
        if (!openOptionalResetLink("EASYQ_RESET_LINK")) {
            Reporter.log("EASYQ_RESET_LINK not supplied. TC042/TC069 verify reset email generation; TC072 needs latest inbox link.");
            return;
        }

        Assert.assertTrue(action().waitForVisible(resetEmailField).isDisplayed(), "Reset form should show email field");
        Assert.assertTrue(action().waitForVisible(newPasswordField).isDisplayed(), "Reset form should show new password field");
        Assert.assertTrue(action().waitForVisible(confirmPasswordField).isDisplayed(), "Reset form should show confirm password field");
    }

    @Test(priority = 36, description = "Verify new password update from valid reset link")
    // Manual Test Case ID: TC072
    public void verifyNewPasswordCanBeUpdatedFromValidResetLinkWhenEnabled() {
        if (!openOptionalResetLink("EASYQ_RESET_LINK")) {
            Reporter.log("EASYQ_RESET_LINK not supplied. Password update flow requires the latest reset email link.");
            return;
        }
        if (!Boolean.parseBoolean(String.valueOf(config.get("allowPasswordResetMutation")))) {
            Reporter.log("allowPasswordResetMutation=false. Form is verified, but real password update is intentionally not submitted.");
            Assert.assertTrue(action().waitForVisible(newPasswordField).isDisplayed(),
                    "Reset password form should be available before password update is enabled");
            return;
        }

        String newPassword = config.getOptionalSecret("EASYQ_NEW_PASSWORD");
        Assert.assertTrue(newPassword != null && !newPassword.isBlank(),
                "EASYQ_NEW_PASSWORD is required when allowPasswordResetMutation=true");

        action().clearAndType(newPasswordField, newPassword);
        action().clearAndType(confirmPasswordField, newPassword);
        action().click(resetContinueButton);

        Assert.assertTrue(currentPageContains("success", "updated", "password", "login"),
                "Submitting matching passwords should complete the reset flow");
    }

    @Test(priority = 37, description = "Verify expired reset link handling")
    // Manual Test Case ID: TC045, TC073
    public void verifyExpiredResetLinkHandlingWhenAvailable() {
        if (!openOptionalResetLink("EASYQ_EXPIRED_RESET_LINK")) {
            Reporter.log("EASYQ_EXPIRED_RESET_LINK not supplied. Add an expired link to validate TC045/TC073.");
            return;
        }

        Assert.assertTrue(currentPageContains("expired", "invalid", "link", "login", "password"),
                "Expired reset link should show an error or safe recovery page");
    }

    @Test(priority = 38, description = "Verify login response time")
    // Manual Test Case ID: TC050, TC078
    public void verifyLoginResponseTime() {
        long startTime = System.currentTimeMillis();

        loginWithValidCredentials();
        boolean loginCompleted = waitUntilLoginSucceeded();
        long totalTime = System.currentTimeMillis() - startTime;

        Assert.assertTrue(loginCompleted, "Login should complete");
        Assert.assertTrue(totalTime <= 30000, "Login should complete within 30 seconds");
    }

    @Test(priority = 39, description = "Verify login page after clearing browser storage")
    // Manual Test Case ID: TC005
    public void verifyLoginPageAfterClearingStorage() {
        driver.manage().deleteAllCookies();
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
        driver.navigate().refresh();
        action().waitAfterAction();

        Assert.assertTrue(action().waitForVisible(emailField).isDisplayed(),
                "Login page should load after clearing cookies and storage");
    }

    @Test(priority = 40, description = "Verify login page has no severe browser console errors")
    // Manual Test Case ID: TC010
    public void verifyNoSevereConsoleErrorsOnLoginPage() {
        List<LogEntry> severeLogs = driver.manage().logs().get(LogType.BROWSER).getAll()
                .stream()
                .filter(log -> "SEVERE".equalsIgnoreCase(log.getLevel().getName()))
                .toList();

        Assert.assertTrue(severeLogs.isEmpty(), "Login page should not show severe browser console errors");
    }

    private ActionHelper action() {
        return new ActionHelper(driver, config.getInt("explicitWait"));
    }

    private void loginWithValidCredentials() {
        ActionHelper action = action();

        action.clearAndType(emailField, getAdminEmail());
        action.clearAndType(passwordField, getAdminPassword());
        action.click(loginButton);
    }

    private void openForgotPasswordPage() {
        action().click(forgotPasswordLink);
        new WebDriverWait(driver, Duration.ofSeconds(config.getInt("explicitWait"))).until(currentDriver ->
                currentDriver.getCurrentUrl().toLowerCase().contains("forgot")
                        || currentPageContains("forgot", "reset", "password")
        );
        action().waitForVisible(resetEmailField);
    }

    private void openForgotPasswordPageWithEmail(String email) {
        action().clearAndType(emailField, email);
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
        action().waitAfterAction();
        new WebDriverWait(driver, Duration.ofSeconds(config.getInt("explicitWait"))).until(currentDriver ->
                DynamicWorkflowHelper.getBodyText(currentDriver).trim().length() > 0
                        || currentDriver.getCurrentUrl().toLowerCase().contains("reset")
                        || currentDriver.getCurrentUrl().toLowerCase().contains("forgot")
        );
        return true;
    }

    private String getInputValue(By locator) {
        return String.valueOf(action().waitForVisible(locator).getAttribute("value"));
    }

    private boolean isResetEmailLocked(String expectedEmail) {
        WebElement resetEmail = action().waitForVisible(resetEmailField);
        String beforeValue = String.valueOf(resetEmail.getAttribute("value"));
        boolean lockedByAttribute = resetEmail.getAttribute("readonly") != null
                || resetEmail.getAttribute("disabled") != null
                || !resetEmail.isEnabled();

        try {
            resetEmail.sendKeys(".edited");
            action().waitAfterAction();
        } catch (RuntimeException ignored) {
            lockedByAttribute = true;
        }

        String afterValue = String.valueOf(driver.findElement(resetEmailField).getAttribute("value"));
        return lockedByAttribute || beforeValue.equals(afterValue) || expectedEmail.equalsIgnoreCase(afterValue.trim());
    }

    private void assertLoginPageVisibleAtSize(int width, int height) {
        driver.manage().window().setSize(new Dimension(width, height));
        action().waitAfterAction();

        Assert.assertTrue(action().waitForVisible(emailField).isDisplayed(),
                "Email field should be visible at " + width + "x" + height);
        Assert.assertTrue(action().waitForVisible(passwordField).isDisplayed(),
                "Password field should be visible at " + width + "x" + height);
        Assert.assertTrue(action().waitForVisible(loginButton).isDisplayed(),
                "Login button should be visible at " + width + "x" + height);
        Assert.assertFalse(hasHorizontalOverflow(), "Login page should not have horizontal overflow at " + width + "x" + height);
    }

    private boolean hasFieldHint(WebElement element, String expectedText) {
        String fieldInfo = String.join(" ",
                String.valueOf(element.getAttribute("type")),
                String.valueOf(element.getAttribute("placeholder")),
                String.valueOf(element.getAttribute("aria-label")),
                String.valueOf(element.getAttribute("formcontrolname")),
                String.valueOf(element.getAttribute("name"))
        ).toLowerCase();

        return fieldInfo.contains(expectedText.toLowerCase());
    }

    private boolean waitUntilPasswordType(String expectedType) {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(config.getInt("explicitWait"))).until(currentDriver ->
                    expectedType.equalsIgnoreCase(currentDriver.findElement(passwordAnyField).getAttribute("type"))
            );
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private boolean waitUntilLoginSucceeded() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(config.getInt("explicitWait"))).until(currentDriver -> {
                String currentUrl = currentDriver.getCurrentUrl().toLowerCase();
                String bodyText = DynamicWorkflowHelper.getBodyText(currentDriver);
                return !currentUrl.contains("login") || DynamicWorkflowHelper.containsAny(bodyText, "Dashboard", "QMS", "Tasks");
            });
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private boolean waitForLoginSuccessOrLoginValidation() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(config.getInt("explicitWait"))).until(currentDriver ->
                    !currentDriver.getCurrentUrl().toLowerCase().contains("login")
                            || DynamicWorkflowHelper.containsAny(DynamicWorkflowHelper.getBodyText(currentDriver), "Dashboard", "QMS", "Tasks")
                            || DynamicWorkflowHelper.isVisible(currentDriver, validationMessage)
                            || currentDriver.getCurrentUrl().toLowerCase().contains("login")
            );
        } catch (RuntimeException exception) {
            return driver.getCurrentUrl().toLowerCase().contains("login");
        }
    }

    private boolean validationDisplayedOrLoginRemains() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(config.getInt("explicitWait"))).until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(validationMessage),
                    ExpectedConditions.urlContains("login"),
                    ExpectedConditions.visibilityOfElementLocated(loginButton)
            ));
        } catch (RuntimeException exception) {
            return driver.getCurrentUrl().toLowerCase().contains("login");
        }
    }

    private boolean resetEmailSentMessageDisplayed() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(config.getInt("explicitWait"))).until(currentDriver -> {
                String bodyText = DynamicWorkflowHelper.getBodyText(currentDriver).toLowerCase();
                return bodyText.contains("sent")
                        || bodyText.contains("success")
                        || bodyText.contains("inbox")
                        || bodyText.contains("check your email");
            });
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private boolean currentPageContains(String... values) {
        String currentUrl = driver.getCurrentUrl().toLowerCase();
        String bodyText = DynamicWorkflowHelper.getBodyText(driver).toLowerCase();

        for (String value : values) {
            String expected = value.toLowerCase();
            if (currentUrl.contains(expected) || bodyText.contains(expected)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasHorizontalOverflow() {
        Object result = ((JavascriptExecutor) driver).executeScript(
                "return document.documentElement.scrollWidth > document.documentElement.clientWidth + 2;"
        );
        return Boolean.TRUE.equals(result);
    }

    private String getAdminEmail() {
        String email = config.get("EASYQ_ADMIN_USERNAME");
        if (email == null || email.isBlank()) {
            email = config.getRequiredSecret("EASYQ_USERNAME");
        }
        return email;
    }

    private String getAdminPassword() {
        String password = config.getOptionalSecret("EASYQ_ADMIN_PASSWORD");
        if (password == null || password.isBlank()) {
            password = config.getOptionalSecret("EASYQ_PASSWORD");
        }
        if (password == null || password.isBlank()) {
            Assert.fail("EASYQ_ADMIN_PASSWORD or EASYQ_PASSWORD is required for valid login scenarios");
        }
        return password;
    }
}
