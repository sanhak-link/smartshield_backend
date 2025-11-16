package sanhak.smartshield.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String code) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);

            // ★ 반드시 username과 동일해야 함
            helper.setFrom("signup@smartshield.site");

            helper.setSubject("[SmartShield] 회원가입 인증번호 안내");

            String html = """
                    <h2>SmartShield 회원가입 인증번호</h2>
                    <p>아래 인증번호를 앱에 입력해주세요.</p>
                    <h1>%s</h1>
                    <p>감사합니다.</p>
                    """.formatted(code);

            helper.setText(html, true);

            mailSender.send(message);

            log.info("이메일 발송 완료 → {}", to);

        } catch (MessagingException e) {
            log.error("메일 발송 실패", e);
            throw new RuntimeException(e);
        }
    }
}
