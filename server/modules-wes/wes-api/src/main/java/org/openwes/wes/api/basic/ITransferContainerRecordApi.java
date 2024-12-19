package org.openwes.wes.api.basic;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.openwes.wes.api.basic.dto.TransferContainerRecordDTO;

import java.util.List;

public interface ITransferContainerRecordApi {

    TransferContainerRecordDTO findCurrentPickOrderTransferContainerRecord(@NotNull Long pickingOrderId, @NotEmpty String containerCode);

    List<TransferContainerRecordDTO> findAllBoundedRecordsByPickingOrderId(Long pickingOrderId);

    TransferContainerRecordDTO findById(Long transferContainerRecordId);

    List<TransferContainerRecordDTO> findAllById(List<Long> currentPeriodRelateRecordIds);
}
