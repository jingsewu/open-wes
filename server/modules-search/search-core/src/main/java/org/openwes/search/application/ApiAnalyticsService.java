package org.openwes.search.application;

import cn.zhxu.bs.BeanSearcher;
import cn.zhxu.bs.SearchResult;
import cn.zhxu.bs.util.MapBuilder;
import cn.zhxu.bs.util.MapUtils;
import com.openwes.search.api.vo.dashboard.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApiAnalyticsService {

    private final BeanSearcher mapSearcher;

    private Map<String, Object> getTodayTimeRange() {
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        return Map.of(
                "todayStart", Timestamp.valueOf(todayStart).getTime(),
                "tomorrowStart", Timestamp.valueOf(tomorrowStart).getTime()
        );
    }

    public ApiStatsDTO getApiStats() {
        Map<String, Object> params = getTodayTimeRange();

        SearchResult<ApiStatsDTO> result = mapSearcher.search(ApiStatsDTO.class, params);
        return result.getDataList().isEmpty() ? new ApiStatsDTO() : result.getDataList().get(0);
    }

    public List<TimeSeriesDataDTO> getTimeSeriesData() {
        Map<String, Object> params = getTodayTimeRange();

        SearchResult<TimeSeriesDataDTO> result = mapSearcher.search(TimeSeriesDataDTO.class, params);
        return result.getDataList();
    }

    public List<EndpointStatsDTO> getTopEndpoints() {
        Map<String, Object> params = getTodayTimeRange();

        SearchResult<EndpointStatsDTO> result = mapSearcher.search(EndpointStatsDTO.class, params);
        return result.getDataList();
    }

    public List<ErrorDistributionDTO> getErrorDistribution() {
        Map<String, Object> params = getTodayTimeRange();

        SearchResult<ErrorDistributionDTO> result = mapSearcher.search(ErrorDistributionDTO.class, params);
        return result.getDataList();
    }

    public List<RecentApiCallDTO> getRecentApiCalls() {
        Map<String, Object> params = MapUtils.builder()
                .page(0, 5)
                .build();
        params.putAll(getTodayTimeRange());
        SearchResult<RecentApiCallDTO> result = mapSearcher.search(RecentApiCallDTO.class, params);
        return result.getDataList();
    }
}
