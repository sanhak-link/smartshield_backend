package sanhak.smartshield.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class VerificationService {

    // 이메일별 인증번호 저장 (임시: 메모리)
    private final Map<String, String> codeStorage = new ConcurrentHashMap<>();

    private final Random random = new Random();

    // 인증코드 생성
    public String generateCode() {
        return String.format("%06d", random.nextInt(999999));
    }

    // 인증코드 저장
    public void saveCode(String email, String code) {
        codeStorage.put(email, code);
        log.info("인증코드 저장: {} → {}", email, code);
    }

    // 인증코드 검증
    public boolean verify(String email, String code) {
        String saved = codeStorage.get(email);
        return saved != null && saved.equals(code);
    }
}
