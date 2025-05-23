package org.openwes.wes.printer.application;

import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.print.IPrintRecordApi;
import org.openwes.wes.api.print.constants.PrintStatusEnum;
import org.openwes.wes.printer.domain.entity.PrintRecord;
import org.openwes.wes.printer.domain.repository.PrintRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class PrintRecordApiImpl implements IPrintRecordApi {

    private final PrintRecordRepository printRecordRepository;

    @Override
    public void updateStatus(Long id, PrintStatusEnum status, String errorMessage) {
        PrintRecord printRecord = printRecordRepository.findById(id).orElseThrow();
        printRecord.updateStatus(status, errorMessage);
        printRecordRepository.save(printRecord);
    }
}
