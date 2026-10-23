package org.openwes.script.plugin.core.domain.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ExecutionStatsDTO;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginExecutionLog;
import org.openwes.script.plugin.core.domain.repository.ExecutionTypeStatsView;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginExecutionLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScriptPluginExecutionQueryServiceTest {

    @Mock
    private ScriptPluginExecutionLogRepository repository;
    @Mock
    private PluginWorkbenchAccessService accessService;

    private ScriptPluginExecutionQueryService service;

    @BeforeEach
    void setUp() {
        service = new ScriptPluginExecutionQueryService(repository, accessService);
    }

    private ScriptPluginExecutionLog log() {
        ScriptPluginExecutionLog row = new ScriptPluginExecutionLog();
        row.setId(7L);
        row.setPluginKey("packing-wave");
        row.setRevisionId(11L);
        row.setExtensionType(ExtensionType.CRON);
        row.setSuccess(true);
        row.setDurationMs(5);
        row.setCreatedAt(1000L);
        return row;
    }

    private ExecutionTypeStatsView view(ExtensionType type, long total, long failed,
                                        Double avg, Long max) {
        ExecutionTypeStatsView view = mock(ExecutionTypeStatsView.class);
        when(view.getExtensionType()).thenReturn(type);
        when(view.getTotal()).thenReturn(total);
        when(view.getFailed()).thenReturn(failed);
        when(view.getAvgDurationMs()).thenReturn(avg);
        when(view.getMaxDurationMs()).thenReturn(max);
        return view;
    }

    @Test
    void page_clampsPageAndSizeAndMapsTheId() {
        Page<ScriptPluginExecutionLog> page =
                new PageImpl<>(List.of(log()), PageRequest.of(0, 100), 1);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResultDTO<ExecutionLogDTO> result = service.page("packing-wave", 0, 500, null, null);

        assertEquals(1, result.getPage());
        assertEquals(100, result.getPageSize());
        assertEquals(1, result.getTotal());
        assertEquals(7L, result.getItems().get(0).getId());
        verify(accessService).requireDeveloper();
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(100, captor.getValue().getPageSize());
    }

    @SuppressWarnings("unchecked")
    @Test
    void page_appliesSuccessAndExtensionTypeFilters() {
        Page<ScriptPluginExecutionLog> page =
                new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        service.page("packing-wave", 1, 20, true, ExtensionType.CRON);

        ArgumentCaptor<Specification<ScriptPluginExecutionLog>> captor =
                ArgumentCaptor.forClass(Specification.class);
        verify(repository).findAll(captor.capture(), any(Pageable.class));

        Root<ScriptPluginExecutionLog> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> pluginKeyPath = mock(Path.class);
        Path<Object> successPath = mock(Path.class);
        Path<Object> typePath = mock(Path.class);
        Predicate pluginKeyPredicate = mock(Predicate.class);
        Predicate successPredicate = mock(Predicate.class);
        Predicate typePredicate = mock(Predicate.class);
        when(root.get("pluginKey")).thenReturn((Path) pluginKeyPath);
        when(root.get("success")).thenReturn((Path) successPath);
        when(root.get("extensionType")).thenReturn((Path) typePath);
        when(cb.equal(pluginKeyPath, "packing-wave")).thenReturn(pluginKeyPredicate);
        when(cb.equal(successPath, true)).thenReturn(successPredicate);
        when(cb.equal(typePath, ExtensionType.CRON)).thenReturn(typePredicate);
        when(cb.and(any(Predicate.class), any(Predicate.class), any(Predicate.class)))
                .thenReturn(pluginKeyPredicate);

        captor.getValue().toPredicate(root, query, cb);

        verify(cb).equal(successPath, true);
        verify(cb).equal(typePath, ExtensionType.CRON);
    }

    @Test
    void stats_aggregatesTotalsFailureRateWeightedAverageAndMax() {
        ExecutionTypeStatsView wave =
                view(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION, 3, 1, 10.0, 100L);
        ExecutionTypeStatsView cron = view(ExtensionType.CRON, 1, 0, 40.0, 40L);
        when(repository.statsByType(eq("packing-wave"), anyLong()))
                .thenReturn(List.of(wave, cron));

        ExecutionStatsDTO stats = service.stats("packing-wave", 24);

        assertEquals(4, stats.getTotal());
        assertEquals(1, stats.getFailed());
        assertEquals(25.0, stats.getFailureRate());
        assertEquals(18, stats.getAvgDurationMs());
        assertEquals(100, stats.getMaxDurationMs());
        assertEquals(2, stats.getByType().size());
        assertEquals(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION,
                stats.getByType().get(0).getExtensionType());
        assertEquals(1, stats.getByType().get(0).getFailed());
        verify(accessService).requireDeveloper();
    }

    @Test
    void stats_emptyWindow_returnsZeros() {
        when(repository.statsByType(eq("packing-wave"), anyLong())).thenReturn(List.of());

        ExecutionStatsDTO stats = service.stats("packing-wave", 24);

        assertEquals(0, stats.getTotal());
        assertEquals(0, stats.getFailed());
        assertEquals(0.0, stats.getFailureRate());
        assertEquals(0, stats.getAvgDurationMs());
        assertEquals(0, stats.getMaxDurationMs());
        assertEquals(List.of(), stats.getByType());
    }

    @Test
    void normalizeHours_clampsToTheSupportedWindow() {
        assertEquals(1, ScriptPluginExecutionQueryService.normalizeHours(0));
        assertEquals(1, ScriptPluginExecutionQueryService.normalizeHours(-5));
        assertEquals(720, ScriptPluginExecutionQueryService.normalizeHours(10_000));
        assertEquals(24, ScriptPluginExecutionQueryService.normalizeHours(24));
    }
}
