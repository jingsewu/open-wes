package org.openwes.domain.event.controller;

import com.google.common.eventbus.EventBus;
import org.openwes.domain.event.domain.entity.DomainEventPO;
import org.openwes.domain.event.domain.repository.DomainEventPORepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("domain/event")
@RestController
@RequiredArgsConstructor
@Tag(name = "Domain Event Module Api")
public class DomainEventController {

    private final EventBus asyncEventBus;
    private final DomainEventPORepository domainEventPORepository;

    @PostMapping("resend")
    public void resendEvent(Long domainEventId) {
        DomainEventPO domainEventPO = domainEventPORepository.findById(domainEventId).orElseThrow();
        asyncEventBus.post(domainEventPO.getEvent());
    }

    @PostMapping("send")
    public void sendEvent(@RequestBody String eventBody) {
        asyncEventBus.post(eventBody);
    }
}
