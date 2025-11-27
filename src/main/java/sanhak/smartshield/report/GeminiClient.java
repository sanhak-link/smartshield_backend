package sanhak.smartshield.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiClient {

    private static final String MODEL_NAME = "gemini-2.5-flash"; // 필요시 변경
    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;

    public GeminiClient(@Value("${GEMINI_API_KEY}") String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY 가 설정되어 있지 않습니다.");
        }
        this.apiKey = apiKey;
    }

    /**
     * 단순 텍스트 프롬프트 → 텍스트 결과 반환
     */
    public String generateText(String prompt) {
        String url = BASE_URL + "/" + MODEL_NAME + ":generateContent?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String rawJson;
        try {
            rawJson = restTemplate.postForObject(url, entity, String.class);
        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage(), e);
            return "";
        }

        return extractTextFromResponse(rawJson);
    }

    /**
     * Gemini 응답 JSON에서 text만 뽑아서 하나의 String으로 합치기
     */
    private String extractTextFromResponse(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                return "";
            }

            JsonNode content = candidates.get(0).path("content");
            JsonNode parts = content.path("parts");
            if (!parts.isArray()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            for (JsonNode part : parts) {
                String text = part.path("text").asText("");
                sb.append(text);
            }
            return sb.toString().trim();
        } catch (IOException e) {
            log.error("Gemini 응답 파싱 실패: {}", e.getMessage(), e);
            return "";
        }
    }
}
