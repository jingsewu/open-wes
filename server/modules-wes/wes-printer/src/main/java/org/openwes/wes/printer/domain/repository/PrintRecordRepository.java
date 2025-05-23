package org.openwes.wes.printer.domain.repository;

import org.openwes.wes.printer.domain.entity.PrintRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrintRecordRepository extends JpaRepository<PrintRecord, Long> {
}
