package me.bombom.api.v1.newsletterrequest.domain;

public enum NewsletterRequestStatus {

    RECEIVED,   // 접수됨
    REVIEWING,  // 초안 준비됨, 운영진 확인 중
    APPROVED,   // 등록됨
    REJECTED;   // 반려됨

    public boolean isInProgress() {
        return this == RECEIVED || this == REVIEWING;
    }

    public boolean isClosed() {
        return this == APPROVED || this == REJECTED;
    }
}
