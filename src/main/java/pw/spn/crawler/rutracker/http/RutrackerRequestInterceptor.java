package pw.spn.crawler.rutracker.http;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;

public class RutrackerRequestInterceptor implements Interceptor {
    private static final String USER_AGENT = "User-Agent";
    private static final String USER_AGENT_VALUE = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36";
    private static final String ACCEPT = "Accept";
    private static final String ACCEPT_VALUE = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ACCEPT_ENCODING_VALUE = "gzip,deflate,sdch";
    private static final String ACCEPT_LANGUAGE = "Accept-Language";
    private static final String ACCEPT_LANGUAGE_VALUE = "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4,ms;q=0.2";
    private static final String REFERER = "Referer";
    private static final String REFERER_VALUE = "http://rutracker.org/forum/index.php";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Builder requestBuilder = original.newBuilder();
        requestBuilder.header(USER_AGENT, USER_AGENT_VALUE);
        requestBuilder.header(ACCEPT, ACCEPT_VALUE);
        requestBuilder.header(ACCEPT_ENCODING, ACCEPT_ENCODING_VALUE);
        requestBuilder.header(ACCEPT_LANGUAGE, ACCEPT_LANGUAGE_VALUE);
        requestBuilder.header(REFERER, REFERER_VALUE);
        Request request = requestBuilder.build();
        return chain.proceed(request);
    }

}
