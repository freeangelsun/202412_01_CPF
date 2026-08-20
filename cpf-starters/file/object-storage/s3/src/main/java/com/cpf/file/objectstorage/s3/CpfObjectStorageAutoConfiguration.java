package com.cpf.file.objectstorage.s3;

import com.cpf.file.objectstorage.api.CpfObjectStorageLifecycleHook;
import com.cpf.file.objectstorage.api.CpfObjectStorageOperations;
import com.cpf.file.objectstorage.api.CpfObjectStorageScanHook;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.file.context.CpfFileContextSupport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * S3 Object Storage capability를 CPF Object Storage API에 연결하는 Public Starter 자동구성입니다.
 * <p>Bucket/Region/Endpoint 정책을 검증하고 scan/lifecycle/context hook을 조립합니다.
 */
@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(CpfObjectStorageProperties.class)
@ConditionalOnClass(S3Client.class)
@ConditionalOnProperty(prefix="cpf.file.object-storage.s3",name="enabled",havingValue="true")
public class CpfObjectStorageAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    S3Client cpfS3Client(CpfObjectStorageProperties p){var b=S3Client.builder().region(Region.of(p.getRegion())).forcePathStyle(p.isPathStyle()).overrideConfiguration(o->o.apiCallTimeout(p.getApiCallTimeout()).apiCallAttemptTimeout(p.getApiCallAttemptTimeout()).retryStrategy(r->r.maxAttempts(p.getMaxAttempts())));if(p.getEndpoint()!=null)b.endpointOverride(p.getEndpoint());return b.build();}
    @Bean @ConditionalOnMissingBean
    S3Presigner cpfS3Presigner(CpfObjectStorageProperties p){var b=S3Presigner.builder().region(Region.of(p.getRegion()));if(p.getEndpoint()!=null)b.endpointOverride(p.getEndpoint());return b.build();}
    @Bean @ConditionalOnMissingBean CpfObjectStorageScanHook cpfObjectStorageScanHook(){return CpfObjectStorageScanHook.NOOP;}
    @Bean @ConditionalOnMissingBean CpfObjectStorageLifecycleHook cpfObjectStorageLifecycleHook(){return CpfObjectStorageLifecycleHook.METADATA_RETENTION;}
    @Bean @ConditionalOnMissingBean CpfFileContextSupport cpfFileContextSupport(CpfContextExecutionFactory factory){return new CpfFileContextSupport(factory);}
    @Bean @ConditionalOnMissingBean
    CpfObjectStorageOperations cpfObjectStorageOperations(S3Client s3,S3Presigner pr,CpfObjectStorageProperties p,CpfObjectStorageScanHook scan,CpfObjectStorageLifecycleHook lifecycle,CpfFileContextSupport ctx){if(p.getBucket()==null||p.getBucket().isBlank())throw new IllegalStateException("cpf.file.object-storage.s3.bucket is required");return new CpfS3ObjectStorageOperations(s3,pr,p,scan,lifecycle,ctx);}
    @Bean @ConditionalOnMissingBean CpfObjectStorageReconciler cpfObjectStorageReconciler(CpfObjectStorageOperations ops,CpfObjectStorageProperties p){return new CpfObjectStorageReconciler(ops,p);}
}
