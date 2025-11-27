package sanhak.smartshield.report;

import lombok.Builder;
import lombok.Data;
import sanhak.smartshield.event.EventLog;

import java.time.OffsetDateTime;

@Data
@Builder
public class IncidentReportResponse {

    private Long reportId;
    private Long eventLogId;

    private String eventId;
    private String cameraId;
    private String detectedClass;
    private String dangerLevel;

    private String videoUrl;
    private String summary;
    private String report;

    private OffsetDateTime createdAt;

    public static IncidentReportResponse from(IncidentReport ir) {
        EventLog ev = ir.getEventLog();

        return IncidentReportResponse.builder()
                .reportId(ir.getId())
                .eventLogId(ev.getId())
                .eventId(ev.getEventId())
                .cameraId(ev.getCameraId())
                .detectedClass(ev.getDetectedClass())
                .dangerLevel(ev.getDangerLevel())
                .videoUrl(ir.getVideoUrl())
                .summary(ir.getSummary())
                .report(ir.getReport())
                .createdAt(ir.getCreatedAt())
                .build();
    }
}
