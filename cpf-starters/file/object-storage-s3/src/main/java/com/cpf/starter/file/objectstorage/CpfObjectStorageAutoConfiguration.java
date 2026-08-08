package com.cpf.starter.file.objectstorage;
import com.cpf.core.api.attachment.CpfObjectStorageOperations;
import org.springframework.boot.autoconfigure.AutoConfiguration; import org.springframework.boot.autoconfigure.condition.*; import org.springframework.boot.context.properties.EnableConfigurationProperties; import org.springframework.context.annotation.Bean; import software.amazon.awssdk.regions.Region; import software.amazon.awssdk.services.s3.*; import software.amazon.awssdk.services.s3.presigner.S3Presigner;
@AutoConfiguration @EnableConfigurationProperties(CpfObjectStorageProperties.class) @ConditionalOnClass(S3Client.class) @ConditionalOnProperty(prefix="cpf.file.object-storage.s3",name="enabled",havingValue="true") public class CpfObjectStorageAutoConfiguration {
 @Bean @ConditionalOnMissingBean S3Client cpfS3Client(CpfObjectStorageProperties p){var b=S3Client.builder().region(Region.of(p.getRegion())).forcePathStyle(p.isPathStyle()); if(p.getEndpoint()!=null)b.endpointOverride(p.getEndpoint());return b.build();}
 @Bean @ConditionalOnMissingBean S3Presigner cpfS3Presigner(CpfObjectStorageProperties p){var b=S3Presigner.builder().region(Region.of(p.getRegion()));if(p.getEndpoint()!=null)b.endpointOverride(p.getEndpoint());return b.build();}
 @Bean @ConditionalOnMissingBean CpfObjectStorageScanHook cpfObjectStorageScanHook(){return CpfObjectStorageScanHook.NOOP;}
 @Bean @ConditionalOnMissingBean CpfObjectStorageOperations cpfObjectStorageOperations(S3Client s3,S3Presigner pr,CpfObjectStorageProperties p,CpfObjectStorageScanHook scan){if(p.getBucket()==null||p.getBucket().isBlank())throw new IllegalStateException("cpf.file.object-storage.s3.bucket is required");return new CpfS3ObjectStorageOperations(s3,pr,p,scan);}
}
