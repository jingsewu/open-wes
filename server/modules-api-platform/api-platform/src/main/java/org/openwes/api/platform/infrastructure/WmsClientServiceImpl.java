package org.openwes.api.platform.infrastructure;

import com.google.common.collect.Lists;
import org.openwes.wes.api.basic.IContainerApi;
import org.openwes.wes.api.basic.dto.ContainerLocationReportDTO;
import org.openwes.wes.api.inbound.IInboundPlanOrderApi;
import org.openwes.wes.api.inbound.dto.InboundPlanOrderCancelDTO;
import org.openwes.wes.api.inbound.dto.InboundPlanOrderDTO;
import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.outbound.IOutboundPlanOrderApi;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderCancelDTO;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.openwes.wes.api.outbound.dto.TransferContainerReleaseDTO;
import org.openwes.wes.api.task.ITransferContainerApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    private final int threadCount = Runtime.getRuntime().availableProcessors() * 2 + 1;

    @Override
    public void createInboundOrder(List<InboundPlanOrderDTO> param) {
        inboundPlanOrderApi.createInboundPlanOrder(param);
    }

    @Override
    @Async("requestExecutor")
    public void asyncCreateInboundOrder(List<InboundPlanOrderDTO> inboundOrderDTOS) {
        List<InboundPlanOrderDTO> resultList = new ArrayList<>(inboundOrderDTOS.size());
        int executeThreads = threadCount;
        if (inboundOrderDTOS.size() < executeThreads) {
            executeThreads = inboundOrderDTOS.size();
        }
        // 按N个线程进行拆分
        ExecutorService executorService = Executors.newFixedThreadPool(executeThreads);
        CountDownLatch latch = new CountDownLatch(executeThreads);
        List<List<InboundPlanOrderDTO>> rsList = Lists.partition(inboundOrderDTOS, executeThreads);
        for (int i = 0; i < rsList.size(); i++) {
            int finalI = i;
            Runnable runnable = () -> {
                List<InboundPlanOrderDTO> list = rsList.get(finalI);
                inboundPlanOrderApi.createInboundPlanOrder(list);
                resultList.addAll(list);
                // 当前线程调用此方法，则计数减一
                latch.countDown();
            };
            executorService.execute(runnable);
        }

        try {
            // 阻塞当前线程，直到计数器的值为0
            boolean result = latch.await(120L, TimeUnit.SECONDS);
            if (!result) {
                log.warn("CoreClientServiceImpl#asyncCreateInboundOrder await failed");
            }
        } catch (Exception e) {
            log.error("CoreClientServiceImpl#asyncCreateInboundOrder error inboundOrderDTOS:[{}]", inboundOrderDTOS, e);
            Thread.currentThread().interrupt();
        } finally {
            executorService.shutdown();
        }
    }

    @Override
    public void createOutboundOrder(List<OutboundPlanOrderDTO> param) {
        outboundPlanOrderApi.createOutboundPlanOrder(param);
    }

    @Override
    @Async("requestExecutor")
    public void asyncCreateOutboundOrder(List<OutboundPlanOrderDTO> outboundOrderDTOS) {
        List<OutboundPlanOrderDTO> resultList = new ArrayList<>(outboundOrderDTOS.size());
        int theads = threadCount;
        if (outboundOrderDTOS.size() < 10) {
            theads = outboundOrderDTOS.size();
        }
        // 按N个线程进行拆分
        ExecutorService executorService = Executors.newFixedThreadPool(theads);
        CountDownLatch latch = new CountDownLatch(theads);
        List<List<OutboundPlanOrderDTO>> rsList = Lists.partition(outboundOrderDTOS, theads);

        for (int i = 0; i < rsList.size(); i++) {
            int finalI = i;
            Runnable runnable = () -> {
                List<OutboundPlanOrderDTO> list = rsList.get(finalI);
                outboundPlanOrderApi.createOutboundPlanOrder(list);
                resultList.addAll(list);
                // 当前线程调用此方法，则计数减一
                latch.countDown();
            };
            executorService.execute(runnable);
        }
        try {
            // 阻塞当前线程，直到计数器的值为0
            boolean result = latch.await(120L, TimeUnit.SECONDS);
            if (!result) {
                log.error("CoreClientServiceImpl#asyncCreateOutboundOrder await failed");
            }
        } catch (Exception e) {
            log.error("CoreClientServiceImpl#asyncCreateOutboundOrder error.outboundOrderDTOS:[{}]", outboundOrderDTOS, e);
            Thread.currentThread().interrupt();
        } finally {
            executorService.shutdown();
        }
    }

    @Override
    public List<String> cancelOutboundOrder(OutboundPlanOrderCancelDTO outboundPlanOrderCancelDTO) {
        return outboundPlanOrderApi.cancelOutboundPlanOrder(outboundPlanOrderCancelDTO);
    }

    /**
     * 创建商品
     *
     * @param param
     */
    @Override
    public int createOrUpdateSku(List<SkuMainDataDTO> param) {
        skuMainDataApi.createOrUpdateBatch(param);
        return 1;
    }

    /**
     * 异步创建商品
     * <p>
     * 1、异步执行 2、根据数量拆分多个子线程
     * </p>
     *
     * @param ksSkuDTOS
     */
    @Override
    @Async("requestExecutor")
    public void asyncCreateOrUpdateSku(List<SkuMainDataDTO> ksSkuDTOS) {

        if (ksSkuDTOS.size() < Runtime.getRuntime().availableProcessors() * 2 + 1) {
            skuMainDataApi.createOrUpdateBatch(ksSkuDTOS);
        } else {
            // 按10个线程进行拆分
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(10);
            List<List<SkuMainDataDTO>> rsList = Lists.partition(ksSkuDTOS, 10);
            for (int i = 0; i < rsList.size(); i++) {
                int finalI = i;
                Runnable runnable = () -> {
                    try {
                        skuMainDataApi.createOrUpdateBatch(rsList.get(finalI));
                    } catch (Exception e) {
                        log.error("CoreClientServiceImpl#asyncCreateOrUpdateSku error.currentIdex:[{}]", finalI, e);
                    } finally {
                        // 当前线程调用此方法，则计数减一
                        latch.countDown();
                    }
                };
                executorService.execute(runnable);
            }

            try {
                // 阻塞当前线程，直到计数器的值为0
                boolean result = latch.await(120L, TimeUnit.SECONDS);
                if (!result) {
                    log.error("CoreClientServiceImpl#asyncCreateOrUpdateSku await failed");
                }
            } catch (Exception e) {
                log.error("CoreClientServiceImpl#asyncCreateOrUpdateSku error.ksSkuDTOS:[{}]", ksSkuDTOS, e);
                Thread.currentThread().interrupt();
            } finally {
                executorService.shutdown();
            }
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
