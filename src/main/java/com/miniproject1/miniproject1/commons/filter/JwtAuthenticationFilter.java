package com.miniproject1.miniproject1.commons.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.commons.exception.ErrorResponse;
import com.miniproject1.miniproject1.commons.token.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || ("POST".equals(request.getMethod())
                && ("/api/auth/login".equals(path) || "/api/auth/logout".equals(path)));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (!header.startsWith("Bearer ")) {
                throw new JwtException("Invalid Authorization header");
            }

            var claims = jwtTokenProvider.parseAccessToken(header.substring(7));
            String role = claims.get("role", String.class);
            var authorities = role == null
                    ? List.<SimpleGrantedAuthority>of()
                    : List.of(new SimpleGrantedAuthority("ROLE_" + role));

            var authentication = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(), null, authorities);
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            ErrorCode errorCode = e instanceof ExpiredJwtException
                    ? ErrorCode.EXPIRED_TOKEN
                    : ErrorCode.INVALID_TOKEN;
            response.setStatus(errorCode.getStatus().value());
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(
                    response.getWriter(),
                    ErrorResponse.of(errorCode, request.getRequestURI())
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
