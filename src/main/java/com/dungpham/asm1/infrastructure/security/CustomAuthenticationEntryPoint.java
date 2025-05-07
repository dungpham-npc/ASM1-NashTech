package com.dungpham.asm1.infrastructure.security;

import com.dungpham.asm1.common.exception.UnauthorizedException;
import com.dungpham.asm1.response.BaseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        UnauthorizedException ex = new UnauthorizedException(request.getRequestURI());

        BaseResponse<Object> errorResponse = BaseResponse.builder()
                .status(false)
                .code(ex.getErrorCodeString())
                .message(ex.getFormattedMessage())
                .data(null)
                .build();

        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }
}
