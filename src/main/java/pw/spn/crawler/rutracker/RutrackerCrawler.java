package pw.spn.crawler.rutracker;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import pw.spn.crawler.rutracker.exception.RutrackerCrawlerException;
import pw.spn.crawler.rutracker.http.RutrackerHttpService;
import pw.spn.crawler.rutracker.model.RutrackerLink;
import pw.spn.crawler.rutracker.model.RutrackerTopic;

public class RutrackerCrawler {
    private static final String TAG_TD = "td";
    private static final String TAG_SPAN = "span";
    private static final String TAG_U = "u";
    private static final String TAG_B = "b";
    private static final String CLASS_APPROVED = "tor-approved";
    private static final String ATTR_HREF = "href";
    private static final String ATTR_ABS_HREF = "abs:href";
    private static final String CSS_SELECTOR_A_IN_DIV = "div > a";

    private final RutrackerHttpService httpService = new RutrackerHttpService();
    private final List<RutrackerTopic> topics;

    public RutrackerCrawler(String username, String password) {
        httpService.login(username, password);
        topics = loadTopics();
    }

    private List<RutrackerTopic> loadTopics() {
        Elements topics = httpService.loadTopics();
        return topics.stream().map(this::mapElementToRutrackerTopic).collect(Collectors.toList());
    }

    private RutrackerTopic mapElementToRutrackerTopic(Element element) {
        String href = element.attr(ATTR_HREF);
        int endIndex = href.indexOf("&");
        if (endIndex == -1) {
            endIndex = href.length();
        }
        int id = Integer.parseInt(href.substring(href.indexOf("?f=") + 3, endIndex));
        String name = element.text();
        return new RutrackerTopic(id, name);
    }

    public List<RutrackerLink> search(String query) {
        return search(query, null);
    }

    public List<RutrackerLink> search(String query, Integer[] rutrackerTopicsIds) {
        if (query == null || query.trim().length() == 0) {
            return Collections.emptyList();
        }
        if (rutrackerTopicsIds == null || rutrackerTopicsIds.length == 0) {
            rutrackerTopicsIds = new Integer[] { -1 };
        }
        Elements searchResultElements = httpService.search(query, rutrackerTopicsIds);
        if (searchResultElements.size() == 0
                || (searchResultElements.size() == 1 && searchResultElements.get(0).select(TAG_TD).size() == 1)) {
            return Collections.emptyList();
        }
        return searchResultElements.stream().map(this::mapElementToRutrackerLink).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private RutrackerLink mapElementToRutrackerLink(Element element) {
        Elements tds = element.select(TAG_TD);
        boolean approved = tds.get(1).select(TAG_SPAN).hasClass(CLASS_APPROVED);
        if (!approved) {
            return null;
        }
        Element topicInfo = tds.get(2).select(CSS_SELECTOR_A_IN_DIV).first();
        RutrackerTopic topic = mapElementToRutrackerTopic(topicInfo);
        Element nameInfo = tds.get(3).select(CSS_SELECTOR_A_IN_DIV).first();
        String title = nameInfo.text();
        String url = nameInfo.attr(ATTR_ABS_HREF);
        String downloadUrl = url.replace("viewtopic", "dl");
        long sizeInBytes = Long.parseLong(tds.get(5).select(TAG_U).first().text());
        int seeds = Integer.parseInt(tds.get(6).select(TAG_U).first().text());
        if (seeds < 0) {
            seeds = 0;
        }
        int leechs = Integer.parseInt(tds.get(7).select(TAG_B).first().text());
        return new RutrackerLink(topic, title, url, downloadUrl, sizeInBytes, seeds, leechs);
    }

    public List<RutrackerTopic> getTopics() {
        return topics;
    }

    public byte[] downloadTorrent(String downloadUrl) {
        if (downloadUrl == null) {
            throw new RutrackerCrawlerException("downloadUrl can not be null");
        }
        return httpService.downloadFile(downloadUrl);
    }
}
