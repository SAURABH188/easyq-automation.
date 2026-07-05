package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;

import java.time.Duration;

public final class HamburgerNavigationHelper {
    private static final By HAMBURGER_TRIGGER = By.xpath("//*[self::button or self::a or @role='button'][contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu') or contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sidebar') or contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'toggle') or contains(translate(@title,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu') or contains(translate(@title,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sidebar') or contains(translate(@title,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'toggle') or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'hamburger') or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu') or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sidebar') or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'toggle') or .//*[contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'hamburger') or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu') or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sidebar') or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'toggle')]]");

    private HamburgerNavigationHelper() {
    }

    public static void openModule(WebDriver driver, WebDriverWait wait, By moduleTitle, String moduleLabel, String menuRegex) {
        openModule(driver, wait, moduleTitle, moduleLabel, menuRegex, true);
    }

    public static void openModule(WebDriver driver, WebDriverWait wait, By moduleTitle, String moduleLabel,
                                  String menuRegex, boolean pauseForObservation) {
        Reporter.log("NAV: Opening " + moduleLabel + " from hamburger/sidebar menu.", true);

        openHamburgerMenu(driver, pauseForObservation);
        boolean clicked = clickModuleInsideNavigationRoots(driver, menuRegex, pauseForObservation);
        if (!clicked) {
            Assert.fail(moduleLabel + " menu item was not found inside hamburger/sidebar navigation. URL: "
                    + safeCurrentUrl(driver) + " | Visible text: " + shortBodyText(driver));
        }

        waitForSmallDelay(pauseForObservation);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(45)).until(currentDriver ->
                    isModulePage(currentDriver, moduleTitle));
        } catch (RuntimeException exception) {
            Assert.fail(moduleLabel + " did not open from hamburger/sidebar menu. URL: "
                    + safeCurrentUrl(driver) + " | Visible text: " + shortBodyText(driver), exception);
        }
    }

    private static void openHamburgerMenu(WebDriver driver, boolean pauseForObservation) {
        Reporter.log("NAV: Opening hamburger/sidebar menu.", true);

        for (WebElement element : driver.findElements(HAMBURGER_TRIGGER)) {
            if (!isUsable(element)) {
                continue;
            }
            try {
                scrollIntoView(driver, element);
                safeClick(driver, element);
                waitForSmallDelay(pauseForObservation);
                return;
            } catch (RuntimeException ignored) {
                // Try the JS-scored fallback below.
            }
        }

        try {
            Object clicked = ((JavascriptExecutor) driver).executeScript(
                    "const visible = el => {"
                            + "  const r = el.getBoundingClientRect();"
                            + "  const s = window.getComputedStyle(el);"
                            + "  return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';"
                            + "};"
                            + "const textOf = el => [el.innerText, el.textContent, el.getAttribute('aria-label'), el.getAttribute('title'), el.getAttribute('class'), el.id]"
                            + "  .join(' ').replace(/\\s+/g, ' ').trim();"
                            + "const controls = Array.from(document.querySelectorAll('button,a,[role=\"button\"],svg,mat-icon,.hamburger,.menu,.menu-toggle,.sidebar-toggle,.navbar-toggler,.toggle'))"
                            + "  .filter(visible);"
                            + "let scored = controls.map(el => {"
                            + "  const target = el.closest('button,a,[role=\"button\"],.hamburger,.menu-toggle,.sidebar-toggle,.navbar-toggler,.toggle') || el;"
                            + "  const rect = target.getBoundingClientRect();"
                            + "  const label = textOf(target);"
                            + "  let score = 0;"
                            + "  if (/hamburger|menu|sidebar|toggle|collapse|expand/i.test(label)) score += 80;"
                            + "  if (rect.left < 140 && rect.top < 140) score += 45;"
                            + "  if (rect.width <= 90 && rect.height <= 90) score += 20;"
                            + "  if (/notification|bell|profile|avatar|logout|download|view/i.test(label)) score -= 100;"
                            + "  return {target, score};"
                            + "}).filter(item => item.score > 40)"
                            + "  .sort((a, b) => b.score - a.score);"
                            + "if (!scored.length) return false;"
                            + "scored[0].target.click();"
                            + "return true;");
            if (Boolean.TRUE.equals(clicked)) {
                waitForSmallDelay(pauseForObservation);
            }
        } catch (RuntimeException ignored) {
            // Navigation-root search can still work if the sidebar is already open.
        }
    }

    private static boolean clickModuleInsideNavigationRoots(WebDriver driver, String menuRegex, boolean pauseForObservation) {
        Reporter.log("NAV: Clicking module inside hamburger/sidebar menu.", true);
        try {
            Object clicked = ((JavascriptExecutor) driver).executeScript(
                    "const pattern = new RegExp(arguments[0], 'i');"
                            + "const visible = el => {"
                            + "  const r = el.getBoundingClientRect();"
                            + "  const s = window.getComputedStyle(el);"
                            + "  return r.width > 0 && r.height > 0 && s.display !== 'none' && s.visibility !== 'hidden';"
                            + "};"
                            + "const textOf = el => [el.innerText, el.textContent, el.getAttribute('aria-label'), el.getAttribute('title'), el.getAttribute('href')]"
                            + "  .join(' ').replace(/\\s+/g, ' ').trim();"
                            + "const roots = Array.from(document.querySelectorAll('aside,nav,[role=\"navigation\"],.sidebar,.side-bar,.sidenav,.drawer,.navigation,.navbar,[role=\"menu\"],.dropdown-menu,.mat-menu-panel,.cdk-overlay-pane,.popover'))"
                            + "  .filter(visible);"
                            + "const selector = 'a,button,[role=\"button\"],[role=\"link\"],[role=\"menuitem\"],li,span,div';"
                            + "const blocked = /no pending items|qms status|all tasks|my tasks|loading content|date of next|open action items|complaints reported|total products|review pending|approval pending|\\bview\\b/i;"
                            + "const scoreOf = el => {"
                            + "  const text = textOf(el);"
                            + "  const rect = el.getBoundingClientRect();"
                            + "  let score = 0;"
                            + "  if (text.length <= 70) score += 100;"
                            + "  if (rect.left <= 360) score += 80;"
                            + "  if (/^(quality policy|quality objective|responsibility and authority|management review|document management|documents|capa|capa & deviation|training|products|complaint management|complaints)$/i.test(text)) score += 120;"
                            + "  if (blocked.test(text)) score -= 500;"
                            + "  return score - text.length;"
                            + "};"
                            + "const isMenuCandidate = el => {"
                            + "  const text = textOf(el);"
                            + "  if (!pattern.test(text) || blocked.test(text) || text.length > 120) return false;"
                            + "  const card = el.closest('[class*=\"card\"],[class*=\"widget\"],section,article');"
                            + "  if (card && blocked.test(textOf(card))) return false;"
                            + "  return true;"
                            + "};"
                            + "const bestCandidate = items => items.filter(visible).filter(isMenuCandidate).sort((a, b) => scoreOf(b) - scoreOf(a))[0];"
                            + "for (const root of roots) {"
                            + "  const match = bestCandidate(Array.from(root.querySelectorAll(selector)));"
                            + "  if (match) {"
                            + "    const target = match.closest('a,button,[role=\"button\"],[role=\"link\"],[role=\"menuitem\"],li') || match;"
                            + "    target.scrollIntoView({block:'center'});"
                            + "    target.click();"
                            + "    return true;"
                            + "  }"
                            + "}"
                            + "const leftPanelMatch = bestCandidate(Array.from(document.querySelectorAll(selector)).filter(el => el.getBoundingClientRect().left <= 360));"
                            + "if (leftPanelMatch) {"
                            + "  const target = leftPanelMatch.closest('a,button,[role=\"button\"],[role=\"link\"],[role=\"menuitem\"],li') || leftPanelMatch;"
                            + "  target.scrollIntoView({block:'center'});"
                            + "  target.click();"
                            + "  return true;"
                            + "}"
                            + "return false;",
                    menuRegex);
            if (Boolean.TRUE.equals(clicked)) {
                waitForSmallDelay(pauseForObservation);
                return true;
            }
        } catch (RuntimeException ignored) {
            // Caller reports the visible state.
        }
        return false;
    }

    private static void waitForSmallDelay(boolean pauseForObservation) {
        if (!pauseForObservation) {
            return;
        }
        waitForSmallDelay();
    }

    private static boolean isModulePage(WebDriver driver, By moduleTitle) {
        String url = safeCurrentUrl(driver).toLowerCase();
        String bodyText = bodyText(driver);
        if (url.contains("dashboard") || bodyText.contains("QMS Status")) {
            return false;
        }
        for (WebElement element : driver.findElements(moduleTitle)) {
            if (isDisplayed(element)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isUsable(WebElement element) {
        try {
            return element.isDisplayed() && element.isEnabled();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static void safeClick(WebDriver driver, WebElement element) {
        try {
            element.click();
        } catch (RuntimeException exception) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private static void scrollIntoView(WebDriver driver, WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
        } catch (RuntimeException ignored) {
            // Scroll only improves reliability.
        }
    }

    private static String safeCurrentUrl(WebDriver driver) {
        try {
            return driver.getCurrentUrl();
        } catch (RuntimeException exception) {
            return "browser-url-unavailable";
        }
    }

    private static String bodyText(WebDriver driver) {
        try {
            return driver.findElement(By.tagName("body")).getText();
        } catch (RuntimeException exception) {
            return "";
        }
    }

    private static String shortBodyText(WebDriver driver) {
        String text = bodyText(driver).replaceAll("\\s+", " ").trim();
        return text.length() > 300 ? text.substring(0, 300) : text;
    }

    private static void waitForSmallDelay() {
        int delayMs = 1500;
        try {
            ConfigReader config = new ConfigReader();
            String configuredDelay = config.getOptionalSecret("EASYQ_VISUAL_DELAY_MS");
            if (configuredDelay == null || configuredDelay.isBlank()) {
                configuredDelay = config.get("actionDelayMs");
            }
            if (configuredDelay != null && !configuredDelay.isBlank()) {
                delayMs = Integer.parseInt(configuredDelay.trim());
            }
        } catch (RuntimeException ignored) {
            delayMs = 1500;
        }
        if (delayMs <= 0) {
            return;
        }

        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
