package sanhak.smartshield.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    /** Body: { "fileName": "20251109/demo01/clips/evt_..._gun_high.mp4" } */
    @PostMapping("/presigned")
    public Map<String, String> presigned(@RequestBody Map<String, String> body) {
        String key = body.get("fileName");
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("fileName is required");
        }
        // 유효시간 3분 (필요시 조정)
        return s3Service.presignPut(key, 3);
    }
}