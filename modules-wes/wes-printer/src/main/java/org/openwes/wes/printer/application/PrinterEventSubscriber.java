package org.openwes.wes.printer.application;

import com.google.common.eventbus.Subscribe;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.wes.printer.application.event.PrintEvent;
import org.openwes.wes.printer.domain.entity.PrintConfig;
import org.openwes.wes.printer.domain.entity.PrintRule;
import org.openwes.wes.printer.domain.repository.PrintConfigRepository;
import org.openwes.wes.printer.domain.repository.PrintRuleRepository;
import jakarta.validation.Valid;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PrinterEventSubscriber {

    @Autowired
    private PrintConfigRepository printConfigRepository;

    @Autowired
    private PrintRuleRepository printRuleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Subscribe
    public void onPrinterEvent(@Valid PrintEvent event) {

        List<PrintConfig> printConfigPOS = printConfigRepository.findAllByWorkStationId(event.getWorkStationId())
            .stream().filter(PrintConfig::isEnabled).filter(v -> CollectionUtils.isNotEmpty(v.getPrintConfigDetails()))
            .toList();
        if (CollectionUtils.isEmpty(printConfigPOS)) {
            return;
        }

        Set<PrintConfig.PrintConfigDetail> printConfigDetails = printConfigPOS.stream()
            .flatMap(v -> v.getPrintConfigDetails().stream()).collect(Collectors.toSet());

        Set<String> ruleCodes = printConfigDetails.stream().map(PrintConfig.PrintConfigDetail::getRuleCode).collect(Collectors.toSet());
        List<PrintRule> printRulePOS = printRuleRepository.findAllByRuleCodeIn(ruleCodes);

        if (printRulePOS.isEmpty()) {
            return;
        }


        Map<String, PrintRule> rulePOMap = printRulePOS.stream().collect(Collectors.toMap(PrintRule::getRuleCode, v -> v));
        printConfigDetails.forEach(printConfigDetail -> {
            PrintRule printRulePO = rulePOMap.get(printConfigDetail.getRuleCode());
            if (printRulePO == null) {
                return;
            }

            List<Map<String, Object>> args = event.getTargetArgs();
            if (StringUtils.isNotEmpty(printRulePO.getSqlScript()) && event.getParameter() != null) {
                Object[] objects = NamedParameterUtils.buildValueArray(printRulePO.getSqlScript(),
                    JsonUtils.string2Map(JsonUtils.obj2String(event.getParameter())));
                args.addAll(jdbcTemplate.queryForList(printRulePO.getSqlScript(), objects));
            }

            //TODO
            // call printer

        });

    }

}
