package org.openwes.wes.api.stocktake;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.openwes.wes.api.stocktake.dto.*;

import java.util.List;

public interface IStocktakeApi {

    void createStocktakeOrder(@Valid StocktakeOrderCreateDTO stocktakeOrderCreateDTO);

    List<String> cancelStocktakeOrder(@Valid StocktakeOrderCancelDTO stocktakeOrderCancelDTO);

    void executeStocktakeOrder(@Valid StocktakeOrderExecuteDTO stocktakeOrderExecuteDTO);

    void receiveStocktakeOrder(@Valid List<Long> stocktakeOrderIds, @NotNull Long workStationId);

    void submitStocktakeRecord(StocktakeRecordSubmitDTO submitDTO);

    void closeStocktakeTask(@Valid StocktakeTaskCloseDTO closeDTO);

    void closeStocktakeTask(Long workStationId);

    List<StocktakeRecordDTO> generateStocktakeRecords(String containerCode, String face, Long workStationId);

    List<StocktakeTaskDTO> getStocktakeTasksByWorkStationId(Long workStationId);
}
