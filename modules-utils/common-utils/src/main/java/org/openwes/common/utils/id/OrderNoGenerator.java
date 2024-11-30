package org.openwes.common.utils.id;

import org.openwes.common.utils.utils.RedisUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@NoArgsConstructor
@ConditionalOnBean(RedisUtils.class)
public class OrderNoGenerator {

    private static final String INBOUND_PLAN_ORDER_NO_PREFIX = "IN_";
    private static final String RECEIVE_ORDER_NO_PREFIX = "RE_";
    private static final String ACCEPT_ORDER_NO_PREFIX = "APT_";
    private static final String MOVEMENT_ORDER_NO_PREFIX = "MOVE_";

    private static final String PUT_AWAY_TASK_NO_PREFIX = "PUT_AWAY_";

    private static final String EMPTY_CONTAINER_INBOUND_ORDER_NO_PREFIX = "ECI_";

    private static final String OUTBOUND_PLAN_ORDER_NO_PREFIX = "OUT_";
    private static final String OUTBOUND_WAVE_NO_PREFIX = "WAVE_";
    private static final String PICKING_ORDER_NO_PREFIX = "PICK_";

    private static final String STOCKTAKE_ORDER_NO_PREFIX = "ST_";

    private static final String STOCK_ABNORMAL_ORDER_NO_PREFIX = "STA_";

    private static final String STOCK_ADJUSTMENT_ORDER_NO_PREFIX = "SAJ_";
    private static final String STOCK_CREATE_DTO_ORDER_NO_PREFIX = "SCT_";

    private static final String CONTAINER_TASK_CODE_PREFIX = "CTC_";

    private static final Map<String, Long> INDEX_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Integer> COUNT_MAP = new ConcurrentHashMap<>();

    // Number of indexes to fetch from Redis at once
    private static final int CACHE_SIZE = 1;


    private static RedisUtils redisUtils;

    @Autowired
    public OrderNoGenerator(RedisUtils redisUtils) {
        OrderNoGenerator.redisUtils = redisUtils;
    }

    public static String generationInboundPlanOrderNo() {
        return generateOrderNo(INBOUND_PLAN_ORDER_NO_PREFIX);
    }

    public static String generationReceiveOrderNo() {
        return generateOrderNo(RECEIVE_ORDER_NO_PREFIX);
    }

    public static String generationAcceptOrderNo() {
        return generateOrderNo(ACCEPT_ORDER_NO_PREFIX);
    }
    public static String generationMovementOrderNo() {
        return generateOrderNo(MOVEMENT_ORDER_NO_PREFIX);
    }

    public static String generationPutAwayTaskNo() {
        return generateOrderNo(PUT_AWAY_TASK_NO_PREFIX);
    }

    public static String generationEmptyContainerInboundOrderNo() {
        return generateOrderNo(EMPTY_CONTAINER_INBOUND_ORDER_NO_PREFIX);
    }

    public static String generationOutboundPlanOrderNo() {
        return generateOrderNo(OUTBOUND_PLAN_ORDER_NO_PREFIX);
    }

    public static String generationOutboundWaveNo() {
        return generateOrderNo(OUTBOUND_WAVE_NO_PREFIX);
    }

    public static String generationPickingOrderNo() {
        return generateOrderNo(PICKING_ORDER_NO_PREFIX);
    }

    public static String generationStocktakeOrderNo() {
        return generateOrderNo(STOCKTAKE_ORDER_NO_PREFIX);
    }

    public static String generationStockAbnormalOrderNo() {
        return generateOrderNo(STOCK_ABNORMAL_ORDER_NO_PREFIX);
    }

    public static String generationStockAdjustmentOrderNo() {
        return generateOrderNo(STOCK_ADJUSTMENT_ORDER_NO_PREFIX);
    }

    public static String generationStockCreateDTOOrderNo() {
        return generateOrderNo(STOCK_CREATE_DTO_ORDER_NO_PREFIX);
    }

    public static String generationContainerTaskCode() {
        return generateOrderNo(CONTAINER_TASK_CODE_PREFIX);
    }

    private static final FastDateFormat formatter = FastDateFormat.getInstance("yyyy-MM-dd");

    /**
     * OrderNo = prefix + datacenterId + workId + "YYYY-MM-DD" + INDEX
     *
     * @param prefix
     *
     * @return
     */
    private static String generateOrderNo(String prefix) {
        String key = prefix + formatter.format(System.currentTimeMillis());
        return prefix + formatter.format(System.currentTimeMillis()) + "-" + generateIndex(key);
    }

    private static String generateIndex(String key) {
        // use string constant pool to lock
        synchronized (key.intern()) {
            if (!COUNT_MAP.containsKey(key) || COUNT_MAP.get(key) == 0) {
                long index = redisUtils.getAndIncrement(key, CACHE_SIZE);
                INDEX_MAP.put(key, index);
                COUNT_MAP.put(key, CACHE_SIZE);
            }

            Long index = INDEX_MAP.get(key);
            COUNT_MAP.put(key, COUNT_MAP.get(key) - 1);
            INDEX_MAP.put(key, index + 1);
            return String.format("%06d", index);
        }
    }
}
