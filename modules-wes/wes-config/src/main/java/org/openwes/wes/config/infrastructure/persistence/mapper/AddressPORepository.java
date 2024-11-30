package org.openwes.wes.config.infrastructure.persistence.mapper;

import org.openwes.wes.config.infrastructure.persistence.po.AddressPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AddressPORepository extends JpaRepository<AddressPO, Long> {

    @Query("SELECT DISTINCT country FROM AddressPO")
    List<String> findDistinctCountry();

    List<AddressPO> findDistinctByCountry(String country);

    List<AddressPO> findDistinctByCountryAndProvince(String country, String province);

    List<AddressPO> findDistinctByCountryAndProvinceAndCity(String country, String province, String city);
}
