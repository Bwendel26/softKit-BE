package com.softKit.softKit_BE.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softKit.softKit_BE.exception.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

		ErrorResponse body = new ErrorResponse(
				"Unauthorized",
				Map.of("auth", "Authentication is required or token is invalid"),
				HttpStatus.UNAUTHORIZED.value(),
				request.getRequestURI(),
				Instant.now()
		);

		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType("application/json");

		objectMapper.writeValue(response.getWriter(), body);
    }
}
