package sanhak.smartshield.event.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class EventCompleteRequest {

    @JsonProperty("event_id")
    private String event_id;      // snake
    @JsonProperty("eventId")
    private void setEventIdAlias(String v) { this.event_id = v; } // camel도 허용

    @JsonProperty("camera_id")
    private String camera_id;
    @JsonProperty("cameraId")
    private void setCameraIdAlias(String v) { this.camera_id = v; }

    @JsonProperty("detected_class")
    private String detected_class;
    @JsonProperty("detectedClass")
    private void setDetectedClassAlias(String v) { this.detected_class = v; }

    @JsonProperty("danger_level")
    private String danger_level;
    @JsonProperty("dangerLevel")
    private void setDangerLevelAlias(String v) { this.danger_level = v; }

    @JsonProperty("file_url")
    private String file_url;
    @JsonProperty("fileUrl")
    private void setFileUrlAlias(String v) { this.file_url = v; }

    @JsonProperty("s3_key")
    private String s3_key; // 옵션

    private Map<String, Object> meta;
}