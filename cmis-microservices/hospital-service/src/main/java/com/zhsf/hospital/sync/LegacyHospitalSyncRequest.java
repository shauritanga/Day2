package com.zhsf.hospital.sync;

// Temporary DTO for syncing hospital data back to the monolith during migration.
// Remove after all modules are extracted to microservices.
public record LegacyHospitalSyncRequest(
        String hospitalCode,
        String name,
        String region,
        String contactEmail,
        String status
) {
    public static LegacyHospitalSyncRequest from(HospitalSyncOutbox event) {
        return new LegacyHospitalSyncRequest(
                event.getHospitalCode(),
                event.getName(),
                event.getRegion(),
                event.getContactEmail(),
                event.getStatus()
        );
    }
}
