package me.bombom.api.v1.article.util.html;

public class RegexHtmlTagCleaner implements HtmlTagCleaner {

    @Override
    public String clean(String html) {
        return html.replaceAll("<[^>]*>", "");
    }
}
