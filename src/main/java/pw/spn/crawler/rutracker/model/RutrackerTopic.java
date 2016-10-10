package pw.spn.crawler.rutracker.model;

public class RutrackerTopic {
    private final int id;
    private final String name;

    public RutrackerTopic(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
