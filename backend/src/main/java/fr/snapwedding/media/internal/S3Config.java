package fr.snapwedding.media.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import java.net.URI;

/** Client S3 générique (RustFS en local, AWS S3 en production) : même code, autre configuration. */
@Slf4j
@Configuration
class S3Config {

    @Bean
    S3Client s3Client(S3Properties props) {
        return S3Client.builder()
                .endpointOverride(URI.create(props.endpoint()))
                .region(Region.of(props.region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.accessKey(), props.secretKey())))
                .forcePathStyle(true) // http://localhost:9000/<bucket>/<clé>
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
    }

    /** Crée le bucket au démarrage s'il n'existe pas. */
    @Bean
    ApplicationRunner createBucketIfMissing(S3Client s3Client, S3Properties props) {
        return args -> {
            try {
                s3Client.headBucket(b -> b.bucket(props.bucket()));
            } catch (NoSuchBucketException e) {
                s3Client.createBucket(b -> b.bucket(props.bucket()));
                log.info("Bucket '{}' created", props.bucket());
            }
        };
    }
}
