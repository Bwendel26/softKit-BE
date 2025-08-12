package com.softKit.softKit_BE.controller;


import com.softKit.softKit_BE.model.vo.UserVO;
import com.softKit.softKit_BE.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class UserController {

    @Autowired
    UserService service;

    @GetMapping("/{id}")
    public ResponseEntity<UserVO> getUserById(@PathVariable Long id) {
        var userVO = service.getUserById(id);
        return ResponseEntity.ok(userVO);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserVO user, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body((buildErrorResponse(bindingResult)));
        }
        // e-mail validation
        if(service.existsByEmail(user.email())) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Validation error: email");
            errorResponse.put("errors", Map.of("email", "Email already in use!"));
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            UserVO savedUser = service.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error creating user");
            errorResponse.put("errors", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private Map<String, Object> buildErrorResponse(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();

        bindingResult.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "validation error");
        errorResponse.put("errors", errors);

        return errorResponse;

    }
}
