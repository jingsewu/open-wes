package org.openwes.wes.config.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("config/metaData")
@Tag(name = "Wms Module Api")
public class MetaDataController {

    @PostMapping("{pathname}")
    public String getByPath(@PathVariable String pathname) {
        return "";
    }
}
