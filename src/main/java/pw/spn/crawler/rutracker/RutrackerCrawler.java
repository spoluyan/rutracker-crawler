package pw.spn.crawler.rutracker;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import pw.spn.crawler.rutracker.exception.RutrackerCrawlerException;
import pw.spn.crawler.rutracker.model.RutrackerLink;
import pw.spn.crawler.rutracker.model.RutrackerTopic;

public class RutrackerCrawler {
    private static final int WAIT_TIMEOUT_IN_SECONDS = 5;

    private final WebDriver webDriver;
    private final String login;
    private final String password;

    public RutrackerCrawler(String login, String password) {
        this(new HtmlUnitDriver(true), login, password);
    }

    public RutrackerCrawler(WebDriver webDriver, String login, String password) {
        this.webDriver = webDriver;
        this.login = login;
        this.password = password;
        login();
    }

    public List<RutrackerLink> search(String query) {
        if (query == null || query.trim().length() == 0) {
            return Collections.emptyList();
        }
        webDriver.findElement(WebElements.ID_SEARCH_BTN).click();
        waitForLoad();
        goToAdvancedSearchPage();
        webDriver.findElement(WebElements.ID_TITLE_SEARCH).sendKeys(query);
        webDriver.findElement(WebElements.ID_SEARCH_SUBMIT_BTN).click();
        waitForLoad();
        List<WebElement> searchResultTable = webDriver.findElements(WebElements.CSS_SELECTOR_SEARCH_RESULTS);
        if (searchResultTable.size() == 1 && searchResultTable.get(0).findElements(WebElements.TAG_TD).size() == 1) {
            return Collections.emptyList();
        }
        return searchResultTable.stream().filter(this::isTorrentApproved).map(this::mapResultRow)
                .collect(Collectors.toList());
    }

    private RutrackerLink mapResultRow(WebElement resultRow) {
        List<WebElement> rowColumns = resultRow.findElements(WebElements.TAG_TD);
        WebElement topicLink = getLinkInsideColumn(rowColumns.get(2));
        RutrackerTopic topic = mapTopicColumn(topicLink);
        WebElement torrentTitleAndUrl = getLinkInsideColumn(rowColumns.get(3));
        String torrentTopicId = torrentTitleAndUrl.getAttribute(WebElements.ATTR_TOPIC_ID);
        String title = torrentTitleAndUrl.getText();
        long size = Long
                .parseLong(getTextFromInvisibleElement(rowColumns.get(5).findElement(WebElements.TAG_U)));
        int seeds = Integer.parseInt(getTextFromInvisibleElement(rowColumns.get(6).findElement(WebElements.TAG_U)));
        if (seeds < 0) {
            seeds = 0;
        }
        int leechs = Integer.parseInt(rowColumns.get(7).findElement(WebElements.TAG_B).getText());
        return new RutrackerLink(topic, title, WebElements.TOPIC_BASE_URL + torrentTopicId,
                WebElements.DOWNLOAD_BASE_URL + torrentTopicId, size, seeds, leechs);
    }

    private String getTextFromInvisibleElement(WebElement element) {
        return element.getAttribute(WebElements.ATTR_INNER_HTML).replace("<" + element.getTagName() + ">", "")
                .replace("</" + element.getTagName() + ">", "").trim();
    }

    private RutrackerTopic mapTopicColumn(WebElement topicLink) {
        String href = topicLink.getAttribute(WebElements.ATTR_HREF);
        int topicId = Integer.parseInt(href.substring(href.indexOf("?f=") + 3, href.indexOf("&")));
        String topicName = topicLink.getText();
        RutrackerTopic topic = new RutrackerTopic(topicId, topicName);
        return topic;
    }

    private WebElement getLinkInsideColumn(WebElement rowColumn) {
        return rowColumn.findElement(WebElements.CSS_SELECTOR_LINK_INSIDE_COLUMN);
    }

    private boolean isTorrentApproved(WebElement resultRow) {
        List<WebElement> rowColumns = resultRow.findElements(WebElements.TAG_TD);
        return rowColumns.get(1).findElement(WebElements.TAG_SPAN).getAttribute(WebElements.ATTR_CLASS)
                .contains(WebElements.ATTR_CLASS_APPROVED_VALUE);
    }

    private void goToAdvancedSearchPage() {
        List<WebElement> advanceSearchLinks = webDriver.findElements(WebElements.ID_ADVANCED_SEARCH_LINK);
        if (!advanceSearchLinks.isEmpty()) {
            advanceSearchLinks.get(0).click();
            waitForLoad();
        }
    }

    private void login() {
        webDriver.get(WebElements.BASE_URL);
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

    private void waitForLoad() {
        WebDriverWait wait = new WebDriverWait(webDriver, WAIT_TIMEOUT_IN_SECONDS);
        wait.until((ExpectedCondition<Boolean>) wdriver -> ((JavascriptExecutor) webDriver)
                .executeScript(WebElements.JS_READY_STATE_SCRIPT).equals(WebElements.JS_READY_STATE_VALID_VALUE));
    }
}
