package com.dungpham.asm1.infrastructure.security;

import com.dungpham.asm1.common.exception.InvalidTokenException;
import com.dungpham.asm1.service.JwtTokenService;
import com.dungpham.asm1.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final List<String> PUBLIC_URL = Arrays.asList(SecurityConfig.PUBLIC_LIST);

    private final JwtTokenService jwtTokenServices;
    private final UserDetailsServiceImpl userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = getTokenFromHeader(request);
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        boolean isPublicEndpoint = isPublicEndpoint(requestURI, method);
        if (isPublicEndpoint) {
            log.info("Skipping authentication for public URL: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        if (currentAuth != null && currentAuth.isAuthenticated()
                && !(currentAuth.getPrincipal() instanceof String)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (token != null && jwtTokenServices.validateToken(token)) {
                String email = jwtTokenServices.getEmailFromJwtToken(token);
                var userDetails = userService.loadUserByUsername(email);

                log.info("User authorities: {}", userDetails.getAuthorities());

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                WebAuthenticationDetails details = new WebAuthenticationDetailsSource().buildDetails(request);
                authenticationToken.setDetails(details);

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.info("Authentication set for user: {}", email);
            } else if(token != null) {
                log.warn("Invalid or missing token for: {}", requestURI);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            filterChain.doFilter(request, response);
        } catch (InvalidTokenException e) {
            log.error("Token validation error: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private boolean isPublicEndpoint(String requestUri, String method) {
        // Check whitelist endpoints (Swagger, etc.)
        for (String pattern : SecurityConfig.WHITE_LIST) {
            if (matchesUrl(pattern, requestUri)) {
                return true;
            }
        }

        // Check regular public endpoints
        for (String pattern : SecurityConfig.PUBLIC_LIST) {
            if (matchesUrl(pattern, requestUri)) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesUrl(String pattern, String requestUri) {
        // Convert Spring's ant pattern to regex pattern
        String regexPattern = pattern
                .replace("/**", "(/.*)?")
                .replace("/*", "(/[^/]*)?")
                .replace("{id}", "[^/]+");
        return requestUri.matches(regexPattern);
    }


    private String getTokenFromHeader(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
