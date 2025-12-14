package com.softKit.softKit_BE.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softKit.softKit_BE.TestSecurityConfig;
import com.softKit.softKit_BE.model.dto.request.UserCreateRequest;
import com.softKit.softKit_BE.model.dto.request.UserUpdateRequest;
import com.softKit.softKit_BE.model.dto.response.UserResponse;
import com.softKit.softKit_BE.model.enums.Role;
import com.softKit.softKit_BE.model.enums.Status;
import com.softKit.softKit_BE.service.JwtService;
import com.softKit.softKit_BE.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private JwtService jwtService;

	@Test
	@WithMockUser(roles = "ADMIN")
	void getAllUsers_shouldReturnPagedList_forAdmin() throws Exception {
		UserResponse user1 = new UserResponse(
				UUID.randomUUID(), "John Doe", "john@example.com",
				null, Role.CUSTOMER, Status.ACTIVE,
				null, null, OffsetDateTime.now(), OffsetDateTime.now()
		);
		UserResponse user2 = new UserResponse(
				UUID.randomUUID(), "Jane Doe", "jane@example.com",
				null, Role.ADMIN, Status.ACTIVE,
				null, null, OffsetDateTime.now(), OffsetDateTime.now()
		);

		Pageable pageable = PageRequest.of(0, 10);
		Page<UserResponse> page = new PageImpl<>(List.of(user1, user2), pageable, 2);

		when(userService.getAll(any(Pageable.class))).thenReturn(page);

		mockMvc.perform(get("/api/v1/users")
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].email").value("john@example.com"))
				.andExpect(jsonPath("$.content[1].email").value("jane@example.com"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void createUser_shouldReturnCreated_forAdmin() throws Exception {

		UserCreateRequest request = new UserCreateRequest(
				"John Doe", "john@example.com", "+5511999999999", "Password123"
		);

		UserResponse response = new UserResponse(
				UUID.randomUUID(), "John Doe", "john@example.com",
				"+5511999999999", Role.CUSTOMER, Status.ACTIVE,
				null, null, OffsetDateTime.now(), OffsetDateTime.now()
		);

		when(userService.create(any(UserCreateRequest.class)))
				.thenReturn(response);

		mockMvc.perform(post("/api/v1/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("john@example.com"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void updateUser_shouldReturnOk_forAdmin() throws Exception {

		UserUpdateRequest request = new UserUpdateRequest(
				"John Doe Updated", "john@example.com", "+5511999999999"
		);

		UUID userId = UUID.randomUUID();
		UserResponse response = new UserResponse(
				userId, "John Doe Updated", "john@example.com",
				"+5511999999999", Role.CUSTOMER, Status.ACTIVE,
				null, null, OffsetDateTime.now(), OffsetDateTime.now()
		);

		when(userService.update(eq(userId), any(UserUpdateRequest.class)))
				.thenReturn(response);

		mockMvc.perform(put("/api/v1/users/{id}", 1L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fullName").value("John Doe Updated"));
	}
}