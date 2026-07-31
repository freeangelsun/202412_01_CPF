package com.cpf.batch.api;
public record AgentArtifactRequest(String serviceId,String coordinate,String version,String sha256,String signatureBase64,String runtimeMode,String configRef,String requestedBy,String reason,long releaseSequence,String environmentCode,String channel,String keyId){
 public AgentArtifactRequest(String serviceId,String coordinate,String version,String sha256,String signatureBase64,String runtimeMode,String configRef,String requestedBy,String reason){this(serviceId,coordinate,version,sha256,signatureBase64,runtimeMode,configRef,requestedBy,reason,0L,"","","");}
}
