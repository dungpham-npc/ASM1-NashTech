package com.dungpham.asm1.infrastructure.security;

import com.dungpham.asm1.common.exception.InvalidTokenException;
import com.dungpham.asm1.service.JwtTokenService;
import com.dungpham.asm1.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final List<PublicEndpoint> PUBLIC_ENDPOINTS = List.of(
//            new PublicEndpoint("GET", "/api/v1/products"),
//            new PublicEndpoint("GET", "/api/v1/products/featured"),
//            new PublicEndpoint("GET", "/api/v1/products/*"),
//            new PublicEndpoint("GET", "/api/v1/categories"),
//            new PublicEndpoint("GET", "/api/v1/categories/*"),
            new PublicEndpoint("POST", "/api/v1/users/login"),
            new PublicEndpoint("POST", "/api/v1/users/register"),
            new PublicEndpoint(null, "/error") // Allow all methods
    );

    private final List<String> WHITE_LIST = Arrays.asList(SecurityConfig.WHITE_LIST);

    private final JwtTokenService jwtTokenServices;
    private final UserDetailsServiceImpl userService;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = getTokenFromHeader(request);
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        if (isPublicEndpoint(method, requestURI)) {
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

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.info("Authentication set for user: {}", email);
            } else if (token != null) {
                throw new InvalidTokenException();
            }

            filterChain.doFilter(request, response);

        } catch (InvalidTokenException ex) {
            log.error("Token validation failed: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            throw ex; // Let Spring Security handle it with CustomAuthenticationEntryPoint
        }
    }

    private boolean isPublicEndpoint(String method, String uri) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(endpoint ->
                (endpoint.method() == null || endpoint.method().equalsIgnoreCase(method))
                        && pathMatcher.match(endpoint.pathPattern(), uri));
    }


    private boolean matchesAny(List<String> patterns, String requestUri) {
        return patterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }

    private String getTokenFromHeader(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
