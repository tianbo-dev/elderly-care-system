package com.lekang.elderlycare.controller;

import com.lekang.elderlycare.common.Result;
import com.lekang.elderlycare.dto.LoginDTO;
import com.lekang.elderlycare.dto.LoginVO;
import com.lekang.elderlycare.dto.RegisterDTO;
import com.lekang.elderlycare.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        try {
            return Result.success(authService.login(dto));
        } catch (Exception e) {
            return Result.error(401, e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterDTO dto) {
        try {
            authService.register(dto);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
