package sanhak.smartshield.alert;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {
    private final AlertService alertService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        System.out.println("🔌 [SSE] client connected");
        return alertService.subscribe();
    }

    // 🔸 main.jsx가 초기 동기화에서 호출하는 API
    @GetMapping("/active")
    public Map<String, Object> active() {
        return Map.of("active", alertService.isActive());
    }

    // 🔸 데모/테스트용 해제 API(원하면 버튼과 연결)
    @PostMapping("/resolve")
    public Map<String, Object> resolve() {
        alertService.signalResolved();
        return Map.of("status", "ok");
    }
}
