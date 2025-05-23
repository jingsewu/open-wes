package org.openwes.wes.api.print;

import org.openwes.wes.api.print.constants.PrintStatusEnum;

public interface IPrintRecordApi {
    void updateStatus(Long id, PrintStatusEnum status, String errorMessage);
}
