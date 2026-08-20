package com.cpf.batch.api;
import java.time.Instant; import java.util.List;
/**
 * Batch 배포 Artifact의 무결성·호환성·공급망 정보를 고정하는 공개 Manifest입니다.
 * <p>Agent/Control Plane은 coordinate와 version뿐 아니라 checksum, provenance, API/Schema 호환성을 함께 검증해야 합니다.
 *
 * @param coordinate Maven/배포 좌표
 * @param version immutable release version
 * @param sha256 artifact SHA-256
 * @param signatureBase64 선택적 서명 값
 * @param sbomRef SBOM 참조
 * @param provenanceRef provenance 참조
 * @param gitSha 빌드 Source identity
 * @param javaVersion 빌드 Java version
 * @param springVersion Spring stack version
 * @param cpfPlatformVersion CPF platform version
 * @param publicApiVersion Public API compatibility version
 * @param schemaCompatibility DB/metadata compatibility marker
 * @param requiredCapabilities 기동에 필요한 capability 목록
 * @param builtAt artifact 생성 시각
 */
public record ArtifactManifest(String coordinate,String version,String sha256,String signatureBase64,String sbomRef,String provenanceRef,String gitSha,String javaVersion,String springVersion,String cpfPlatformVersion,String publicApiVersion,String schemaCompatibility,List<String> requiredCapabilities,Instant builtAt) {
 public ArtifactManifest { if(coordinate==null||coordinate.isBlank())throw new IllegalArgumentException("coordinate"); if(version==null||version.isBlank())throw new IllegalArgumentException("version"); if(sha256==null||!sha256.matches("(?i)[0-9a-f]{64}"))throw new IllegalArgumentException("sha256"); requiredCapabilities=requiredCapabilities==null?List.of():List.copyOf(requiredCapabilities); }
}
