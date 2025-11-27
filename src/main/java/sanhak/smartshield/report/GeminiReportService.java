package sanhak.smartshield.report;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sanhak.smartshield.event.EventLog;

@Service
@RequiredArgsConstructor
public class GeminiReportService {

    private final GeminiClient geminiClient;
    private final IncidentReportRepository incidentReportRepository;

    /**
     * EventLog 기준으로 Gemini에 프롬프트를 보내서 출동 경위서를 생성하고,
     * IncidentReport 엔티티로 저장한 뒤 반환.
     */
    public IncidentReport generateIncidentReport(EventLog eventLog) {

        // 1) 프롬프트 구성 (한국어로 깔끔하게)
        String prompt = buildPrompt(eventLog);

        // 2) Gemini 호출 → 보고서 본문 생성
        String reportText = geminiClient.generateText(prompt);

        if (reportText == null || reportText.isBlank()) {
            // 실패한 경우에 대한 처리 (간단히 메시지 대체)
            reportText = "Gemini API 호출에 실패하여 자동 경위서를 생성하지 못했습니다. " +
                    "필요 시 수동으로 보고서를 작성해 주세요.\n\n" +
                    "(eventId=" + eventLog.getEventId() + ")";
        }

        // 3) 요약문은 일단 앞부분만 잘라서 사용 (나중에 별도 요약 모델 호출해도 됨)
        String summary = summarize(reportText);

        // 4) IncidentReport 엔티티 생성 및 저장
        IncidentReport incidentReport = IncidentReport.builder()
                .eventLog(eventLog)
                .videoUrl(eventLog.getS3Url())      // EventLog의 s3Url 그대로 사용
                .summary(summary)
                .report(reportText)
                .status(IncidentReportStatus.COMPLETED)
                .build();

        return incidentReportRepository.save(incidentReport);
    }

    /**
     * Gemini 프롬프트 작성
     */
    private String buildPrompt(EventLog log) {

        String createdAtText = (log.getCreatedAt() != null)
                ? log.getCreatedAt().toString()
                : "알 수 없음";

        return """
            너는 현장으로 출동한 경찰관의 바디캠에 녹화된 현장 영상을 분석하여 "출동 경위서"를 자동으로 작성하는 비서이다.
            아래에 제공되는 사건 정보를 바탕으로, 실제 경찰 또는 보안팀에 제출해도 어색하지 않은
            형식의 출동 경위서를 한국어로 존댓말을 사용하여 사무적인 말투로 작성해라. 너무 길게 작성하진 않도록 해라.


            [작성 규칙]
            1. 반드시 아래 [출력 형식]에 제시된 제목과 순서를 그대로 사용한다.
            2. 각 항목은 존댓말을 사용해서 완전한 문장으로 작성한다.
            3. 한 항목당 2~4문장 정도로, 과도한 반복 없이 핵심 위주로 작성한다.
            4. 제공된 정보 범위 안에서만 합리적으로 추론하고, 과도한 상상이나 확정적인 표현은 피한다.
            5. 실제 보고서처럼 날짜, 장소, 상황, 조치 내용을 구체적으로 설명한다.
            6. 불확실한 정보는 "추정되며", "가능성이 있습니다" 등으로 표현한다.

            [출력 형식]
            아래 형식을 그대로 따라 텍스트를 출력하라.
            Markdown, 불릿포인트, 번호 매기기 등을 쓰지 말고,
            각 항목 제목과 내용 사이에는 줄바꿈을 한 줄 넣어라.

            제목: (감지된 무기를 언급하며 한 줄로 간단한 사건 제목 작성)
            작성자 : Smartshield AI 보고자
            
            [사건 기본 정보]
            - 이벤트 ID: %s
            - 카메라 ID: %s
            - 감지된 객체(무기 종류): %s
            - 위험도 등급: %s
            - 관련 영상 URL: %s
            - 발생 시각: %s

            1. 사건 개요
            (사건 발생 일시, 장소, 어떤 위험 요소가 감지되었는지 요약)

            2. 출동 경위
            (어떤 위협요소가 감지되어 출동을 했는지 요약)

            3. 현장 상황
            (출동 시간, 위협 요소의 상태 등을 정리)

            4. 출동 필요성
            (감지된 무기와 위험 등급을 고려했을 때 출동을 해야 했던 이유를 들며 필요성 요약)

            5. 향후 조치 및 건의 사항
            (추가적으로 필요한 조치, 재발 방지를 위한 제언 등)

            위의 출력 형식을 정확히 지키며, 하나의 완성된 출동 경위서를 작성하라.
            """.formatted(
                log.getEventId(),
                log.getCameraId(),
                log.getDetectedClass(),
                log.getDangerLevel(),
                log.getS3Url(),
                createdAtText
        );
    }


    /**
     * 간단 요약: 앞부분 200자 정도만 잘라서 summary로 사용
     */
    private String summarize(String fullText) {
        if (fullText == null) return null;
        int limit = 200;
        return fullText.length() <= limit ? fullText : fullText.substring(0, limit) + "...";
    }
}
