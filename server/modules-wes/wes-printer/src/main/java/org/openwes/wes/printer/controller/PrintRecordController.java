package org.openwes.wes.printer.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.wes.api.print.IPrintRecordApi;
import org.openwes.wes.api.print.dto.PrintRecordStatusUpdateDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("print/record")
@RequiredArgsConstructor
@Tag(name = "Wms Module Api")
public class PrintRecordController {

    private final IPrintRecordApi printRecordApi;

    @PostMapping("/{id}/status")
    public Object updatePrintStatus(@PathVariable Long id, @RequestBody PrintRecordStatusUpdateDTO request) {
        printRecordApi.updateStatus(id, request.getStatus(), request.getErrorMessage());
        return Response.success();
    }
}
