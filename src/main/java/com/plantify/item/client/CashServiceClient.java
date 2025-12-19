package com.plantify.item.client;

import com.plantify.item.config.FeignConfig;
import com.plantify.item.domain.dto.request.CashRequest;
import com.plantify.item.domain.dto.response.CashResponse;
import com.plantify.item.global.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "cash-service",
        url = "${cash.service.url}",
        configuration = FeignConfig.class
)
public interface CashServiceClient {

    @PostMapping("/v1/cash/use")
    ApiResponse<CashResponse> buyByCash(@RequestBody CashRequest request);
}
