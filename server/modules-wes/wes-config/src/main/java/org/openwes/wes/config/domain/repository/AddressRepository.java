package org.openwes.wes.config.domain.repository;

import org.openwes.wes.api.main.data.dto.AddressDTO;

import java.util.List;
import java.util.Map;

public interface AddressRepository {

    Map<String, List<String>> getNextAddresses(AddressDTO addressDTO);
}
