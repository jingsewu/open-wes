package org.openwes.wes.api.print.dto;

import lombok.Data;
import org.openwes.wes.api.print.constants.PrintStatusEnum;

@Data
public class PrintRecordStatusUpdateDTO {
    private PrintStatusEnum status;
    private String errorMessage;
}
