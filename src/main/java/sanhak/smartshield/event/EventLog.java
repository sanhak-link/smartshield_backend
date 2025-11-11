package sanhak.smartshield.event;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.*;

import java.time.OffsetDateTime;


@Entity
@Table(name = "event_log")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EventLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64, unique = true, nullable = false)
    private String eventId;

    @Column(length = 32, nullable = false)
    private String cameraId;

    @Column(length = 16, nullable = false)
    private String detectedClass;   // gun / knife

    @Column(length = 16, nullable = false)
    private String dangerLevel;     // LOW/MEDIUM/HIGH

    @Column(length = 255, nullable = false)
    private String s3Key;           // YYYYMMDD/{camera}/clips/{eventId}_{class}_{level}.mp4

    @Lob @Column(nullable = false)
    private String s3Url;           // 정보 제공용(버킷 private이면 presigned GET으로 재생)

    private OffsetDateTime createdAt;

    @Lob
    private String metaJson;        // JSON 문자열 (fps, clip_start_sec 등)
}