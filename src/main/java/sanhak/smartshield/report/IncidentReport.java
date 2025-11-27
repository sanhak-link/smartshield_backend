package sanhak.smartshield.report;


import jakarta.persistence.*;
import lombok.*;
import sanhak.smartshield.event.EventLog;

import java.time.OffsetDateTime;

@Entity
@Table(name = "incident_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 어떤 EventLog(사건)에 대한 출동 경위서인지 연결 (1:1)
     */
    @OneToOne(fetch = FetchType.EAGER) //LAZY에서 변경
    @JoinColumn(name = "event_log_id", nullable = false, unique = true)
    private EventLog eventLog;

    /**
     * S3에 저장된 영상 URL
     * - EventLog.s3Url와 동일 값을 보관해도 되고,
     *   나중에 URL 정책이 바뀌어도 당시 기준을 보존하기 위한 용도로도 사용 가능
     */
    @Column(name = "video_url", nullable = false, length = 1024)
    private String videoUrl;

    /**
     * 리스트 화면에서 한 줄로 보여줄 요약
     */
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    /**
     * 전체 출동 경위서 본문
     */
    @Lob
    @Column(name = "report", nullable = false, columnDefinition = "LONGTEXT")
    private String report;

    /**
     * 생성 상태: PENDING / COMPLETED / FAILED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IncidentReportStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) {
            // 현재는 동기 생성 기준으로 COMPLETED 기본값 사용
            this.status = IncidentReportStatus.COMPLETED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
