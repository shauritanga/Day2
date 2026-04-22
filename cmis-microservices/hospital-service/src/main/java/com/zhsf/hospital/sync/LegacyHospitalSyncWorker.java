package com.zhsf.hospital.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Instant;

@Component
// Temporary migration worker: remove after all hospital-dependent monolith modules
// have been extracted and cmis_db.hospitals is no longer needed.
public class LegacyHospitalSyncWorker {

    private static final Logger log = LoggerFactory.getLogger(LegacyHospitalSyncWorker.class);

    private final boolean enabled;
    private final HospitalSyncOutboxRepository repository;
    private final RestClient monolithRestClient;

    public LegacyHospitalSyncWorker(@Value("${legacy-sync.enabled:true}") boolean enabled,
                                    @Value("${legacy-sync.monolith-base-url}") String monolithBaseUrl,
                                    HospitalSyncOutboxRepository repository) {
        this.enabled = enabled;
        this.repository = repository;
        this.monolithRestClient = RestClient.builder()
                .baseUrl(monolithBaseUrl)
                .defaultHeader("X-Internal-Client", "hospital-service")
                .build();
    }

    @Scheduled(fixedDelayString = "${legacy-sync.fixed-delay-ms:5000}")
    @Transactional
    public void syncPendingHospitals() {
        if (!enabled) {
            return;
        }

        for (HospitalSyncOutbox event : repository.findTop20BySyncedAtIsNullOrderByIdAsc()) {
            try {
                monolithRestClient.post()
                        .uri("/api/hospitals/internal/sync")
                        .body(LegacyHospitalSyncRequest.from(event))
                        .retrieve()
                        .toBodilessEntity();

                event.setSyncedAt(Instant.now());
                event.setLastError(null);
                log.info("Synced hospital {} to monolith legacy database", event.getHospitalCode());
            } catch (Exception ex) {
                event.setAttempts(event.getAttempts() + 1);
                event.setLastError(errorMessage(ex));
                log.warn("Failed to sync hospital {} to monolith: {}", event.getHospitalCode(), ex.getMessage());
            }
        }
    }

    private String errorMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = ex.getClass().getSimpleName();
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
