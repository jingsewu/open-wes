package org.openwes.wes.printer.infrastructure.persistence.mapper;

import org.openwes.wes.printer.infrastructure.persistence.po.PrintTemplatePO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrintTemplatePORepository extends JpaRepository<PrintTemplatePO, Long> {
}
