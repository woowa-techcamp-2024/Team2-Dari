package com.wootecam.festivals.global.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    private static void logResponse(HttpServletRequest request, HttpServletResponse response, long elapsedTime) {
        log.info(
                "[{}] {} | Status: {} | Elapsed Time: {}ms",
                String.format("%-5s", request.getMethod()),
                String.format("%-50s", request.getRequestURI()),
                String.format("%-3d", response.getStatus()),
                String.format("%-5d", elapsedTime)
        );
        if (elapsedTime > 1000) {
            log.warn(
                    "Slow Response: [{}] {} | Status: {} | Elapsed Time: {}ms",
                    String.format("%-5s", request.getMethod()),
                    String.format("%-50s", request.getRequestURI()),
                    String.format("%-3d", response.getStatus()),
                    String.format("%-5d", elapsedTime)
            );
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        MDC.put("traceId", UUID.randomUUID().toString());
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            logResponse(request, response, System.currentTimeMillis() - start);
        }
        MDC.clear();
    }
}