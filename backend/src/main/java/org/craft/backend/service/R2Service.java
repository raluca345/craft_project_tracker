package org.craft.backend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.time.Duration;

@Service
public class R2Service {

    @Value("${cloudflare.r2.account-id}")
    private String accountId;

    @Value("${cloudflare.r2.access-key}")
    private String accessKey;

    @Value("${cloudflare.r2.secret-key}")
    private String secretKey;

    @Value("${cloudflare.r2.bucket}")
    private String bucketName;

    private S3Client s3Client;
    private S3Presigner presigner;

    @PostConstruct
    public void init() {
        StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );
        URI endpoint = URI.create("https://" + accountId + ".r2.cloudflarestorage.com");

        s3Client = S3Client.builder()
                .endpointOverride(endpoint)
                .credentialsProvider(credentials)
                .region(Region.US_EAST_1)
                .build();

        presigner = S3Presigner.builder()
                .endpointOverride(endpoint)
                .credentialsProvider(credentials)
                .region(Region.US_EAST_1)
                .build();
    }

    public String putObject(String key, byte[] content, String contentType) {
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(content));
        return key;
    }

    public void deleteObject(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }

    // Short-lived signed GET URL: browsers fetch images straight from R2 while
    // the bucket stays private and ownership is checked where URLs are minted.
    public String presignGetUrl(String key, Duration ttl) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        return presigner.presignGetObject(b -> b
                        .getObjectRequest(request)
                        .signatureDuration(ttl))
                .url()
                .toString();
    }
}
