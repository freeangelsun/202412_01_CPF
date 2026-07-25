package com.cpf.core.api.product;

import java.util.Set;

/**
 * 고객/Edition별 Capability 허용 범위를 공급하는 선택 SPI.
 * Core Framework는 특정 라이선스 서버나 상용 정책에 의존하지 않습니다.
 */
@FunctionalInterface
public interface CpfCapabilityLicenseProvider {
    Set<CpfCapability> licensedCapabilities();
}
