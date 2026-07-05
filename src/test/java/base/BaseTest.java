package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import utils.ConfigReader;
import utils.WaitHelper;

import java.time.Duration;

public class BaseTest {
    protected WebDriver driver;
    protected ConfigReader config;

    @BeforeMethod
    public void setUp() {
        config = new ConfigReader();
        driver = createDriver(config.get("browser"));
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(90));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(45));
        driver.get(config.get("baseUrl"));
        new WaitHelper(driver, config.getInt("explicitWait")).waitForAppToLoad();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private WebDriver createDriver(String browser) {
        String selectedBrowser = browser == null ? "chrome" : browser.toLowerCase();

        return switch (selectedBrowser) {
            case "edge" -> {
                WebDriverManager.edgedriver().setup();
                yield new EdgeDriver(new EdgeOptions());
            }
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                yield new FirefoxDriver(new FirefoxOptions());
            }
            default -> {
                WebDriverManager.chromedriver().setup();
                yield new ChromeDriver(new ChromeOptions());
            }
        };
    }
}
