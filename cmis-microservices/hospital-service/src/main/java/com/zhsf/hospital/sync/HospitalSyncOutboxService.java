package com.zhsf.hospital.sync;

import com.zhsf.hospital.domain.Hospital;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HospitalSyncOutboxService {

    private final HospitalSyncOutboxRepository repository;

    public HospitalSyncOutboxService(HospitalSyncOutboxRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void queueHospitalSync(Hospital hospital) {
        // Temporary migration sync: remove this queueing once the monolith no longer
        // needs a legacy copy of hospital data in cmis_db.
        HospitalSyncOutbox event = new HospitalSyncOutbox();
        event.setHospitalCode(hospital.getHospitalCode());
        event.setName(hospital.getName());
        event.setRegion(hospital.getRegion());
        event.setContactEmail(hospital.getContactEmail());
        event.setStatus(hospital.getStatus().name());
        repository.save(event);
    }
}
