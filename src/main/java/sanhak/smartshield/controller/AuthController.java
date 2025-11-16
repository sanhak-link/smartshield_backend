package sanhak.smartshield.controller;

import sanhak.smartshield.dto.ApiResponse;
import sanhak.smartshield.dto.AuthResponse;
import sanhak.smartshield.dto.LoginRequest;
import sanhak.smartshield.dto.SignupRequest;
import sanhak.smartshield.security.jwt.CookieUtil;
import sanhak.smartshield.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final CookieUtil cookieUtil;
    
    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(
        @Valid @RequestBody SignupRequest request,
        HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.signup(request);
        
        if (authResponse.isSuccess()) {
            // RefreshToken을 HttpOnly 쿠키로 설정
            String refreshToken = authResponse.getAccessToken(); // 실제로는 별도로 반환받아야 함
            // cookieUtil.addRefreshTokenCookie(response, refreshToken);
        }
        
        return ResponseEntity.ok(authResponse);
    }
    
    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.login(request);
        
        if (authResponse.isSuccess()) {
            // RefreshToken을 HttpOnly 쿠키로 설정
            // 실제 구현에서는 AuthResponse에 refreshToken 필드 추가 필요
        }
        
        return ResponseEntity.ok(authResponse);
    }
    
    /**
     * AccessToken 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(request)
            .orElseThrow(() -> new RuntimeException("RefreshToken이 없습니다."));
        
        AuthResponse authResponse = authService.refreshAccessToken(refreshToken);
        
        if (authResponse.isSuccess()) {
            // 새로운 RefreshToken을 HttpOnly 쿠키로 설정
            // cookieUtil.addRefreshTokenCookie(response, newRefreshToken);
        }
        
        return ResponseEntity.ok(authResponse);
    }
    
    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(
        Authentication authentication,
        HttpServletResponse response
    ) {
        String email = authentication.getName();
        authService.logout(email);
        
        // RefreshToken 쿠키 삭제
        cookieUtil.deleteRefreshTokenCookie(response);
        
        return ResponseEntity.ok(ApiResponse.builder()
            .success(true)
            .message("로그아웃 되었습니다.")
            .build());
    }
    
    /**
     * 현재 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.builder()
            .success(true)
            .message("사용자 정보 조회 성공")
            .data(authentication.getPrincipal())
            .build());
    }
}
