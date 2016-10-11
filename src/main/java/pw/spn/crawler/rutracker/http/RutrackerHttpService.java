package pw.spn.crawler.rutracker.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pw.spn.crawler.rutracker.exception.RutrackerCrawlerException;

public class RutrackerHttpService {
    private static final String BASE_URL = "http://rutracker.org/forum/";
    private static final String INDEX_URL = BASE_URL + "index.php";
    private static final String LOGIN_URL = BASE_URL + "login.php";
    private static final String SEARCH_URL = BASE_URL + "tracker.php";
    private static final String ENCODING = "cp1251";
    private static final String SET_COOKIE = "Set-Cookie";
    private static final String COOKIE = "Cookie";
    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String GZIP = "gzip";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_FORM_VALUE = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE_TORRENT_VALUE = "application/x-bittorrent";
    private static final String CSS_SELECTOR_TOPICS = "table.forums a[href^=\"viewforum.php?f=\"]";
    private static final String CSS_SELECTOR_SEARCH_RESULTS = "table.forumline.tablesorter > tbody > tr";

    private static final Logger logger = LoggerFactory.getLogger(RutrackerHttpService.class);

    private final OkHttpClient httpClient;
    private String cookies;

    public RutrackerHttpService() {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.addInterceptor(new RutrackerRequestInterceptor());
        okHttpClientBuilder.connectTimeout(2, TimeUnit.SECONDS).writeTimeout(2, TimeUnit.SECONDS).readTimeout(2,
                TimeUnit.SECONDS);
        okHttpClientBuilder.followRedirects(false);
        httpClient = okHttpClientBuilder.build();
    }

    public void login(String username, String password) {
        String query = "login_username=" + username + "&login_password=" + password + "&login=%C2%F5%EE%E4";
        try {
            RequestBody requestBody = RequestBody.create(MediaType.parse(CONTENT_TYPE_FORM_VALUE),
                    query.getBytes(ENCODING));
            Response response = httpClient.newCall(new Request.Builder().url(LOGIN_URL).post(requestBody).build())
                    .execute();
            if (response.isRedirect()) {
                logger.info("Login succeded.");
                List<String> cookies = response.headers(SET_COOKIE);
                if (cookies == null || cookies.isEmpty()) {
                    throw new RutrackerCrawlerException("Unable to login.");
                }
                this.cookies = cookies.stream().collect(Collectors.joining(","));
            }
        } catch (IOException e) {
            throw new RutrackerCrawlerException(e);
        }
    }

    public Elements loadTopics() {
        InputStream html = null;
        try {
            Response response = httpClient.newCall(new Request.Builder().url(INDEX_URL).get().build()).execute();
            html = getInputStream(response);
            return Jsoup.parse(html, ENCODING, BASE_URL).select(CSS_SELECTOR_TOPICS);
        } catch (IOException e) {
            logger.error("Unable to load topics.", e);
        } finally {
            closeInputStream(html);
        }
        return new Elements();
    }

    public Elements search(String query, Integer[] topicIds) {
        String queryParams = "f=" + Arrays.stream(topicIds).map(String::valueOf).collect(Collectors.joining(","))
                + "&nm=" + query;
        try {
            RequestBody requestBody = RequestBody.create(MediaType.parse(CONTENT_TYPE_FORM_VALUE),
                    queryParams.getBytes(ENCODING));
            Response response = httpClient
                    .newCall(new Request.Builder().url(SEARCH_URL).header(COOKIE, cookies).post(requestBody).build())
                    .execute();
            if (response.isSuccessful()) {
                return extractSearchResultElements(response);
            }
            logger.error("Unable to make search. Response code is {}", response.code());
        } catch (IOException e) {
            logger.error("Unable to make search.", e);
        }
        return new Elements();
    }

    private Elements extractSearchResultElements(Response response) {
        InputStream html = null;
        try {
            html = getInputStream(response);
            return Jsoup.parse(html, ENCODING, BASE_URL).select(CSS_SELECTOR_SEARCH_RESULTS);
        } catch (IOException e) {
            logger.error("Unable to parse search results.");
        } finally {
            closeInputStream(html);
        }
        return null;
    }

    public byte[] downloadFile(String url) {
        try {
            Response response = httpClient.newCall(new Request.Builder().url(url).header(COOKIE, cookies).get().build())
                    .execute();
            if (response.isSuccessful() && response.header(CONTENT_TYPE) != null
                    && response.header(CONTENT_TYPE).contains(CONTENT_TYPE_TORRENT_VALUE)) {
                return response.body().bytes();
            }
            logger.error("Invalid response from url {}.", url);
        } catch (IOException e) {
            logger.error("Unable to download file.", e);
        }
        return null;
    }

    private InputStream getInputStream(Response response) throws IOException {
        InputStream is = response.body().byteStream();
        if (GZIP.equals(response.header(CONTENT_ENCODING))) {
            return new GZIPInputStream(is);
        }
        return is;
    }

    private void closeInputStream(InputStream in) {
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
        }
    }
}
