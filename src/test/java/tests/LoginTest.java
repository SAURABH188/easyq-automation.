package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;
import utils.WaitHelper;

import java.time.Duration;

public class LoginTest extends BaseTest {
    private final By dashboardHeading = By.xpath("//*[contains(normalize-space(.),'Dashboard')]");

    @Test
    public void userCanOpenLoginPage() {
        LoginPage loginPage = new LoginPage(driver, config.getInt("explicitWait"));

        Assert.assertTrue(loginPage.isLoaded(), "Login page should be visible");
    }

    @Test
    public void validUserCanLogin() {
        LoginPage loginPage = new LoginPage(driver, config.getInt("explicitWait"));

        loginPage.login(config.getRequiredSecret("EASYQ_USERNAME"), config.getRequiredSecret("EASYQ_PASSWORD"));

        new WaitHelper(driver, config.getInt("explicitWait")).waitForAppToLoad();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getInt("explicitWait")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(dashboardHeading));

        Assert.assertTrue(driver.findElement(dashboardHeading).isDisplayed(), "Dashboard should be visible after successful login");
    }
}
