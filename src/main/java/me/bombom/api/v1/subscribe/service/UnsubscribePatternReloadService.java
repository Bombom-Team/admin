package me.bombom.api.v1.subscribe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class UnsubscribePatternReloadService {

    private final EmailServerUnsubscribePatternReloadClient emailServerUnsubscribePatternReloadClient;

    public void reloadAfterCommit() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            registerAfterCommit();
            return;
        }

        emailServerUnsubscribePatternReloadClient.reload();
    }

    private void registerAfterCommit() {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                emailServerUnsubscribePatternReloadClient.reload();
            }
        });
    }
}
