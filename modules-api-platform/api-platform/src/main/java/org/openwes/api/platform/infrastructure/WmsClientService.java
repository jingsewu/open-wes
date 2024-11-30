package org.openwes.api.platform.infrastructure;

import org.openwes.wes.api.basic.dto.ContainerLocationReportDTO;
import org.openwes.wes.api.inbound.dto.InboundPlanOrderCancelDTO;
import org.openwes.wes.api.inbound.dto.InboundPlanOrderDTO;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderCancelDTO;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.openwes.wes.api.outbound.dto.TransferContainerReleaseDTO;

import java.util.List;

public interface WmsClientService {


    /**
     * 创建入库单
     *
     * @param param
     */
    void createInboundOrder(List<InboundPlanOrderDTO> param);

    /**
     * 异步创建入库单
     *
     * @param param
     */
    void asyncCreateInboundOrder(List<InboundPlanOrderDTO> param);

    /**
     * 申请取消入库单
     *
     * @param param
     */
    void cancelInboundOrder(InboundPlanOrderCancelDTO param);

    /**
     * 创建出库单
     *
     * @param param
     */
    void createOutboundOrder(List<OutboundPlanOrderDTO> param);

    /**
     * 异步创建出库单
     *
     * @param param
     */
    void asyncCreateOutboundOrder(List<OutboundPlanOrderDTO> param);

    /**
     * 申请取消出库单
     *
     * @param outboundPlanOrderCancelDTO
     */
    List<String> cancelOutboundOrder(OutboundPlanOrderCancelDTO outboundPlanOrderCancelDTO);

    /**
     * 创建商品
     *
     * @param param
     * @return
     */
    int createOrUpdateSku(List<SkuMainDataDTO> param);

    /**
     * 异步创建商品
     *
     * @param param
     * @return
     */
    void asyncCreateOrUpdateSku(List<SkuMainDataDTO> param);

    void containerLocationReport(List<ContainerLocationReportDTO> reportDTOS);

    void transferContainerRelease(List<TransferContainerReleaseDTO> releaseDTOS);

//    TransferContainerReportResultDTO transferContainerReport(TransferContainerReportRequestDTO requestDTO);

//    void transferContainerRelease(List<TransferContainerReleaseDTO> releaseDTOS);
}
