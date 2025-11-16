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

    @GetMapping("/active")
    public Map<String, Object> active() {
        return Map.of("active", alertService.isActive());
    }

    @PostMapping("/resolve")
    public Map<String, Object> resolve() {
        alertService.signalResolved();
        return Map.of("status", "ok");
    }
}
