package pw.spn.crawler.rutracker.exception;

public class RutrackerCrawlerException extends RuntimeException {
    private static final long serialVersionUID = -7096726682576281338L;

    public RutrackerCrawlerException(String message) {
        super(message);
    }

    public RutrackerCrawlerException(Throwable e) {
        super(e);
    }
}
