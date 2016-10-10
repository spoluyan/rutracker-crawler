package pw.spn.crawler.rutracker;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import pw.spn.crawler.rutracker.model.RutrackerLink;

public class RutrackerCrawlerTest {
    private RutrackerCrawler testSubject = new RutrackerCrawler("crawler123", "123");

    @Test
    public void search_validData_listOfRutrackerLinksReturned() {
        // given
        String query = "deadpool";

        // when
        List<RutrackerLink> result = testSubject.search(query);

        // then
        assertThat(result, is(not(empty())));
    }
}
