package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
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

public class EasyQHomepage {
    private WebDriver driver;
    private WebDriverWait wait;
    private final String baseUrl = "https://beta.easyqsolutions.com/#/easyqsolutions/login";
    private final String email = "varunt@easyqsolutions.com";

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        driver.manage().window().maximize();
        driver.get(baseUrl);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='email' or contains(@formcontrolname,'email')]")
        ));
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='password' or contains(@formcontrolname,'password')]")
        ));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(normalize-space(.),'Log In')]")
        ));

        emailField.sendKeys(email);
        passwordField.sendKeys(System.getenv("EASYQ_PASSWORD"));
        loginButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(normalize-space(.),'Dashboard')]")
        ));
    }

    @AfterMethod
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(priority = 1)
    public void dashboardDisplay() {
        WebElement dashboard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(normalize-space(.),'Dashboard')]")
        ));

        Assert.assertTrue(dashboard.isDisplayed(), "Dashboard should be displayed");
    }

    @Test(priority = 2)
    public void qualityPolicyCardDisplay() {
        WebElement qualityPolicy = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(normalize-space(.),'Quality Policy')]")
        ));

        Assert.assertTrue(qualityPolicy.isDisplayed(), "Quality Policy should be displayed");
    }

    @Test(priority = 3)
    public void qualityObjectiveCardDisplay() {
        WebElement qualityObjective = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(normalize-space(.),'Quality Objective')]")
        ));

        Assert.assertTrue(qualityObjective.isDisplayed(), "Quality Objective should be displayed");
    }

    @Test(priority = 4)
    public void documentsCardDisplay() {
        WebElement documents = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(normalize-space(.),'Documents')]")
        ));

        Assert.assertTrue(documents.isDisplayed(), "Documents should be displayed");
    }

    @Test(priority = 5)
    public void capaAndDeviationCardDisplay() {
        WebElement capaAndDeviation = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(normalize-space(.),'CAPA')]")
        ));

        Assert.assertTrue(capaAndDeviation.isDisplayed(), "CAPA & Deviation should be displayed");
    }

    @Test(priority = 6)
    public void trainingCardDisplay() {
        WebElement training = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(normalize-space(.),'Training')]")
        ));

        Assert.assertTrue(training.isDisplayed(), "Training should be displayed");
    }

    @Test(priority = 7)
    public void productsCardDisplay() {
        WebElement products = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(normalize-space(.),'Products')]")
        ));

        Assert.assertTrue(products.isDisplayed(), "Products should be displayed");
    }
}
