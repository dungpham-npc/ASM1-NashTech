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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private final UserDetailsServiceImpl userService;
    private final JwtTokenService jwtTokenService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    public static final String[] WHITE_LIST = {
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-ui/index.html"
    };

    public static final String[] PUBLIC_LIST = {
            // Authentication
            "/api/v1/users/login",
            "/api/v1/users/register",
    };

    public static final String[] CUSTOMER_LIST = {
            "/api/v1/users/current",
            "/api/v1/users/current/**",
            "/api/v1/users/logout",
            "/api/v1/products/{id}/rate"
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
        log.info("Configured DaoAuthenticationProvider with UserDetailsService and BCryptPasswordEncoder");
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        log.info("Configuring AuthenticationManager");
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter authTokenFilter() {
        log.info("Creating JwtAuthenticationFilter");
        return new JwtAuthenticationFilter(jwtTokenService, userService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring SecurityFilterChain");

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {
                    log.info("Configuring CORS with source: {}", corsConfigurationSource());
                    cors.configurationSource(corsConfigurationSource());
                })
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> {
                    log.info("Setting session management to STATELESS");
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .exceptionHandling(exceptions -> {
                    log.info("Configuring exception handling with CustomAuthenticationEntryPoint and AccessDeniedHandler");
                    exceptions
                            .authenticationEntryPoint(authenticationEntryPoint)
                            .accessDeniedHandler((request, response, accessDeniedException) -> {
                                log.warn("Access denied for URI: {}", request.getRequestURI());
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
                            });
                })
                .authorizeHttpRequests(requests -> {
                    log.info("Configuring authorization rules");
                    requests
                            .requestMatchers(WHITE_LIST).permitAll()
                            .requestMatchers(PUBLIC_LIST).permitAll()
                            .requestMatchers(HttpMethod.GET, "/error").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/products").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/products/featured").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/products").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.GET, "/api/v1/categories").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/categories").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.POST, "/api/v1/users").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.GET, "/api/v1/users/current").hasAnyRole("CUSTOMER", "ADMIN")
                            .requestMatchers(HttpMethod.PUT,"/api/v1/users/current/**").hasAnyRole("CUSTOMER", "ADMIN")
                            .requestMatchers(HttpMethod.POST, "/api/v1/users/logout").hasAnyRole("CUSTOMER", "ADMIN")
                            .requestMatchers(HttpMethod.POST, "/api/v1/products/{id}/rate").hasRole("CUSTOMER")
                            .anyRequest().authenticated();

                    // Log each authorization rule
                    log.info("Authorization rule: permitAll for WHITE_LIST: {}", (Object) WHITE_LIST);
                    log.info("Authorization rule: permitAll for PUBLIC_LIST: {}", (Object) PUBLIC_LIST);
                    log.info("Authorization rule: permitAll for GET /error");
                    log.info("Authorization rule: permitAll for GET /api/v1/products");
                    log.info("Authorization rule: permitAll for GET /api/v1/products/featured");
                    log.info("Authorization rule: permitAll for GET /api/v1/products/**");
                    log.info("Authorization rule: hasRole(ADMIN) for POST /api/v1/products");
                    log.info("Authorization rule: hasRole(ADMIN) for PUT /api/v1/products/**");
                    log.info("Authorization rule: hasRole(ADMIN) for DELETE /api/v1/products/**");
                    log.info("Authorization rule: permitAll for GET /api/v1/categories");
                    log.info("Authorization rule: permitAll for GET /api/v1/categories/**");
                    log.info("Authorization rule: hasRole(ADMIN) for POST /api/v1/categories");
                    log.info("Authorization rule: hasRole(ADMIN) for PUT /api/v1/categories/**");
                    log.info("Authorization rule: hasRole(ADMIN) for DELETE /api/v1/categories/**");
                    log.info("Authorization rule: hasRole(ADMIN) for /api/v1/users");
                    log.info("Authorization rule: hasRole(ADMIN) for /api/v1/users/**");
                    log.info("Authorization rule: hasAnyRole(CUSTOMER, ADMIN) for /api/v1/users/current");
                    log.info("Authorization rule: hasAnyRole(CUSTOMER, ADMIN) for /api/v1/users/current/**");
                    log.info("Authorization rule: hasAnyRole(CUSTOMER, ADMIN) for /api/v1/users/logout");
                    log.info("Authorization rule: hasAnyRole(CUSTOMER) for /api/v1/products/{id}/rate");
                    log.info("Authorization rule: authenticated for any other request");
                });

        log.info("Adding authentication provider");
        http.authenticationProvider(authenticationProvider());

        log.info("Adding JwtAuthenticationFilter before UsernamePasswordAuthenticationFilter");
        http.addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        log.info("SecurityFilterChain configuration completed");
        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("http://localhost:5173");
        corsConfig.addAllowedMethod("*");
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedOriginPattern("*");
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        log.info("Configured CORS: allowed origins={}, methods={}, headers={}, credentials={}",
                corsConfig.getAllowedOrigins(), corsConfig.getAllowedMethods(),
                corsConfig.getAllowedHeaders(), corsConfig.getAllowCredentials());
        return source;
    }
}