package sanhak.smartshield.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sanhak.smartshield.alert.AlertService;
import sanhak.smartshield.event.dto.EventCompleteRequest;
import sanhak.smartshield.report.GeminiReportService;
import sanhak.smartshield.report.IncidentReport;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
public class EventController {

    private final EventLogRepository repo;
    private final AlertService alertService;
    private final GeminiReportService geminiReportService;
    private final ObjectMapper om = new ObjectMapper();

    @PostMapping("/complete")
    public Map<String, Object> complete(@RequestBody EventCompleteRequest req) throws JsonProcessingException {
        String metaJson = req.getMeta() == null ? "{}" : om.writeValueAsString(req.getMeta());

        EventLog log = EventLog.builder()
                .eventId(req.getEvent_id())
                .cameraId(req.getCamera_id())
                .detectedClass(req.getDetected_class())
                .dangerLevel(req.getDanger_level())
                .s3Key(req.getS3_key() == null ? "" : req.getS3_key())
                .s3Url(req.getFile_url())
                .createdAt(OffsetDateTime.now())
                .metaJson(metaJson)
                .build();
        repo.save(log);

        // Gemini 출동 경위서 자동 생성
        IncidentReport report = geminiReportService.generateIncidentReport(log);
        System.out.println("📝 [REPORT] incident_report 생성 완료 id=" + report.getId());

        // 실시간 알림 push (프론트는 /api/alerts/stream 구독)
        Map<String, Object> payload = Map.of(
                "event_id", req.getEvent_id(),
                "camera_id", req.getCamera_id(),
                "detected_class", req.getDetected_class(),
                "danger_level", req.getDanger_level(),
                "video_url", req.getFile_url()
        );
        alertService.broadcast(payload);

        // main.jsx 호환: ON 신호 추가
        alertService.signalCreated();

        System.out.println("✅ [EVENT] saved: eventId=" + req.getEvent_id() + ", level=" + req.getDanger_level());

        return Map.of(
                "status", "ok",
                "id", log.getId(),
                "reportId", report.getId()      // 프론트에서 필요하면 사용
        );
    }

    @GetMapping("/list")
    public List<EventLog> list() {
        return repo.findAll();
    }
}
