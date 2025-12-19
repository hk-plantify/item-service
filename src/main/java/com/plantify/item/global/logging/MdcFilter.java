package com.plantify.item.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcFilter extends OncePerRequestFilter {

    @Value("${spring.application.name}")
    private String serviceName;

    private static final String REQUEST_ID = "X-Request-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String requestId = request.getHeader(REQUEST_ID);
            if (requestId == null) {
                requestId = UUID.randomUUID().toString();
            }

            MDC.put("requestId", requestId);
            MDC.put("service", serviceName);

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
