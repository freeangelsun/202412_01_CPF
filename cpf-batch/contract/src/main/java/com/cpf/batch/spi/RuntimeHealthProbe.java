package com.cpf.batch.spi;
import com.cpf.batch.api.DeploymentCellManifest;
public interface RuntimeHealthProbe {
    Health probe(DeploymentCellManifest.Instance instance,String healthPath,int timeoutSeconds);
    default Health smoke(DeploymentCellManifest.Instance instance,int timeoutSeconds){return probe(instance,"/internal/v1/runtime/smoke",timeoutSeconds);}
    record Health(boolean live,boolean ready,String detail){}
}
