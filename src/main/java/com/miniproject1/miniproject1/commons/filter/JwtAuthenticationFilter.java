package com.miniproject1.miniproject1.commons.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT 기반의 인증을 처리하는 필터
 * 모든 HTTP 요청마다 실행되며, Header의 JWT 토큰을 검증하고 SecurityContext에 인증 정보를 등록합니다.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;
    private Key key;

    // JWT 인증을 거치지 않을 화이트리스트 URL 목록
    // 코딩하면서 수정 진행
    private static final List<String> WHITE_LIST = List.of(
            "/api/auth/**",
            "/token",
            "/test/**",
            "/swagger-ui/**",
            "/v3/api-docs/**");

    private final AntPathMatcher matcher = new AntPathMatcher();

    /**
     * 요청 경로가 화이트리스트에 포함되는지 확인합니다.
     */
    public boolean isPath(String path) {
        return WHITE_LIST.stream()
                .anyMatch(pattern -> matcher.match(pattern, path));
    }

    /**
     * 의존성 주입 완료 후 비밀키(Secret Key)를 기반으로 Key 객체를 생성합니다.
     */
    @PostConstruct
    private void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String endPoint = request.getRequestURI();

        // 1. CORS Preflight 요청(OPTIONS)은 토큰 검증 없이 진행
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 화이트리스트에 정의된 경로는 토큰 검증 없이 통과
        if (isPath(endPoint)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 공개 API는 토큰 없이도 통과할 수 있도록 헤더가 없으면 다음 필터로 전달
        String header = request.getHeader("Authorization");
        if (header == null || header.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 4. 'Bearer ' 이후의 순수 토큰 문자열 추출
        String token = header.substring(7);

        try {
            // 5. 토큰 파싱 및 서명 검증 (유효하지 않거나 만료된 경우 Exception 발생)
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 6. Claims에서 정보(사용자 이메일 및 권한) 추출
            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            // 7. Security Context에 저장할 Authentication 객체 생성
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    role != null ? List.of(() -> "ROLE_" + role) : List.of());

            // 8. 요청의 세부 정보(IP, Session ID 등)를 인증 객체에 설정
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 9. SecurityContextHolder에 인증 객체 등록
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        } catch (Exception e) {
            // 토큰 검증 실패 시 401 Unauthorized 반환
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 10. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}
