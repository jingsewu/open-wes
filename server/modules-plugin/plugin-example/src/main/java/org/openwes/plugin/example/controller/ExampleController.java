package org.openwes.plugin.example.controller;

import org.openwes.wes.api.outbound.IOutboundPlanOrderApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("example")
public class ExampleController {

    @GetMapping("hello")
    public String hello() {
        return "Hello World!";
    }

    @Autowired
    private IOutboundPlanOrderApi outboundPlanOrderApi;

    @GetMapping("outbound")
    public Object outbound(@RequestBody List<Long> ids) {
        return outboundPlanOrderApi.getByIds(ids);
    }
}
