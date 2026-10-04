package me.bombom.api.v1.newsletterrequest.collector;

/**
 * 한 건의 수집이 실패했음을 알린다. 메시지는 초안의 failure_reason에 그대로 남는다.
 */
public class CollectFailedException extends RuntimeException {

    public CollectFailedException(String message) {
        super(message);
    }

    public CollectFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
