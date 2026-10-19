package school.sptech;

import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3Provider {
    private final AwsSessionCredentials credentials;

    public S3Provider() {
        // Se você está usando AWS Academy ou um laboratório com credenciais temporárias,
        // tem que usar session credentials
        this.credentials = AwsSessionCredentials.create(
                "SUA_ACCESS_KEY",
                "SUA_SECRET_KEY",
                "SEU_SESSION_TOKEN"
        );
    }

    public S3Client getS3Client() {
        return S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
