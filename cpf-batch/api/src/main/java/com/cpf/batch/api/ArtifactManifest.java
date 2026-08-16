package com.cpf.batch.api;
import java.time.Instant; import java.util.List;
public record ArtifactManifest(String coordinate,String version,String sha256,String signatureBase64,String sbomRef,String provenanceRef,String gitSha,String javaVersion,String springVersion,String cpfPlatformVersion,String publicApiVersion,String schemaCompatibility,List<String> requiredCapabilities,Instant builtAt) {
 public ArtifactManifest { if(coordinate==null||coordinate.isBlank())throw new IllegalArgumentException("coordinate"); if(version==null||version.isBlank())throw new IllegalArgumentException("version"); if(sha256==null||!sha256.matches("(?i)[0-9a-f]{64}"))throw new IllegalArgumentException("sha256"); requiredCapabilities=requiredCapabilities==null?List.of():List.copyOf(requiredCapabilities); }
}
