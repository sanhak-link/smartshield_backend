package sanhak.smartshield.alert;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        System.out.println(" [SSE] Client connected");
        return alertService.subscribe();
    }

    @PostMapping("/created")
    public void sendCreated() {
        alertService.signalCreated();
    }

    @PostMapping("/resolved")
    public void sendResolved() {
        alertService.signalResolved();
    }
}
