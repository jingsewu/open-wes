package org.openwes.wes.printer.domain.repository;

import org.openwes.wes.printer.domain.entity.PrintTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrintTemplateRepository extends JpaRepository<PrintTemplate, Long> {
    PrintTemplate findByTemplateCode(String templateCode);
}
