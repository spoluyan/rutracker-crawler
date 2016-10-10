package pw.spn.crawler.rutracker;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final RutrackerSeleniumService rutrackerSeleniumService;
    private final List<RutrackerTopic> topics;

    public RutrackerCrawler(String login, String password) {
        this(new HtmlUnitDriver(true), login, password);
    }

    public RutrackerCrawler(WebDriver webDriver, String login, String password) {
        this.rutrackerSeleniumService = new RutrackerSeleniumService(webDriver);
        rutrackerSeleniumService.login(login, password);
        topics = rutrackerSeleniumService.loadTopics().stream().map(this::mapTopic).collect(Collectors.toList());
    }

    public List<RutrackerLink> search(String query) {
        return search(query, null);
    }

    public List<RutrackerLink> search(String query, int... rutrackerTopicsIds) {
        if (rutrackerTopicsIds == null || rutrackerTopicsIds.length == 0) {
            rutrackerTopicsIds = new int[] { -1 };
        }
        List<RutrackerLink> result = new ArrayList<>();
        Arrays.stream(rutrackerTopicsIds).forEach(topicId -> result.addAll(search(query, topicId)));
        return result;
    }

    private List<RutrackerLink> search(String query, int topicId) {
        if (query == null || query.trim().length() == 0) {
            return Collections.emptyList();
        }
        List<WebElement> searchResultTable = rutrackerSeleniumService.performSearch(query, topicId);
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
        RutrackerTopic topic = mapTopic(topicLink);
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

    private RutrackerTopic mapTopic(WebElement topicLink) {
        String href = topicLink.getAttribute(WebElements.ATTR_HREF);
        int endIndex = href.indexOf("&");
        if (endIndex < 0) {
            endIndex = href.length();
        }
        int topicId = Integer.parseInt(href.substring(href.indexOf("?f=") + 3, endIndex));
        String topicName = topicLink.getText();
        RutrackerTopic topic = new RutrackerTopic(topicId, topicName);
        return topic;
    }
}
