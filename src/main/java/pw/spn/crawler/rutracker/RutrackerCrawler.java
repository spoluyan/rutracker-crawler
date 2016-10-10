package pw.spn.crawler.rutracker;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import pw.spn.crawler.rutracker.model.RutrackerLink;
import pw.spn.crawler.rutracker.model.RutrackerTopic;
import pw.spn.crawler.rutracker.selenium.RutrackerSeleniumService;
import pw.spn.crawler.rutracker.selenium.WebElements;

public class RutrackerCrawler {
    private final WebDriver webDriver;
    private final RutrackerSeleniumService rutrackerSeleniumService;
    private final List<RutrackerTopic> topics;

    public RutrackerCrawler(String login, String password) {
        this(new HtmlUnitDriver(true), login, password);
    }

    public RutrackerCrawler(WebDriver webDriver, String login, String password) {
        this.webDriver = webDriver;
        this.rutrackerSeleniumService = new RutrackerSeleniumService(webDriver);
        rutrackerSeleniumService.login(login, password);
        topics = rutrackerSeleniumService.loadTopics();
    }

    public List<RutrackerLink> search(String query) {
        if (query == null || query.trim().length() == 0) {
            return Collections.emptyList();
        }
        webDriver.findElement(WebElements.ID_SEARCH_BTN).click();
        rutrackerSeleniumService.waitForLoad();
        rutrackerSeleniumService.goToAdvancedSearchPage();
        webDriver.findElement(WebElements.ID_TITLE_SEARCH).sendKeys(query);
        webDriver.findElement(WebElements.ID_SEARCH_SUBMIT_BTN).click();
        rutrackerSeleniumService.waitForLoad();
        List<WebElement> searchResultTable = webDriver.findElements(WebElements.CSS_SELECTOR_SEARCH_RESULTS);
        if (searchResultTable.size() == 1 && searchResultTable.get(0).findElements(WebElements.TAG_TD).size() == 1) {
            return Collections.emptyList();
        }
        return searchResultTable.stream().filter(rutrackerSeleniumService::isTorrentApproved).map(this::mapResultRow)
                .collect(Collectors.toList());
    }

    public List<RutrackerTopic> getTopics() {
        return topics;
    }

    private RutrackerLink mapResultRow(WebElement resultRow) {
        List<WebElement> rowColumns = resultRow.findElements(WebElements.TAG_TD);
        WebElement topicLink = rutrackerSeleniumService.getLinkInsideColumn(rowColumns.get(2));
        RutrackerTopic topic = mapTopicColumn(topicLink);
        WebElement torrentTitleAndUrl = rutrackerSeleniumService.getLinkInsideColumn(rowColumns.get(3));
        String torrentTopicId = torrentTitleAndUrl.getAttribute(WebElements.ATTR_TOPIC_ID);
        String title = torrentTitleAndUrl.getText();
        long size = Long.parseLong(
                rutrackerSeleniumService.getTextFromInvisibleElement(rowColumns.get(5).findElement(WebElements.TAG_U)));
        int seeds = Integer.parseInt(
                rutrackerSeleniumService.getTextFromInvisibleElement(rowColumns.get(6).findElement(WebElements.TAG_U)));
        if (seeds < 0) {
            seeds = 0;
        }
        int leechs = Integer.parseInt(rowColumns.get(7).findElement(WebElements.TAG_B).getText());
        return new RutrackerLink(topic, title, WebElements.BASE_URL_TOPIC + torrentTopicId,
                WebElements.BASE_URL_DOWNLOAD + torrentTopicId, size, seeds, leechs);
    }

    private RutrackerTopic mapTopicColumn(WebElement topicLink) {
        String href = topicLink.getAttribute(WebElements.ATTR_HREF);
        int topicId = Integer.parseInt(href.substring(href.indexOf("?f=") + 3, href.indexOf("&")));
        String topicName = topicLink.getText();
        RutrackerTopic topic = new RutrackerTopic(topicId, topicName);
        return topic;
    }
}
