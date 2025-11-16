package sanhak.smartshield.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sanhak.smartshield.dto.EmailRequest;
import sanhak.smartshield.dto.VerifyRequest;
import sanhak.smartshield.service.EmailService;
import sanhak.smartshield.service.VerificationService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final VerificationService verificationService;

    // 인증메일 보내기
    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody EmailRequest request) {

        String email = request.getEmail();
        String code = verificationService.generateCode();

        verificationService.saveCode(email, code);
        emailService.sendVerificationEmail(email, code);

        return ResponseEntity.ok(Map.of(
                "message", "인증번호가 발송되었습니다.",
                "email", email
        ));
    }

    // 인증번호 검증
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest request) {

        boolean result = verificationService.verify(request.getEmail(), request.getCode());

        if (result) {
            return ResponseEntity.ok(Map.of(
                    "message", "인증 성공"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "인증 실패 (코드 불일치)"
            ));
        }
    }
}
