package org.openwes.wes.config.controller;

import org.openwes.wes.api.main.data.dto.AddressDTO;
import org.openwes.wes.config.domain.repository.AddressRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("config/address")
@Validated
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class AddressController {

    private final AddressRepository addressRepository;

    @PostMapping(value = "getNextAddresses")
    public Object getNextAddresses(@RequestBody AddressDTO addressDTO) {
        return addressRepository.getNextAddresses(addressDTO);
    }
}
