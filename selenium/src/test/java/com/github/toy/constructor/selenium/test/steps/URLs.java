package com.github.toy.constructor.selenium.test.steps;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public enum URLs {
    BLANK(EMPTY, "about:blank"),
    GOOGLE("https://www.google.com/", "Google"),
    YOUTUBE("https://www.youtube.com/", "Youtube"),
    GITHUB("https://github.com/", "Github Inc"),
    FACEBOOK("https://www.facebook.com/", "Facebook"),
    DEEZER("https://www.deezer.com/", "Deezer Flow"),
    PAY_PAL("https://www.paypal.com/", "PayPal");

    private final String url;
    private final String title;

    URLs(String url, String title) {
        this.url = url;
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }
}
