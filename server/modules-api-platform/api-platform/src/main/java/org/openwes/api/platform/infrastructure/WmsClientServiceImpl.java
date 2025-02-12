package org.openwes.api.platform.infrastructure;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.openwes.wes.api.basic.IContainerApi;
import org.openwes.wes.api.basic.ITransferContainerApi;
import org.openwes.wes.api.basic.dto.ContainerLocationReportDTO;
import org.openwes.wes.api.inbound.IInboundPlanOrderApi;
import org.openwes.wes.api.inbound.dto.InboundPlanOrderCancelDTO;
import org.openwes.wes.api.inbound.dto.InboundPlanOrderDTO;
import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.outbound.IOutboundPlanOrderApi;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderCancelDTO;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.openwes.wes.api.task.dto.TransferContainerReleaseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Service
@EnableAsync
@Slf4j
public class WmsClientServiceImpl implements WmsClientService {

    @DubboReference
    private ISkuMainDataApi skuMainDataApi;
    @DubboReference
    private IInboundPlanOrderApi inboundPlanOrderApi;
    @DubboReference
    private IOutboundPlanOrderApi outboundPlanOrderApi;
    @DubboReference
    private IContainerApi containerApi;
    @DubboReference
    private ITransferContainerApi transferContainerApi;

    @Autowired
    private Executor requestExecutor;

    @Override
    public void createInboundOrder(List<InboundPlanOrderDTO> param) {
        inboundPlanOrderApi.createInboundPlanOrder(param);
    }

    @Override
    public void asyncCreateInboundOrder(List<InboundPlanOrderDTO> inboundOrderDTOS) {
        int executeThreads = Math.max(1, inboundOrderDTOS.size() / 200);

        List<List<InboundPlanOrderDTO>> partitions = Lists.partition(inboundOrderDTOS, executeThreads);

        List<CompletableFuture<Void>> futures = partitions.stream()
                .map(partition ->
                        CompletableFuture.runAsync(() -> inboundPlanOrderApi.createInboundPlanOrder(partition), requestExecutor)
                ).toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            log.error("async create inbound order error inboundOrderDTOS: {}", inboundOrderDTOS, e.getCause());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("async create inbound order error inboundOrderDTOS: {}", inboundOrderDTOS, e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void createOutboundOrder(List<OutboundPlanOrderDTO> param) {
        outboundPlanOrderApi.createOutboundPlanOrder(param);
    }

    @Override
    public void asyncCreateOutboundOrder(List<OutboundPlanOrderDTO> outboundOrderDTOS) {
        int threads = Math.max(1, outboundOrderDTOS.size() / 100);

        List<List<OutboundPlanOrderDTO>> partitions = Lists.partition(outboundOrderDTOS, threads);

        List<CompletableFuture<Void>> futures = partitions.stream()
                .map(partition ->
                        CompletableFuture.runAsync(() -> outboundPlanOrderApi.createOutboundPlanOrder(partition), requestExecutor)
                ).toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            log.error("async create outbound order error. outboundOrderDTOS: {}", outboundOrderDTOS, e.getCause());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("async create outbound order error. outboundOrderDTOS: {}", outboundOrderDTOS, e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public List<String> cancelOutboundOrder(OutboundPlanOrderCancelDTO outboundPlanOrderCancelDTO) {
        return outboundPlanOrderApi.cancelOutboundPlanOrder(outboundPlanOrderCancelDTO);
    }

    @Override
    public int createOrUpdateSku(List<SkuMainDataDTO> param) {
        skuMainDataApi.createOrUpdateBatch(param);
        return param.size();
    }

    @Override
    public void asyncCreateOrUpdateSku(List<SkuMainDataDTO> ksSkuDTOS) {

        int numThreads = Math.max(1, ksSkuDTOS.size() / 300);

        List<List<SkuMainDataDTO>> partitions = Lists.partition(ksSkuDTOS, numThreads);

        List<CompletableFuture<Void>> futures = partitions.stream()
                .map(partition ->
                        CompletableFuture.runAsync(() -> skuMainDataApi.createOrUpdateBatch(partition), requestExecutor)
                ).toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            log.error("async create or update SKU error. ksSkuDTOS: {}", ksSkuDTOS, e.getCause());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("async create or update SKU error. ksSkuDTOS: {}", ksSkuDTOS, e);
            Thread.currentThread().interrupt();
        }
    }


    @Override
    public void cancelInboundOrder(InboundPlanOrderCancelDTO param) {
        inboundPlanOrderApi.cancel(param.getIdentifyNos(), param.getWarehouseCode());
    }

    @Override
    public void containerLocationReport(List<ContainerLocationReportDTO> reportDTOS) {
        containerApi.updateContainerLocation(reportDTOS);
    }

    @Override
    public void transferContainerRelease(List<TransferContainerReleaseDTO> releaseDTOS) {
        transferContainerApi.transferContainerRelease(releaseDTOS);
    }
}
