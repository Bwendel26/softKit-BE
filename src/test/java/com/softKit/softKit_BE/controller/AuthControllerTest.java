package com.softKit.softKit_BE.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softKit.softKit_BE.TestSecurityConfig;
import com.softKit.softKit_BE.model.dto.request.LoginRequest;
import com.softKit.softKit_BE.model.dto.request.RegisterRequest;
import com.softKit.softKit_BE.model.dto.response.LoginResponse;
import com.softKit.softKit_BE.model.dto.response.RegisterResponse;
import com.softKit.softKit_BE.model.dto.response.UserResponse;
import com.softKit.softKit_BE.model.enums.Role;
import com.softKit.softKit_BE.model.enums.Status;
import com.softKit.softKit_BE.service.AuthService;
import com.softKit.softKit_BE.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private JwtService jwtService;

	@Test
	void login_shouldReturnOkAndBody() throws Exception {

		LoginRequest request = new LoginRequest("john@example.com", "Password123");

		UserResponse userResponse = new UserResponse(
				UUID.randomUUID(), "John Doe", "john@example.com", null,
				Role.CUSTOMER, Status.ACTIVE, null, null,
				OffsetDateTime.now(), OffsetDateTime.now()
		);
		LoginResponse loginResponse = new LoginResponse(
				"jwt-token", "Bearer", 3600000L, userResponse
		);

		Mockito.when(authService.login(any(LoginRequest.class)))
				.thenReturn(loginResponse);

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.token").value("jwt-token"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.tokenType").value("Bearer"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.user.email").value("john@example.com"));
	}

	@Test
	void register_shouldReturnCreated() throws Exception {
		RegisterRequest request = new RegisterRequest(
				"John Doe", "john@example.com", "+5511999999999", "Password123"
		);

		RegisterResponse response = new RegisterResponse(
				UUID.randomUUID(), "John Doe", "john@example.com",
				"+5511999999999", LocalDateTime.now(), LocalDateTime.now()
		);

		when(authService.register(any(RegisterRequest.class)))
				.thenReturn(response);

		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.email").value("john@example.com"));
	}
}