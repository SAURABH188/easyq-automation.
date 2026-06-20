package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.DynamicWorkflowHelper;

import java.time.Duration;

public class EasyQCredentialValidationTest extends BaseTest {
    private record RoleAccount(String roleName, String usernameKey, String passwordKey) {
    }

    private final By emailField = By.xpath("//input[@type='email' or contains(@placeholder,'Email') or contains(@placeholder,'email') or contains(@formcontrolname,'email')]");
    private final By passwordField = By.xpath("//input[@type='password' or contains(@placeholder,'Password') or contains(@placeholder,'password') or contains(@formcontrolname,'password')]");
    private final By loginButton = By.xpath("//button[contains(normalize-space(.),'Log In') or contains(normalize-space(.),'Login') or contains(normalize-space(.),'Sign in') or contains(normalize-space(.),'Sign In')]");
    private final By successfulLoginState = By.xpath("//*[contains(normalize-space(.),'Dashboard') or contains(normalize-space(.),'QMS') or contains(normalize-space(.),'Tasks')]");
    private final By loginErrorState = By.xpath(
            "//*[contains(@class,'error') or contains(@class,'invalid') or contains(@class,'danger') "
                    + "or contains(normalize-space(.),'Invalid') or contains(normalize-space(.),'incorrect') "
                    + "or contains(normalize-space(.),'Wrong') or contains(normalize-space(.),'failed') "
                    + "or contains(normalize-space(.),'required') or contains(normalize-space(.),'not found')]"
    );

    @DataProvider(name = "allRoleAccounts")
    public Object[][] allRoleAccounts() {
        return new Object[][]{
                {new RoleAccount("Admin - Varun", "EASYQ_ADMIN_USERNAME", "EASYQ_ADMIN_PASSWORD")},
                {new RoleAccount("Doc Controller - Pavan", "EASYQ_DOC_CONTROLLER_USERNAME", "EASYQ_DOC_CONTROLLER_PASSWORD")},
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

    @Test(priority = 1, dataProvider = "allRoleAccounts", description = "Verify each configured role user can login")
    // Test Case No: CRED_TC001 to CRED_TC010
    public void verifyEachConfiguredUserCanLogin(RoleAccount account) {
        String username = getUsername(account);
        String password = getPassword(account);

        Reporter.log("Checking login for " + account.roleName() + " (" + username + ")", true);

        driver.findElement(emailField).clear();
        driver.findElement(emailField).sendKeys(username);
        driver.findElement(passwordField).clear();
        driver.findElement(passwordField).sendKeys(password);
        driver.findElement(loginButton).click();

        boolean loginCompleted = waitForSuccessfulLoginOrError();
        if (!loginCompleted) {
            Assert.fail("Login did not complete for " + account.roleName() + " (" + username
                    + "). Check email/password or whether the beta environment is loading slowly.");
        }

        if (DynamicWorkflowHelper.isVisible(driver, loginErrorState)) {
            Assert.fail("Wrong/invalid credential detected for " + account.roleName() + " (" + username + ").");
        }

        Assert.assertTrue(
                DynamicWorkflowHelper.isVisible(driver, successfulLoginState)
                        || !driver.getCurrentUrl().toLowerCase().contains("login"),
                account.roleName() + " (" + username + ") should login successfully"
        );
    }

    private boolean waitForSuccessfulLoginOrError() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(config.getInt("explicitWait"))).until(currentDriver ->
                    DynamicWorkflowHelper.isVisible(currentDriver, successfulLoginState)
                            || DynamicWorkflowHelper.isVisible(currentDriver, loginErrorState)
                            || !currentDriver.getCurrentUrl().toLowerCase().contains("login")
            );
            DynamicWorkflowHelper.waitForStablePage(driver, config.getInt("explicitWait"));
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
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
}
