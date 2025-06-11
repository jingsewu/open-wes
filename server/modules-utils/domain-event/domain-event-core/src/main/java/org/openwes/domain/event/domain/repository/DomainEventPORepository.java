package org.openwes.domain.event.domain.repository;

import org.openwes.domain.event.constants.DomainEventStatusEnum;
import org.openwes.domain.event.domain.entity.DomainEventPO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DomainEventPORepository extends JpaRepository<DomainEventPO, Long> {

    List<DomainEventPO> findByStatusAndCreateTimeBetween(DomainEventStatusEnum status, Long startTime, Long endTime,
                                                         Pageable limit);

    void deleteAllByCreateTimeLessThanAndStatus(Long createTime, DomainEventStatusEnum status);

}
