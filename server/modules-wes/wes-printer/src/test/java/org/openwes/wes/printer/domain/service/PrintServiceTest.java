package org.openwes.wes.printer.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.mq.MqClient;
import org.openwes.wes.api.print.dto.PrintContentDTO;
import org.openwes.wes.printer.application.event.PrintEvent;
import org.openwes.wes.api.print.constants.ModuleEnum;
import org.openwes.wes.api.print.constants.PrintNodeEnum;
import org.openwes.wes.printer.domain.entity.PrintConfig;
import org.openwes.wes.printer.domain.entity.PrintRecord;
import org.openwes.wes.printer.domain.entity.PrintRule;
import org.openwes.wes.printer.domain.entity.PrintTemplate;
import org.openwes.wes.printer.domain.repository.PrintRecordRepository;
import org.openwes.wes.printer.domain.repository.PrintTemplateRepository;
import org.openwes.wes.printer.domain.service.impl.PrintServiceImpl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrintServiceTest {

    @InjectMocks
    private PrintServiceImpl printService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private MqClient mqClient;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private PrintTemplateRepository printTemplateRepository;

    @Mock
    private PrintRecordRepository printRecordRepository;

    @Test
    void testPrint_NormalCase_WithSqlAndTemplate() {
        // Arrange
        PrintRule rule = mock(PrintRule.class);
        when(rule.getRuleCode()).thenReturn("RULE1");
        when(rule.getSqlScript()).thenReturn("SELECT * FROM table WHERE id = :id");
        when(rule.getTemplateCode()).thenReturn("TEMPLATE1");

        PrintConfig.PrintConfigDetail detail = mock(PrintConfig.PrintConfigDetail.class);
        when(detail.getRuleCode()).thenReturn("RULE1");
        when(detail.getPrinter()).thenReturn("PRINTER1");

        PrintEvent event = mock(PrintEvent.class);
        when(event.getModule()).thenReturn(ModuleEnum.INBOUND);
        when(event.getPrintNode()).thenReturn(PrintNodeEnum.PRINT_NODE_CLICK_PRINT);
        when(event.getWorkStationId()).thenReturn(1L);
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("id", "123");
        when(event.getParameter()).thenReturn(parameter);

        Map<String, Object> targetArgs = new HashMap<>();
        targetArgs.put("name", "John");
        when(event.getTargetArgs()).thenReturn(targetArgs);

        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> resultRow = new HashMap<>();
        resultRow.put("age", 30);
        results.add(resultRow);

        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(results);

        PrintTemplate template = mock(PrintTemplate.class);
        when(template.getTemplateContent()).thenReturn("<p>Hello ${name}, age: ${age}</p>");
        when(template.getTemplateName()).thenReturn("TestTemplate");
        when(printTemplateRepository.findByTemplateCode("TEMPLATE1")).thenReturn(template);

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>content</html>");

        PrintRecord printRecord = new PrintRecord();
        printRecord.setId(1L);
        when(printRecordRepository.save(any(PrintRecord.class))).thenReturn(printRecord);

        // Act
        printService.print(Arrays.asList(rule), Set.of(detail), event);

        // Assert
        verify(jdbcTemplate).queryForList(anyString(), any(Object[].class));
        verify(templateEngine).process(anyString(), any(Context.class));
        verify(printRecordRepository).save(any(PrintRecord.class));
        verify(mqClient).sendMessage(eq(RedisConstants.PRINTER_TOPIC), any(PrintContentDTO.class));
    }

    @Test
    void testPrint_SqlScriptEmpty_ShouldSkipQuery() {
        // Arrange
        PrintRule rule = mock(PrintRule.class);
        when(rule.getRuleCode()).thenReturn("RULE1");
        when(rule.getSqlScript()).thenReturn(null); // No SQL script
        when(rule.getTemplateCode()).thenReturn("TEMPLATE1");

        PrintConfig.PrintConfigDetail detail = mock(PrintConfig.PrintConfigDetail.class);
        when(detail.getRuleCode()).thenReturn("RULE1");

        PrintEvent event = mock(PrintEvent.class);
        when(event.getModule()).thenReturn(ModuleEnum.INBOUND);
        when(event.getPrintNode()).thenReturn(PrintNodeEnum.PRINT_NODE_CLICK_PRINT);
        when(event.getWorkStationId()).thenReturn(1L);
        when(event.getTargetArgs()).thenReturn(new HashMap<>());

        PrintTemplate template = mock(PrintTemplate.class);
        when(template.getTemplateContent()).thenReturn("<p>Hello</p>");
        when(template.getTemplateName()).thenReturn("TestTemplate");
        when(printTemplateRepository.findByTemplateCode("TEMPLATE1")).thenReturn(template);

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>content</html>");

        PrintRecord printRecord = new PrintRecord();
        printRecord.setId(1L);
        when(printRecordRepository.save(any(PrintRecord.class))).thenReturn(printRecord);

        // Act
        printService.print(Arrays.asList(rule), Set.of(detail), event);

        // Assert
        verify(jdbcTemplate, never()).queryForList(anyString(), any(Object[].class));
        verify(templateEngine).process(anyString(), any(Context.class));
        verify(printRecordRepository).save(any(PrintRecord.class));
        verify(mqClient).sendMessage(eq(RedisConstants.PRINTER_TOPIC), any(PrintContentDTO.class));
    }

    @Test
    void testPrint_NoMatchingRuleCode_ShouldSkipProcessing() {
        // Arrange
        PrintRule rule = mock(PrintRule.class);
        when(rule.getRuleCode()).thenReturn("RULE1");

        PrintConfig.PrintConfigDetail detail = mock(PrintConfig.PrintConfigDetail.class);
        when(detail.getRuleCode()).thenReturn("UNKNOWN_RULE"); // Not in map

        PrintEvent event = mock(PrintEvent.class);

        // Act
        printService.print(Arrays.asList(rule), Set.of(detail), event);

        // Assert
        verifyNoInteractions(jdbcTemplate, templateEngine, printTemplateRepository, printRecordRepository, mqClient);
    }
}
