package pw.spn.crawler.rutracker.model;

public class RutrackerLink {
    private final RutrackerTopic topic;
    private final String title;
    private final String url;
    private final String downloadUrl;
    private final long sizeInBytes;
    private final int seeds;
    private final int leeches;

    public RutrackerLink(RutrackerTopic topic, String title, String url, String downloadUrl, long sizeInBytes,
            int seeds,
            int leeches) {
        this.topic = topic;
        this.title = title;
        this.url = url;
        this.downloadUrl = downloadUrl;
        this.sizeInBytes = sizeInBytes;
        this.seeds = seeds;
        this.leeches = leeches;
    }

    public RutrackerTopic getTopic() {
        return topic;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public int getSeeds() {
        return seeds;
    }

    public int getLeeches() {
        return leeches;
    }
}
