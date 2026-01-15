package me.bombom.api.v1.session.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class SpringSessionEntity {

    @Id
    private String primaryId;

    private String sessionId;

    private Long creationTime;

    private Long lastAccessTime;

    private Integer maxInactiveInterval;

    private Long expiryTime;

    private String principalName;
}
