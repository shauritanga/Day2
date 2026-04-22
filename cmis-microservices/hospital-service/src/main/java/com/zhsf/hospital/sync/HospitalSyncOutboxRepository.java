package com.zhsf.hospital.sync;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HospitalSyncOutboxRepository extends JpaRepository<HospitalSyncOutbox, Long> {
    List<HospitalSyncOutbox> findTop20BySyncedAtIsNullOrderByIdAsc();
}
