package pw.spn.crawler.rutracker;

public interface WebElements {
    String BASE_URL = "http://rutracker.org";

    String CSS_SELECTOR_LOGGED_IN = "span.logged-in-as-cap";

    String ID_LOGIN = "top-login-uname";
    String ID_PWD = "top-login-pwd";
    String ID_LOGIN_BTN = "top-login-btn";

    String JS_READY_STATE_SCRIPT = "return document.readyState";
    String JS_READY_STATE_VALID_VALUE = "complete";
}
