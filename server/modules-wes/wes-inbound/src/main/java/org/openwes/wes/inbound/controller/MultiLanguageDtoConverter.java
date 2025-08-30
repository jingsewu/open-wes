package org.openwes.wes.inbound.controller;

import org.openwes.wes.api.inbound.dto.ImportInboundPlanOrderBaseDTO;
import org.openwes.wes.api.inbound.dto.ImportInboundPlanOrderEnDTO;
import org.openwes.wes.api.inbound.dto.ImportInboundPlanOrderZhDTO;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

public class MultiLanguageDtoConverter {

    public static ImportInboundPlanOrderBaseDTO convertToBaseDto(ImportInboundPlanOrderEnDTO enDto) {
        ImportInboundPlanOrderBaseDTO baseDto = new ImportInboundPlanOrderBaseDTO();
        BeanUtils.copyProperties(enDto, baseDto);
        return baseDto;
    }

    public static ImportInboundPlanOrderBaseDTO convertToBaseDto(ImportInboundPlanOrderZhDTO zhDto) {
        ImportInboundPlanOrderBaseDTO baseDto = new ImportInboundPlanOrderBaseDTO();
        BeanUtils.copyProperties(zhDto, baseDto);
        return baseDto;
    }

    public static List<ImportInboundPlanOrderBaseDTO> convertEnListToBase(
            List<ImportInboundPlanOrderEnDTO> enList) {
        return enList.stream()
                .map(MultiLanguageDtoConverter::convertToBaseDto)
                .collect(Collectors.toList());
    }

    public static List<ImportInboundPlanOrderBaseDTO> convertZhListToBase(
            List<ImportInboundPlanOrderZhDTO> zhList) {
        return zhList.stream()
                .map(MultiLanguageDtoConverter::convertToBaseDto)
                .collect(Collectors.toList());
    }
}
