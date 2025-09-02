package org.openwes.search.controller;

import cn.zhxu.bs.MapSearcher;
import com.openwes.search.api.vo.dashboard.InboundProgress;
import com.openwes.search.api.vo.dashboard.OperatorRanking;
import com.openwes.search.api.vo.dashboard.OutboundProgress;
import com.openwes.search.api.vo.dashboard.WorkstationTask;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final MapSearcher mapSearcher;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate; // Changed to named parameter version

    private Map<String, Object> getTodayTimeRange() {
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        return Map.of(
                "todayStart", Timestamp.valueOf(todayStart).getTime(),
                "tomorrowStart", Timestamp.valueOf(tomorrowStart).getTime()
        );
    }

    // 今日出库进度
    @GetMapping("/outbound-progress")
    public Response<List<Map<String, Object>>> outboundProgress() {
        return Response.success(mapSearcher.searchList(OutboundProgress.class, getTodayTimeRange()));

    }

    // 今日入库进度
    @GetMapping("/inbound-progress")
    public Response<List<Map<String, Object>>> inboundProgress() {
        return Response.success(mapSearcher.searchList(InboundProgress.class, getTodayTimeRange()));
    }

    // 每小时出入库流量
    @GetMapping("/hourly-flow")
    public Response hourlyFlow() {
        Map<String, Object> timeParams = getTodayTimeRange();

        // 入库流量查询
        String inboundSql = "SELECT HOUR(create_time) as hour, SUM(qty_restocked) as qty "
                + "FROM w_inbound_plan_order_detail "
                + "WHERE create_time >= :todayStart AND create_time < :tomorrowStart "
                + "GROUP BY HOUR(create_time)";

        // 出库流量查询
        String outboundSql = "SELECT HOUR(create_time) as hour, SUM(qty_required) as qty "
                + "FROM w_outbound_plan_order_detail "
                + "WHERE create_time >= :todayStart AND create_time < :tomorrowStart "
                + "GROUP BY HOUR(create_time)";

        return Response.success(Map.of(
                "inbound", generateHourlyData(inboundSql, timeParams),
                "outbound", generateHourlyData(outboundSql, timeParams)));
    }

    // 员工作业排行榜
    @GetMapping("/operator-ranking")
    public Response<List<Map<String, Object>>> operatorRanking() {
        return Response.success(mapSearcher.searchList(OperatorRanking.class, getTodayTimeRange()));
    }

    // 工作站任务
    @GetMapping("/workstation-tasks")
    public Response<List<Map<String, Object>>> workstationTasks() {
        return Response.success(mapSearcher.searchList(WorkstationTask.class, getTodayTimeRange()));
    }

    private List<Integer> generateHourlyData(String sql, Map<String, Object> params) {
        List<Integer> hourlyData = new ArrayList<>(Collections.nCopies(24, 0));

        namedParameterJdbcTemplate.query(
                sql,
                new MapSqlParameterSource(params),
                (rs, rowNum) -> {
                    int hour = rs.getInt("hour");
                    int qty = rs.getInt("qty");
                    if (hour >= 0 && hour <= 23) {
                        hourlyData.set(hour, qty);
                    }
                    return null; // Result processing doesn't need to return anything
                }
        );

        return hourlyData;
    }
}
