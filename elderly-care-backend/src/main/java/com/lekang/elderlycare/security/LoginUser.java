package com.lekang.elderlycare.security;

/**
 * 登录用户身份（由 JWT 解析而来，存入 Spring Security 上下文）
 */
public record LoginUser(Long userId, String username, String role) {

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isFamily() {
        return "FAMILY".equals(role);
    }
}
