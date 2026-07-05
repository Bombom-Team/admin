package me.bombom.api.v1.subscribe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UnsubscribePatternReloadEventListener {

    private final EmailServerUnsubscribePatternReloadClient emailServerUnsubscribePatternReloadClient;

    @Async
    @TransactionalEventListener
    public void reload(ParseUnsubscribePatternUpdatedEvent event) {
        emailServerUnsubscribePatternReloadClient.reload();
    }
}
