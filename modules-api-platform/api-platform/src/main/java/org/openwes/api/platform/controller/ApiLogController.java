package org.openwes.api.platform.controller;

import org.openwes.api.platform.domain.entity.ApiLogPO;
import org.openwes.api.platform.domain.repository.ApiLogPORepository;
import org.openwes.common.utils.http.Response;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api-log-management")
@Tag(name = "Api Platform Module Api")
public class ApiLogController {

    private final ApiLogPORepository apiLogPORepository;

    @GetMapping("/{id}")
    public Response<ApiLogPO> detail(@PathVariable Long id) {
        ApiLogPO apiLogPO = apiLogPORepository.findById(id).orElseThrow();
        return Response.success(apiLogPO);
    }
}
