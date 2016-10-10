package pw.spn.crawler.rutracker;

import org.openqa.selenium.By;

public interface WebElements {
    String BASE_URL = "http://rutracker.org";
    String TOPIC_BASE_URL = BASE_URL + "/forum/viewtopic.php?t=";
    String DOWNLOAD_BASE_URL = BASE_URL + "/forum/dl.php?t=";

    By CSS_SELECTOR_LOGGED_IN = By.cssSelector("span.logged-in-as-cap");
    By CSS_SELECTOR_LOGIN_LINK = By.cssSelector("span.a-like.bold");
    By CSS_SELECTOR_SEARCH_RESULTS = By.cssSelector("table.forumline.tablesorter > tbody > tr");
    By CSS_SELECTOR_LINK_INSIDE_COLUMN = By.cssSelector("div > a");
    By CSS_SELECTOR_PAGINATION_LINK = By.cssSelector("a.pg");

    By ID_LOGIN = By.id("top-login-uname");
    By ID_PWD = By.id("top-login-pwd");
    By ID_LOGIN_BTN = By.id("top-login-btn");
    By ID_SEARCH_BTN = By.id("search-submit");
    By ID_ADVANCED_SEARCH_LINK = By.id("tr-advanced-search");
    By ID_TITLE_SEARCH = By.id("title-search");
    By ID_SEARCH_SUBMIT_BTN = By.id("tr-submit-btn");

    By TAG_TD = By.tagName("td");
    By TAG_SPAN = By.tagName("span");
    By TAG_U = By.tagName("u");
    By TAG_B = By.tagName("b");

    String ATTR_CLASS = "class";
    String ATTR_CLASS_APPROVED_VALUE = "tor-approved";
    String ATTR_HREF = "href";
    String ATTR_TOPIC_ID = "data-topic_id";
    String ATTR_INNER_HTML = "innerHtml";

    String JS_READY_STATE_SCRIPT = "return document.readyState";
    String JS_READY_STATE_VALID_VALUE = "complete";
}
