package sanhak.smartshield.report;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final IncidentReportRepository incidentReportRepository;

    /**
     * 가장 최근에 생성된 IncidentReport 한 건 반환
     * write_down.jsx에서 /api/report/latest 로 호출
     */
    @GetMapping("/latest")
    public ResponseEntity<IncidentReportResponse> getLatest() {
        return incidentReportRepository.findTopByOrderByCreatedAtDesc()
                .map(IncidentReportResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
