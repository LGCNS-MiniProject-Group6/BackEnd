package com.miniproject1.miniproject1.commons.filter;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 모든 HTTP 요청을 한 줄씩 로그로 남기는 필터
 * 형식: IP "메서드 경로" 상태코드 처리시간 Origin
 * Security 필터보다 먼저 실행되도록 최우선 순위로 등록해 401/403 응답도 기록합니다.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 스웨거 화면을 열 때 로드되는 정적 파일은 기록하지 않음
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            String query = request.getQueryString();
            String uri = query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;
            log.info("{} \"{} {}\" {} {}ms Origin={}",
                    request.getRemoteAddr(),
                    request.getMethod(),
                    uri,
                    response.getStatus(),
                    System.currentTimeMillis() - start,
                    request.getHeader("Origin"));
        }
    }
}
