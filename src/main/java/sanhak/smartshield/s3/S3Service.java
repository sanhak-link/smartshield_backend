package sanhak.smartshield.s3;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URL;
import java.time.Duration;
import java.util.Map;

@Service
public class S3Service {

    private final String bucket;
    private final String region;
    private final S3Presigner presigner;

    public S3Service() {
        // .env -> DotenvLoader 가 System properties 로 주입해둔 값 사용
        String accessKey = System.getProperty("AWS_ACCESS_KEY_ID");
        String secretKey = System.getProperty("AWS_SECRET_ACCESS_KEY");
        this.region      = System.getProperty("AWS_REGION", "ap-northeast-2");
        this.bucket      = System.getProperty("S3_BUCKET");

        this.presigner = S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .build();
    }

    /** 업로드용 Presigned PUT URL 발급 */
    public Map<String, String> presignPut(String key, int ttlMinutes) {
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        PutObjectPresignRequest preq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(ttlMinutes))
                .putObjectRequest(put)
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(preq);
        URL uploadUrl = presigned.url();

        // 참고용(버킷 private이면 직접 열리진 않을 수 있음)
        String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);

        return Map.of(
                "uploadUrl", uploadUrl.toString(),
                "fileUrl",   fileUrl
        );
    }
}