package com.softKit.softKit_BE.controller;

import com.softKit.softKit_BE.model.dto.LoginRequestDTO;
import com.softKit.softKit_BE.model.dto.LoginResponseDTO;
import com.softKit.softKit_BE.model.dto.UserCreateDTO;
import com.softKit.softKit_BE.model.vo.UserResponseVO;
import com.softKit.softKit_BE.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO requestDTO, BindingResult result) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(buidErrorResponse(result));
        }

        try {
            LoginResponseDTO responseDTO = authService.login(requestDTO);
            return ResponseEntity.ok(responseDTO);
        }  catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid credentials");
            errorResponse.put("errors", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserCreateDTO  createDTO, BindingResult result) {
        if(result.hasErrors()) {
            return ResponseEntity.badRequest().body(buidErrorResponse(result));
        }
        try {
            UserResponseVO responseVO = authService.register(createDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseVO);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Registration failed");
            errorResponse.put("errors", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Registration failed");
            errorResponse.put("errors", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    private Map<String, Object> buidErrorResponse(BindingResult bindingResult) {

        Map<String, Object> errors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "Validation error");
        errorResponse.put("errors", errors);
        return errorResponse;
    }

}
