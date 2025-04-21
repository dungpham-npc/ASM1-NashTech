package com.dungpham.asm1.infrastructure.security;

import com.dungpham.asm1.common.enums.ErrorCode;
import com.dungpham.asm1.common.exception.ForbiddenException;
import com.dungpham.asm1.common.exception.UnauthorizedException;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.ExceptionResponse;
import com.dungpham.asm1.service.JwtTokenService;
import com.dungpham.asm1.service.impl.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailsServiceImpl userService;
    private final JwtTokenService jwtTokenService;

    public static final String[] WHITE_LIST = {
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-ui/index.html"
    };

    public static final String[] PUBLIC_LIST = {
            // Authentication
            "/api/v1/users/login",
            "/api/v1/users/register",

            // Public product browsing
            "/api/v1/products",
            "/api/v1/products/featured",
            "/api/v1/products/{id}",

            // Public category browsing
            "/api/v1/categories",
            "/api/v1/categories/{id}"
    };

    public static final String[] CUSTOMER_LIST = {
            // User profile management
            "/api/v1/users/current",
            "/api/v1/users/current/**",
            "/api/v1/users/logout",

            // Product interactions
            "/api/v1/products/{id}/rate"
    };

    public static final String[] ADMIN_LIST = {
            // User management
            "/api/v1/users",
            "/api/v1/users/{id}",

            // Product management
            "/api/v1/products/POST",
            "/api/v1/products/{id}/PUT",
            "/api/v1/products/{id}/DELETE",

            // Category management
            "/api/v1/categories/POST",
            "/api/v1/categories/{id}/PUT",
            "/api/v1/categories/{id}/DELETE"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter authTokenFilter() {
        return new JwtAuthenticationFilter(jwtTokenService, userService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

                            String uri = request.getRequestURI();
                            ForbiddenException ex = new ForbiddenException(uri);

                            BaseResponse<Object> errorResponse = BaseResponse.builder()
                                    .status(false)
                                    .code(ex.getErrorCodeString())
                                    .message(ex.getFormattedMessage())
                                    .data(null)
                                    .build();

                            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
                        }))
                .authorizeHttpRequests(
                        request -> request
                                .requestMatchers(WHITE_LIST).permitAll()
                                .requestMatchers(PUBLIC_LIST).permitAll()

                                // Explicitly allow access to current user endpoint for CUSTOMER
                                .requestMatchers("/api/v1/users/current").hasAnyRole("CUSTOMER", "ADMIN")
                                .requestMatchers("/api/v1/users/current/**").hasAnyRole("CUSTOMER", "ADMIN")

                                // Admin-only user management
                                .requestMatchers("/api/v1/users").hasRole("ADMIN")
                                .requestMatchers("/api/v1/users/{id}").hasRole("ADMIN")

                                // Method-specific authorization
                                .requestMatchers(HttpMethod.POST, "/api/v1/products").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/v1/products/{id}").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/products/{id}").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/v1/categories").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/v1/categories/{id}").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/{id}").hasRole("ADMIN")

                                .anyRequest().authenticated());

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("http://localhost:5173");
        corsConfig.addAllowedOrigin("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.addAllowedHeader("*");
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}

