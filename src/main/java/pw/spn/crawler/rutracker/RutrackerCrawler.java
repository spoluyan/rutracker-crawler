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
    private static final String CLASS_APPROVED = "tor-approved";
    private static final String ATTR_HREF = "href";
    private static final String DATA_TS_TEXT= "data-ts_text";
    private static final String ATTR_ABS_HREF = "abs:href";
    private static final String CSS_SELECTOR_A_IN_DIV = "div > a";

    private static final int TD_APPROVED_INFO_INDEX = 1;
    private static final int TD_TOPIC_INFO_INDEX = 2;
    private static final int TD_NAME_INFO_INDEX = 3;
    private static final int TD_FILE_SIZE_INFO_INDEX = 5;
    private static final int TD_SEEDS_INFO_INDEX = 6;
    private static final int TD_LEECHES_INFO_INDEX = 7;

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
            rutrackerTopicsIds = new Integer[]{-1};
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
        boolean approved = tds.get(TD_APPROVED_INFO_INDEX).select(TAG_SPAN).hasClass(CLASS_APPROVED);
        if (!approved) {
            return null;
        }
        Element topicInfo = tds.get(TD_TOPIC_INFO_INDEX).select(CSS_SELECTOR_A_IN_DIV).first();
        RutrackerTopic topic = mapElementToRutrackerTopic(topicInfo);
        Element nameInfo = tds.get(TD_NAME_INFO_INDEX).select(CSS_SELECTOR_A_IN_DIV).first();
        String title = nameInfo.text();
        String url = nameInfo.attr(ATTR_ABS_HREF);
        String downloadUrl = url.replace("viewtopic", "dl");

        String sizeInBytesStr = tds.get(TD_FILE_SIZE_INFO_INDEX).attr(DATA_TS_TEXT);
        long sizeInBytes = Long.parseLong(sizeInBytesStr);

        String seedsStr = tds.get(TD_SEEDS_INFO_INDEX).attr(DATA_TS_TEXT);
        int seeds = Math.max(Integer.parseInt(seedsStr), 0);

        String leechesRowStr = tds.get(TD_LEECHES_INFO_INDEX).text();
        int leeches = Integer.parseInt(leechesRowStr);

        return new RutrackerLink(topic, title, url, downloadUrl, sizeInBytes, seeds, leeches);
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
