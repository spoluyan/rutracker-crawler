package pw.spn.crawler.rutracker.selenium;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import pw.spn.crawler.rutracker.exception.RutrackerCrawlerException;
import pw.spn.crawler.rutracker.model.RutrackerTopic;

public class RutrackerSeleniumService {
    private static final int WAIT_TIMEOUT_IN_SECONDS = 5;

    private final WebDriver webDriver;

    public RutrackerSeleniumService(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public void waitForLoad() {
        WebDriverWait wait = new WebDriverWait(webDriver, WAIT_TIMEOUT_IN_SECONDS);
        wait.until((ExpectedCondition<Boolean>) wdriver -> ((JavascriptExecutor) webDriver)
                .executeScript(WebElements.JS_READY_STATE_SCRIPT).equals(WebElements.JS_READY_STATE_VALID_VALUE));
    }

    public String getTextFromInvisibleElement(WebElement element) {
        return element.getAttribute(WebElements.ATTR_INNER_HTML).replace("<" + element.getTagName() + ">", "")
                .replace("</" + element.getTagName() + ">", "").trim();
    }

    public WebElement getLinkInsideColumn(WebElement rowColumn) {
        return rowColumn.findElement(WebElements.CSS_SELECTOR_LINK_INSIDE_COLUMN);
    }

    public void login(String login, String password) {
        webDriver.get(WebElements.BASE_URL_TRACKER);
        if (!isLoggedIn()) {
            webDriver.findElement(WebElements.CSS_SELECTOR_LOGIN_LINK).click();
            webDriver.findElement(WebElements.ID_LOGIN).sendKeys(login);
            webDriver.findElement(WebElements.ID_PWD).sendKeys(password);
            webDriver.findElement(WebElements.ID_LOGIN_BTN).click();
            waitForLoad();
        }
        if (!isLoggedIn()) {
            throw new RutrackerCrawlerException("Invalid login or password.");
        }
    }

    private boolean isLoggedIn() {
        return !webDriver.findElements(WebElements.CSS_SELECTOR_LOGGED_IN).isEmpty();
    }

    public void goToAdvancedSearchPage() {
        List<WebElement> advanceSearchLinks = webDriver.findElements(WebElements.ID_ADVANCED_SEARCH_LINK);
        if (!advanceSearchLinks.isEmpty()) {
            advanceSearchLinks.get(0).click();
            waitForLoad();
        }
    }

    public boolean isTorrentApproved(WebElement resultRow) {
        List<WebElement> rowColumns = resultRow.findElements(WebElements.TAG_TD);
        return rowColumns.get(1).findElement(WebElements.TAG_SPAN).getAttribute(WebElements.ATTR_CLASS)
                .contains(WebElements.ATTR_CLASS_APPROVED_VALUE);
    }

    public List<RutrackerTopic> loadTopics() {
        return webDriver.findElements(WebElements.CSS_SELECTOR_TOPICS).stream().map(this::mapRutrackerTopic)
                .collect(Collectors.toList());
    }

    private RutrackerTopic mapRutrackerTopic(WebElement topicLink) {
        String href = topicLink.getAttribute(WebElements.ATTR_HREF);
        int topicId = Integer.parseInt(href.substring(href.indexOf("?f=") + 3));
        String topicName = topicLink.getText();
        RutrackerTopic topic = new RutrackerTopic(topicId, topicName);
        return topic;
    }
}
