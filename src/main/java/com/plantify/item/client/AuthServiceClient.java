package com.plantify.item.client;

import com.plantify.item.config.FeignConfig;
import com.plantify.item.domain.dto.response.AuthUserResponse;
import com.plantify.item.global.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "auth-service",
        url = "${auth.service.url}",
        configuration = FeignConfig.class
)
public interface AuthServiceClient {

    @PostMapping("/v1/auth/validate-token")
    ApiResponse<AuthUserResponse> getUserInfo(@RequestHeader("Authorization") String authorizationHeader);
}
