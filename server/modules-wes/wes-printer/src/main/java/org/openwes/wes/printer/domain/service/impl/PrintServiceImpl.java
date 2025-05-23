package org.openwes.wes.printer.domain.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.mq.MqClient;
import org.openwes.wes.api.print.dto.PrintContentDTO;
import org.openwes.wes.printer.application.event.PrintEvent;
import org.openwes.wes.printer.domain.entity.PrintConfig;
import org.openwes.wes.printer.domain.entity.PrintRecord;
import org.openwes.wes.printer.domain.entity.PrintRule;
import org.openwes.wes.printer.domain.entity.PrintTemplate;
import org.openwes.wes.printer.domain.repository.PrintRecordRepository;
import org.openwes.wes.printer.domain.repository.PrintTemplateRepository;
import org.openwes.wes.printer.domain.service.PrintService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PrintServiceImpl implements PrintService {

    private final JdbcTemplate jdbcTemplate;
    private final MqClient mqClient;
    private final TemplateEngine templateEngine;
    private final PrintTemplateRepository printTemplateRepository;
    private final PrintRecordRepository printRecordRepository;

    @Override
    public void print(List<PrintRule> printRulePOS, Set<PrintConfig.PrintConfigDetail> printConfigDetails, @Valid PrintEvent event) {
        Map<String, PrintRule> rulePOMap = printRulePOS.stream().collect(Collectors.toMap(PrintRule::getRuleCode, v -> v));
        printConfigDetails.forEach(printConfigDetail -> {
            PrintRule printRulePO = rulePOMap.get(printConfigDetail.getRuleCode());
            if (printRulePO == null) {
                return;
            }

            Map<String, Object> args = event.getTargetArgs();
            if (StringUtils.isNotEmpty(printRulePO.getSqlScript()) && event.getParameter() != null) {
                Object[] objects = NamedParameterUtils.buildValueArray(printRulePO.getSqlScript(),
                        JsonUtils.string2Map(JsonUtils.obj2String(event.getParameter())));
                List<Map<String, Object>> results = jdbcTemplate.queryForList(printRulePO.getSqlScript(), objects);

                if (CollectionUtils.isNotEmpty(results)) {
                    args.putAll(results.get(0));
                }
            }

            PrintTemplate printTemplate = printTemplateRepository.findByTemplateCode(printRulePO.getTemplateCode());

            Context ctx = new Context();
            ctx.setVariables(args);
            String html = templateEngine.process(printTemplate.getTemplateContent(), ctx);

            //create print record
            PrintRecord saved = printRecordRepository.save(PrintRecord.create(event.getModule(), event.getPrintNode(),
                    printTemplate.getTemplateCode(), printTemplate.getTemplateName(),
                    event.getWorkStationId(),
                    html, printConfigDetail.getPrinter()));

            //send print message
            mqClient.sendMessage(RedisConstants.PRINTER_TOPIC, new PrintContentDTO()
                    .setPrintRecordId(saved.getId())
                    .setHtml(html)
                    .setWorkStationId(event.getWorkStationId())
                    .setPrinter(printConfigDetail.getPrinter()));
        });
    }
}
