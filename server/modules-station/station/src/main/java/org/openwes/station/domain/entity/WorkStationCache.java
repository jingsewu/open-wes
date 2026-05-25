package org.openwes.station.domain.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.api.model.*;
import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.station.application.business.handler.event.OnlineEvent;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.constants.WorkStationStatusEnum;
import org.openwes.wes.api.basic.dto.WorkStationConfigDTO;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A unified cache model structured around UI areas. Subclasses retain mode-specific
 * behavior but add no state fields. The cache serializes directly to Redis and serves
 * as the GET /api response.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OutboundWorkStationCache.class, name = "outbound"),
        @JsonSubTypes.Type(value = InboundWorkStationCache.class, name = "inbound"),
        @JsonSubTypes.Type(value = StocktakeWorkStationCache.class, name = "stocktake")
})
@RedisHash("WorkStation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class WorkStationCache {

    @Id
    protected Long id;

    protected String warehouseCode;
    protected Long warehouseAreaId;
    protected String stationCode;

    protected WorkStationModeEnum workStationMode;
    protected WorkStationStatusEnum workStationStatus;

    // Area objects — the cache IS the view
    protected WorkLocationArea workLocationArea;
    protected SkuArea skuArea;
    protected PutWallArea putWallArea;
    protected OrderArea orderArea;
    protected Toolbar toolbar;

    protected WorkStationConfigDTO workStationConfig;
    protected WorkStationVO.ChooseAreaEnum chooseArea;
    protected List<Tip> tips;

    protected ApiCodeEnum eventCode;

    protected boolean hasOrder;

    // Inbound-mode fields (on base to avoid casting in common handlers)
    protected List<String> callContainers;
    protected List<InboundWorkStationCache.ContainerTaskCache> containerTasks;

    public void online(WorkStationDTO dto, OnlineEvent event) {
        this.workStationMode = dto.getWorkStationMode();
        this.workStationStatus = WorkStationStatusEnum.ONLINE;
        this.hasOrder = event.isHasOrder();
        this.workStationConfig = dto.getWorkStationConfig();
        this.workLocationArea = buildWorkLocationArea(dto.getWorkLocations());
        if (workStationMode == WorkStationModeEnum.PICKING
                || workStationMode == WorkStationModeEnum.SELECTION) {
            this.putWallArea = buildPutWallArea(dto);
        }
        this.skuArea = new SkuArea();
        this.toolbar = new Toolbar();
        this.tips = new ArrayList<>();
        this.orderArea = new OrderArea();
    }

    private WorkLocationArea buildWorkLocationArea(List<WorkStationDTO.WorkLocation<? extends WorkStationDTO.WorkLocationSlot>> workLocations) {
        if (workLocations == null) return new WorkLocationArea();
        List<WorkLocationArea.WorkLocationView> views = workLocations.stream().map(wl -> {
            List<WorkLocationArea.WorkLocationSlot> slots = wl.getWorkLocationSlots() == null
                    ? new ArrayList<>()
                    : wl.getWorkLocationSlots().stream().map(s -> {
                WorkLocationArea.WorkLocationSlot slot = new WorkLocationArea.WorkLocationSlot();
                slot.setSlotCode(s.getSlotCode());
                slot.setWorkLocationCode(s.getWorkLocationCode());
                slot.setGroupCode(s.getGroupCode());
                slot.setLevel(s.getLevel() == null ? 0 : s.getLevel());
                slot.setBay(s.getBay() == null ? 0 : s.getBay());
                slot.setEnable(s.isEnable());
                return slot;
            }).collect(Collectors.toList());

            WorkLocationArea.WorkLocationView view = new WorkLocationArea.WorkLocationView();
            view.setWorkLocationCode(wl.getWorkLocationCode());
            view.setWorkLocationType(wl.getWorkLocationType() == null ? null : wl.getWorkLocationType().name());
            view.setEnable(wl.isEnable());
            view.setStationCode(wl.getStationCode());
            view.setWorkLocationSlots(slots);
            return view;
        }).collect(Collectors.toList());

        WorkLocationArea area = new WorkLocationArea();
        area.setWorkLocationViews(views);
        return area;
    }

    private PutWallArea buildPutWallArea(WorkStationDTO dto) {
        PutWallArea area = new PutWallArea();
        area.setPutWallViews(dto.getPutWalls());
        return area;
    }

    public WorkStationConfigDTO getWorkStationConfig() {
        return workStationConfig == null ? new WorkStationConfigDTO() : workStationConfig;
    }

    public void chooseArea(WorkStationVO.ChooseAreaEnum chooseArea) {
        log.info("work station: {} code: {} choose area: {}", this.id, this.stationCode, chooseArea);
        this.chooseArea = chooseArea;
    }

    public void updateConfiguration(WorkStationConfigDTO workStationConfigDTO) {
        log.info("work station: {} code: {} update configuration: {}", this.id, this.stationCode, workStationConfigDTO);
        this.workStationConfig = workStationConfigDTO;
    }

    public void addTip(Tip tip) {
        log.info("work station: {} code: {} add tip: {}", this.id, this.stationCode, tip);
        if (this.tips == null) {
            this.tips = Lists.newArrayList();
        }
        // avoid repeat confirm tip
        this.tips.removeIf(exitsTip
                -> Tip.TipShowTypeEnum.CONFIRM.getValue().equals(exitsTip.getType()));
        tips.add(tip);
    }

    public void closeTip(String tipCode) {
        log.info("work station: {} code: {} close tip: {}", this.id, this.stationCode, tipCode);
        if (this.tips == null) {
            return;
        }
        if (tipCode == null) {
            this.tips.clear();
            return;
        }
        this.tips.removeIf(tip -> tip.getTipCode().equals(tipCode));
    }

    public void scanBarcode(String barcode) {
        log.info("work station: {} code: {} scan barcode: {}", this.id, this.stationCode, barcode);
        if (this.skuArea == null) {
            this.skuArea = new SkuArea();
        }
        this.skuArea.setScanCode(barcode);
    }

    // Protected hooks for subclass override
    protected void recalculateChooseArea() {
        // no-op in base — subclasses override
    }

    protected void recalculateToolbar() {
        // no-op in base — subclasses override
    }

    protected void recalculateProcessingStatus() {
        // no-op in base — subclasses override
    }
}
