package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ActionHelper;
import utils.WaitHelper;

import java.time.Duration;

public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final ActionHelper action;
    private final WaitHelper waitHelper;

    private final By emailField = By.xpath("//input[@type='email' or contains(@placeholder,'Email') or contains(@placeholder,'email') or contains(@formcontrolname,'email')]");
    private final By passwordField = By.xpath("//input[@type='password' or contains(@placeholder,'Password') or contains(@placeholder,'password') or contains(@formcontrolname,'password')]");
    private final By loginButton = By.xpath("//button[contains(normalize-space(.),'Log In') or contains(normalize-space(.),'Login') or contains(normalize-space(.),'Sign in') or contains(normalize-space(.),'Sign In')]");

    public LoginPage(WebDriver driver, int explicitWaitSeconds) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds));
        this.action = new ActionHelper(driver, explicitWaitSeconds);
        this.waitHelper = new WaitHelper(driver, explicitWaitSeconds);
    }

    public boolean isLoaded() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).isDisplayed();
    }

    public void login(String email, String password) {
        action.clearAndType(emailField, email);
        action.clearAndType(passwordField, password);
        action.click(loginButton);
        waitHelper.waitForAppToLoad();
    }
}
