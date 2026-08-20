package com.cpf.batch.api;
import java.util.*;
/**
 * Batch Runtime Cell의 배포 목표와 Instance 구성을 불변 입력으로 전달하는 공개 Manifest입니다.
 * <p>Control Plane과 Agent가 동일 배포 계획을 공유하기 위한 계약이며 Secret 원문 대신 reference만 포함합니다.
 *
 * @param cellId 배포 Cell 식별자
 * @param environment 환경 식별자
 * @param runtimeRole Runtime 역할
 * @param serviceId 서비스 식별자
 * @param artifact 배포 Artifact Manifest
 * @param runtimeMode Runtime mode
 * @param instances 목표 Instance 목록
 * @param desiredState 목표 상태
 * @param deployment 배포 전략/Health 정책
 * @param dependencies 선행 서비스 의존 목록
 * @param secretReferences Secret reference 목록
 * @param labels 운영/배포 metadata
 */
public record DeploymentCellManifest(String cellId,String environment,RuntimeRole runtimeRole,String serviceId,ArtifactManifest artifact,String runtimeMode,List<Instance> instances,DesiredState desiredState,DeploymentPolicy deployment,List<String> dependencies,List<String> secretReferences,Map<String,String> labels) {
 public DeploymentCellManifest { if(cellId==null||cellId.isBlank()||runtimeRole==null||artifact==null)throw new IllegalArgumentException("cell/role/artifact"); instances=instances==null?List.of():List.copyOf(instances); if(instances.isEmpty())throw new IllegalArgumentException("instances"); dependencies=dependencies==null?List.of():List.copyOf(dependencies); secretReferences=secretReferences==null?List.of():List.copyOf(secretReferences); labels=labels==null?Map.of():Map.copyOf(labels); }
 /** Deployment Cell을 구성하는 개별 Runtime Instance의 배치/Port/Profile 정보를 나타냅니다. */
 public record Instance(String instanceId,String wasId,String hostAlias,int port,String profile,String zone,String pool,List<String> capability,String agentBaseUrl,String configRef){}
 /** Cell rollout 시 healthy 수, timeout, rollback version, smoke 정책을 정의합니다. */
 public record DeploymentPolicy(DeploymentStrategy strategy,int minHealthy,int maxUnavailable,String healthPath,int drainTimeoutSeconds,int healthTimeoutSeconds,String rollbackVersion,boolean functionalSmokeRequired){}
}
