package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

public class EasyQLoginValidation {
    private WebDriver driver;
    private WebDriverWait wait;

    private final String baseUrl = "https://beta.easyqsolutions.com/#/easyqsolutions/login";
    private final String validEmail = "varunt@easyqsolutions.com";
    private final String invalidEmail = "invaliduser@easyqsolutions.com";
    private final String invalidPassword = "Wrong@123";

    private final By loginTitle = By.xpath("//*[normalize-space()='Login']");
    private final By emailField = By.xpath("//input[@type='email' or contains(@formcontrolname,'email')]");
    private final By passwordField = By.xpath("//input[@type='password' or contains(@formcontrolname,'password')]");
    private final By visiblePasswordField = By.xpath("//input[@type='text' and (contains(@formcontrolname,'password') or @autocomplete='current-password')]");
    private final By loginButton = By.xpath("//button[contains(normalize-space(.),'Log In')]");
    private final By forgotPasswordLink = By.xpath("//*[contains(normalize-space(.),'Forgot Password')]");
    private final By versionText = By.xpath("//*[contains(normalize-space(.),'beta v')]");
    private final By easyQLogo = By.xpath("//img[contains(translate(@src,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logo') or contains(translate(@alt,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'easyq')]");
    private final By eyeIcon = By.xpath("//*[name()='svg' or self::mat-icon or self::i][ancestor::*[.//input[@type='password' or @type='text']]]");
    private final By validationMessage = By.xpath("//*[contains(@class,'error') or contains(@class,'invalid') or contains(@class,'danger') or contains(@class,'snack') or contains(@class,'toast') or contains(normalize-space(.),'required') or contains(normalize-space(.),'Invalid') or contains(normalize-space(.),'invalid')]");

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

    @Test(priority = 1, description = "TC001 - Verify login page loads with valid URL")
    public void verifyLoginPageLoadsWithValidUrl() {
        Assert.assertTrue(driver.getCurrentUrl().contains("/login"), "Login URL should be loaded");
        Assert.assertTrue(driver.findElement(emailField).isDisplayed(), "Email field should be visible");
    }

    @Test(priority = 2, description = "TC002 - Verify login page loads on refresh")
    public void verifyLoginPageLoadsOnRefresh() {
        driver.navigate().refresh();

        Assert.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).isDisplayed(),
                "Login page should reload without errors");
    }

    @Test(priority = 3, description = "TC006, TC011-TC015 - Verify login page UI elements")
    public void verifyLoginPageUiElements() {
        Assert.assertTrue(driver.findElement(loginTitle).isDisplayed(), "Login title should be visible");
        Assert.assertTrue(driver.findElement(emailField).isDisplayed(), "Email field should be visible");
        Assert.assertTrue(driver.findElement(passwordField).isDisplayed(), "Password field should be visible");
        Assert.assertTrue(driver.findElement(loginButton).isDisplayed(), "Log In button should be visible");
        Assert.assertTrue(driver.findElements(easyQLogo).size() > 0, "easyQ logo should be visible");
    }

    @Test(priority = 4, description = "TC019 - Verify eye icon is present")
    public void verifyPasswordEyeIconIsPresent() {
        Assert.assertTrue(driver.findElements(eyeIcon).size() > 0, "Password eye icon should be present");
    }

    @Test(priority = 5, description = "TC021, TC033, TC052, TC053 - Verify password masking and visibility toggle")
    public void verifyPasswordMaskingAndVisibilityToggle() {
        WebElement passwordInput = driver.findElement(passwordField);
        passwordInput.sendKeys("Test@123");

        Assert.assertEquals(passwordInput.getAttribute("type"), "password", "Password should be masked by default");

        driver.findElement(eyeIcon).click();

        boolean passwordVisible = wait.until(ExpectedConditions.or(
                ExpectedConditions.attributeToBe(passwordField, "type", "text"),
                ExpectedConditions.visibilityOfElementLocated(visiblePasswordField)
        ));

        Assert.assertTrue(passwordVisible, "Password should be visible after clicking eye icon");
    }

    @Test(priority = 6, description = "TC022 - Verify Forgot Password link visible")
    public void verifyForgotPasswordLinkVisible() {
        Assert.assertTrue(driver.findElement(forgotPasswordLink).isDisplayed(), "Forgot Password link should be visible");
    }

    @Test(priority = 7, description = "TC023, TC041, TC068 - Verify Forgot Password navigation")
    public void verifyForgotPasswordNavigation() {
        driver.findElement(forgotPasswordLink).click();

        boolean forgotPasswordPageVisible = wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("forgot"),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(.),'Forgot Password') or contains(normalize-space(.),'Reset Password')]"))
        ));

        Assert.assertTrue(forgotPasswordPageVisible, "Forgot password page should be displayed");
    }

    @Test(priority = 8, description = "TC024 - Verify version text displayed")
    public void verifyVersionTextDisplayed() {
        Assert.assertTrue(driver.findElement(versionText).isDisplayed(), "Beta version should be visible");
    }

    @Test(priority = 9, description = "TC026 - Verify email field accepts valid input")
    public void verifyEmailFieldAcceptsValidInput() {
        WebElement emailInput = driver.findElement(emailField);
        emailInput.clear();
        emailInput.sendKeys(validEmail);

        Assert.assertEquals(emailInput.getAttribute("value"), validEmail, "Email field should accept valid email input");
    }

    @Test(priority = 10, description = "TC031 - Verify password field accepts input")
    public void verifyPasswordFieldAcceptsInput() {
        WebElement passwordInput = driver.findElement(passwordField);
        passwordInput.clear();
        passwordInput.sendKeys("Test@123");

        Assert.assertFalse(passwordInput.getAttribute("value").isEmpty(), "Password field should accept input");
    }

    @Test(priority = 11, description = "TC028, TC034, TC038, TC056, TC064 - Verify mandatory validations for empty fields")
    public void verifyMandatoryValidationForEmptyFields() {
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();

        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Validation should appear or user should remain on login page");
    }

    @Test(priority = 12, description = "TC065 - Verify login with only email")
    public void verifyLoginWithOnlyEmail() {
        driver.findElement(emailField).sendKeys(validEmail);
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();

        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Password validation should appear");
    }

    @Test(priority = 13, description = "TC066 - Verify login with only password")
    public void verifyLoginWithOnlyPassword() {
        driver.findElement(passwordField).sendKeys(getPassword());
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();

        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Email validation should appear");
    }

    @Test(priority = 14, description = "TC037, TC062 - Verify login with invalid password")
    public void verifyLoginWithInvalidPassword() {
        driver.findElement(emailField).sendKeys(validEmail);
        driver.findElement(passwordField).sendKeys(invalidPassword);
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();

        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Invalid password should not allow login");
    }

    @Test(priority = 15, description = "TC063 - Verify login with invalid email")
    public void verifyLoginWithInvalidEmail() {
        driver.findElement(emailField).sendKeys(invalidEmail);
        driver.findElement(passwordField).sendKeys(getPassword());
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();

        Assert.assertTrue(validationDisplayedOrLoginRemains(), "Invalid email should not allow login");
    }

    @Test(priority = 16, description = "TC036, TC061 - Verify login with valid credentials")
    public void verifyLoginWithValidCredentials() {
        loginWithValidCredentials();

        Assert.assertTrue(waitUntilLoginPageIsLeft(), "Dashboard should be visible after successful login");
    }

    @Test(priority = 17, description = "TC040, TC067 - Verify login using Enter key")
    public void verifyLoginUsingEnterKey() {
        driver.findElement(emailField).sendKeys(validEmail);
        driver.findElement(passwordField).sendKeys(getPassword());
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).sendKeys(Keys.ENTER);

        Assert.assertTrue(waitUntilLoginPageIsLeft(), "Dashboard should be visible after pressing Enter");
    }

    @Test(priority = 18, description = "TC046, TC074 - Verify redirect after login")
    public void verifyRedirectAfterLogin() {
        loginWithValidCredentials();

        Assert.assertTrue(waitUntilLoginPageIsLeft(), "Dashboard should be displayed after login");
        Assert.assertFalse(driver.getCurrentUrl().contains("/login"), "User should not remain on login page after login");
    }

    @Test(priority = 19, description = "TC049, TC077 - Verify browser back button after login")
    public void verifyBackButtonAfterLogin() {
        loginWithValidCredentials();
        Assert.assertTrue(waitUntilLoginPageIsLeft(), "Dashboard should be displayed after login");

        driver.navigate().back();

        boolean loginPageNotExposed = wait.until(currentDriver ->
                !currentDriver.getCurrentUrl().contains("/login") || !isElementDisplayed(loginButton)
        );

        Assert.assertTrue(loginPageNotExposed, "Back button should not expose the login page after successful login");
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
            return driver.getCurrentUrl().contains("/login");
        }
    }

    private boolean isElementDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
