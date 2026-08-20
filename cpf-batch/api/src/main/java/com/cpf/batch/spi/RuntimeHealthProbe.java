package com.cpf.batch.spi;
import com.cpf.batch.api.DeploymentCellManifest;
/**
 * 배포 대상 Batch Runtime Instance의 liveness/readiness를 Control Plane이 확인하는 SPI입니다.
 * <p>Probe 실패나 timeout은 배포 성공으로 간주하지 않으며 rollout/rollback 정책의 입력으로 사용합니다.
 */
public interface RuntimeHealthProbe {
    Health probe(DeploymentCellManifest.Instance instance,String healthPath,int timeoutSeconds);
    default Health smoke(DeploymentCellManifest.Instance instance,int timeoutSeconds){return probe(instance,"/internal/v1/runtime/smoke",timeoutSeconds);}
    record Health(boolean live,boolean ready,String detail){}
}
