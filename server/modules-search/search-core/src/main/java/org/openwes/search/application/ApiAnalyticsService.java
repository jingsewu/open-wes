package org.openwes.search.application;

import cn.zhxu.bs.BeanSearcher;
import cn.zhxu.bs.SearchResult;
import cn.zhxu.bs.util.MapUtils;
import com.openwes.search.api.vo.dashboard.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApiAnalyticsService {

    private final BeanSearcher mapSearcher;

    public ApiStatsDTO getApiStats() {
        Map<String, Object> params = new HashMap<>();
        long twentyFourHoursAgo = LocalDateTime.now().minusHours(24).toEpochSecond(ZoneOffset.UTC);
        params = MapUtils.builder()
                .field("create_time", twentyFourHoursAgo)
                .op("ge")
                .build();

        SearchResult<ApiStatsDTO> result = mapSearcher.search(ApiStatsDTO.class, params);
        return result.getDataList().isEmpty() ? new ApiStatsDTO() : result.getDataList().get(0);
    }

    public List<TimeSeriesDataDTO> getTimeSeriesData() {
        Map<String, Object> params = new HashMap<>();
        long twentyFourHoursAgo = LocalDateTime.now().minusHours(24).toEpochSecond(ZoneOffset.UTC);
        params = MapUtils.builder()
                .field("create_time", twentyFourHoursAgo)
                .op("ge")
                .build();

        SearchResult<TimeSeriesDataDTO> result = mapSearcher.search(TimeSeriesDataDTO.class, params);
        return result.getDataList();
    }

    public List<EndpointStatsDTO> getTopEndpoints() {
        Map<String, Object> params = new HashMap<>();
        long twentyFourHoursAgo = LocalDateTime.now().minusHours(24).toEpochSecond(ZoneOffset.UTC);
        params = MapUtils.builder()
                .field("create_time", twentyFourHoursAgo)
                .op("ge")
                .limit(0, 5)
                .build();

        SearchResult<EndpointStatsDTO> result = mapSearcher.search(EndpointStatsDTO.class, params);
        return result.getDataList();
    }

    public List<ErrorDistributionDTO> getErrorDistribution() {
        Map<String, Object> params = new HashMap<>();
        long twentyFourHoursAgo = LocalDateTime.now().minusHours(24).toEpochSecond(ZoneOffset.UTC);

        // Filter for error statuses and time range
        params = MapUtils.builder()
                .field("create_time", twentyFourHoursAgo)
                .op("ge")
                .build();

        SearchResult<ErrorDistributionDTO> result = mapSearcher.search(ErrorDistributionDTO.class, params);
        return result.getDataList();
    }

    public List<RecentApiCallDTO> getRecentApiCalls() {
        Map<String, Object> params = new HashMap<>();
        long twentyFourHoursAgo = LocalDateTime.now().minusHours(24).toEpochSecond(ZoneOffset.UTC);
        params = MapUtils.builder()
                .field("create_time", twentyFourHoursAgo)
                .op("ge")
                .limit(0, 10)
                .orderBy("create_time", "desc")
                .build();

        SearchResult<RecentApiCallDTO> result = mapSearcher.search(RecentApiCallDTO.class, params);
        return result.getDataList();
    }
}
