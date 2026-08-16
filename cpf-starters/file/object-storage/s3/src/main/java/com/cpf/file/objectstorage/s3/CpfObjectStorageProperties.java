package com.cpf.file.objectstorage.s3;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** S3-compatible provider configuration. Credentials remain in the AWS credential-provider chain/Secret owner. */
@ConfigurationProperties("cpf.file.object-storage.s3")
/** CpfObjectStorageProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfObjectStorageProperties {
    private boolean enabled;
    private String bucket;
    private String region="us-east-1";
    private URI endpoint;
    private boolean pathStyle=true;
    private String kmsKeyId;
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private Duration apiCallTimeout=Duration.ofSeconds(30);
    private Duration apiCallAttemptTimeout=Duration.ofSeconds(10);
    private int maxAttempts=3;
    private Duration reconcileDelay=Duration.ofMinutes(10);
    private Duration orphanMultipartAge=Duration.ofHours(24);
    private boolean allowPresignedPut;
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
    public String getBucket(){return bucket;} public void setBucket(String v){bucket=v;}
    public String getRegion(){return region;} public void setRegion(String v){region=v;}
    public URI getEndpoint(){return endpoint;} public void setEndpoint(URI v){endpoint=v;}
    public boolean isPathStyle(){return pathStyle;} public void setPathStyle(boolean v){pathStyle=v;}
    public String getKmsKeyId(){return kmsKeyId;} public void setKmsKeyId(String v){kmsKeyId=v;}
    public Duration getApiCallTimeout(){return apiCallTimeout;} public void setApiCallTimeout(Duration v){apiCallTimeout=positive(v,"apiCallTimeout");}
    public Duration getApiCallAttemptTimeout(){return apiCallAttemptTimeout;} public void setApiCallAttemptTimeout(Duration v){apiCallAttemptTimeout=positive(v,"apiCallAttemptTimeout");}
    public int getMaxAttempts(){return maxAttempts;} public void setMaxAttempts(int v){if(v<1||v>10)throw new IllegalArgumentException("maxAttempts must be 1..10");maxAttempts=v;}
    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    public Duration getReconcileDelay(){return reconcileDelay;} public void setReconcileDelay(Duration v){reconcileDelay=positive(v,"reconcileDelay");}
    public Duration getOrphanMultipartAge(){return orphanMultipartAge;} public void setOrphanMultipartAge(Duration v){orphanMultipartAge=positive(v,"orphanMultipartAge");}
    public boolean isAllowPresignedPut(){return allowPresignedPut;} public void setAllowPresignedPut(boolean v){allowPresignedPut=v;}
    private static Duration positive(Duration v,String n){if(v==null||v.isZero()||v.isNegative())throw new IllegalArgumentException(n+" must be positive");return v;}
}
