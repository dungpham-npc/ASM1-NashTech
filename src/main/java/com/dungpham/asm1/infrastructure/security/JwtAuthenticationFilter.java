package com.dungpham.asm1.infrastructure.security;

import com.dungpham.asm1.common.exception.InvalidTokenException;
import com.dungpham.asm1.service.JwtTokenService;
import com.dungpham.asm1.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final List<String> PUBLIC_URL =
            List.of(
                    "/api/v1/users/login");

    private final JwtTokenService jwtTokenServices;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String token = getTokenFromHeader(request);
        String requestURI = request.getRequestURI();

        var isPublic = PUBLIC_URL.stream().anyMatch(requestURI::contains);
        if (isPublic) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            if (jwtTokenServices.validateToken(token)) {
                String email = jwtTokenServices.getEmailFromJwtToken(token);
                var principle = userService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(principle, null, principle.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
            filterChain.doFilter(request, response);
        } catch (InvalidTokenException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private String getTokenFromHeader(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null) return headerAuth.substring(7);
        return null;
    }
}
