package me.bombom.api.v1.subscribe.dto.request;

public enum UnsubscribePatternType {

    AUTO_UNSUBSCRIBE,
    PARSE;

    public boolean isParse() {
        return this == PARSE;
    }
}
