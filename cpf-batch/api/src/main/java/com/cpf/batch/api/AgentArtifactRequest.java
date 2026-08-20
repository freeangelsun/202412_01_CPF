package com.cpf.batch.api;
/**
 * Control Plane이 Batch Agent에 Artifact 준비/배포를 요청할 때 사용하는 공개 계약입니다.
 * <p>checksum/signature와 release sequence를 함께 전달해 stale 또는 변조 Artifact 적용을 차단합니다.
 */
public record AgentArtifactRequest(String serviceId,String coordinate,String version,String sha256,String signatureBase64,String runtimeMode,String configRef,String requestedBy,String reason,long releaseSequence,String environmentCode,String channel,String keyId){
 public AgentArtifactRequest(String serviceId,String coordinate,String version,String sha256,String signatureBase64,String runtimeMode,String configRef,String requestedBy,String reason){this(serviceId,coordinate,version,sha256,signatureBase64,runtimeMode,configRef,requestedBy,reason,0L,"","","");}
}
