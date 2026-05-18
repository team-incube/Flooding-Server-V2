package team.incube.flooding.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

@Configuration
class R2StorageConfig {
    @Bean
    fun s3Client(fileStorageProperties: FileStorageProperties): S3Client {
        val credentials =
            AwsBasicCredentials.create(
                fileStorageProperties.accessKey,
                fileStorageProperties.secretKey,
            )

        val serviceConfiguration =
            S3Configuration
                .builder()
                .pathStyleAccessEnabled(true)
                .chunkedEncodingEnabled(false)
                .build()

        return S3Client
            .builder()
            .endpointOverride(URI.create(fileStorageProperties.endpoint))
            .region(Region.of(fileStorageProperties.region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .serviceConfiguration(serviceConfiguration)
            .build()
    }
}
