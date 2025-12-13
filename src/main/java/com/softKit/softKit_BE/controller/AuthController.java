package com.softKit.softKit_BE.controller;

import com.softKit.softKit_BE.model.dto.requests.ForgotPasswordRequest;
import com.softKit.softKit_BE.model.dto.requests.LoginRequest;
import com.softKit.softKit_BE.model.dto.requests.RegisterRequest;
import com.softKit.softKit_BE.model.dto.requests.ResetPasswordRequest;
import com.softKit.softKit_BE.model.dto.responses.LoginResponse;
import com.softKit.softKit_BE.model.dto.responses.RegisterResponse;
import com.softKit.softKit_BE.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginrequest) {
		LoginResponse response = authService.login(loginrequest);
		return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
		RegisterResponse registerResponse = authService.register(registerRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }

	@PostMapping("/refresh-token")
	public ResponseEntity<LoginResponse> refreshToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
		LoginResponse response = authService.refreshToken(authHeader);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/reset-password")
	public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		authService.resetPassword(request);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		authService.forgotPassword(request);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/validate-token")
	public ResponseEntity<String> validateToken() {
		// TODO: implement a real flow for validate tokens.
		return ResponseEntity.ok("ok");
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout() {
		// TODO: implement a real flow for blacklist/whitelist tokens.
		return ResponseEntity.ok("ok");
	}

	@PostMapping("/verify-email")
	public ResponseEntity<String> verifyEmail() {
		// TODO: implement a real flow for verify registered email. Priority, depends on the register flow.
		return ResponseEntity.ok("ok");
	}

}
