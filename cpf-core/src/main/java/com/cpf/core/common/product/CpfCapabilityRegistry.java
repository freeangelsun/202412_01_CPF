package com.cpf.core.common.product;

import com.cpf.core.api.product.CpfCapability;
import com.cpf.core.api.product.CpfCapabilityLicenseProvider;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Edition 문자열 분기를 Runtime 전역에 퍼뜨리지 않는 Capability Registry.
 * 설치된 기능과 라이선스 허용 기능의 교집합만 활성 Capability로 노출합니다.
 */
public final class CpfCapabilityRegistry {
    private final Set<CpfCapability> installed;
    private final CpfCapabilityLicenseProvider licenseProvider;

    public CpfCapabilityRegistry(Set<CpfCapability> installed, CpfCapabilityLicenseProvider licenseProvider) {
        this.installed = installed == null || installed.isEmpty()
                ? EnumSet.of(CpfCapability.CORE_RUNTIME)
                : EnumSet.copyOf(installed);
        this.licenseProvider = licenseProvider == null ? () -> EnumSet.allOf(CpfCapability.class) : licenseProvider;
    }

    public Set<CpfCapability> activeCapabilities() {
        EnumSet<CpfCapability> result = EnumSet.copyOf(installed);
        Set<CpfCapability> licensed = Objects.requireNonNullElseGet(licenseProvider.licensedCapabilities(), Set::of);
        result.retainAll(licensed);
        return Set.copyOf(result);
    }

    public boolean enabled(CpfCapability capability) { return activeCapabilities().contains(capability); }

    public void require(CpfCapability capability) {
        if (!enabled(capability)) throw new IllegalStateException("CPF capability is not enabled: " + capability);
    }
}
