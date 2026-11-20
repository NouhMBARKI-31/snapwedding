package fr.snapwedding.media.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Propriétés storage.s3.* de application.yaml. */
@ConfigurationProperties(prefix = "storage.s3")
record S3Properties(String endpoint, String region, String accessKey, String secretKey, String bucket) {
}
