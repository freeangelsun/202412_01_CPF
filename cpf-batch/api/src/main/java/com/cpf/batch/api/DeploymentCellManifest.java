package com.cpf.batch.api;
import java.util.*;
public record DeploymentCellManifest(String cellId,String environment,RuntimeRole runtimeRole,String serviceId,ArtifactManifest artifact,String runtimeMode,List<Instance> instances,DesiredState desiredState,DeploymentPolicy deployment,List<String> dependencies,List<String> secretReferences,Map<String,String> labels) {
 public DeploymentCellManifest { if(cellId==null||cellId.isBlank()||runtimeRole==null||artifact==null)throw new IllegalArgumentException("cell/role/artifact"); instances=instances==null?List.of():List.copyOf(instances); if(instances.isEmpty())throw new IllegalArgumentException("instances"); dependencies=dependencies==null?List.of():List.copyOf(dependencies); secretReferences=secretReferences==null?List.of():List.copyOf(secretReferences); labels=labels==null?Map.of():Map.copyOf(labels); }
 public record Instance(String instanceId,String wasId,String hostAlias,int port,String profile,String zone,String pool,List<String> capability,String agentBaseUrl,String configRef){}
 public record DeploymentPolicy(DeploymentStrategy strategy,int minHealthy,int maxUnavailable,String healthPath,int drainTimeoutSeconds,int healthTimeoutSeconds,String rollbackVersion,boolean functionalSmokeRequired){}
}
