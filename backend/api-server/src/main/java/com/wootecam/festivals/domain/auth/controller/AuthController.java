package com.wootecam.festivals.domain.auth.controller;

import com.wootecam.festivals.domain.auth.dto.LoginRequest;
import com.wootecam.festivals.domain.auth.service.AuthService;
import com.wootecam.festivals.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/login")
    public ApiResponse<Void> login(@Valid @RequestBody LoginRequest dto) {
        authService.login(dto.email());
        return ApiResponse.empty();
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.empty();
    }
}
