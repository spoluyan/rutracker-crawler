package pw.spn.crawler.rutracker;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import pw.spn.crawler.rutracker.model.RutrackerLink;
import pw.spn.crawler.rutracker.model.RutrackerTopic;

public class RutrackerCrawlerTest {
    private RutrackerCrawler testSubject = new RutrackerCrawler("crawler123", "123");

    @Test
    public void search_validData_listOfRutrackerLinksReturned() {
        // given
        String query = "ubuntu";

        // when
        List<RutrackerLink> result = testSubject.search(query);

        // then
        assertThat(result, is(not(empty())));
    }

    @Test
    public void search_validDataWithTopicsIds_listOfRutrackerLinksReturned() {
        // given
        String query = "ubuntu";
        int[] topicIds = new int[] { 1379, 1570 };

        //when
        List<RutrackerLink> result = testSubject.search(query, topicIds);

        //then
        assertThat(result, is(not(empty())));
        result.forEach(link -> assertThat(Arrays.binarySearch(topicIds, link.getTopic().getId()), is(greaterThan(-1))));
    }

    @Test
    public void search_validDataWithNullTopicsIds_listOfRutrackerLinksReturned() {
        // given
        String query = "ubuntu";
        int[] topicIds = null;

        // when
        List<RutrackerLink> result = testSubject.search(query, topicIds);

        // then
        assertThat(result, is(not(empty())));
        assertThat(result.stream().mapToInt(link -> link.getTopic().getId()).distinct().count(), is(greaterThan(2L)));
    }

    @Test
    public void search_emptyQuery_emptyListReturned() {
        // given
        String query = "   ";

        // when
        List<RutrackerLink> result = testSubject.search(query);

        // then
        assertThat(result, is(empty()));
    }

    @Test
    public void search_noResults_emptyListReturned() {
        // given
        String query = "dalsdksladklfdj";

        // when
        List<RutrackerLink> result = testSubject.search(query);

        // then
        assertThat(result, is(empty()));
    }

    @Test
    public void getTopics_listOfRutrackerTopicsReturned() {
        // given

        // when
        List<RutrackerTopic> result = testSubject.getTopics();

        // then
        assertThat(result, is(not(empty())));
    }
}
