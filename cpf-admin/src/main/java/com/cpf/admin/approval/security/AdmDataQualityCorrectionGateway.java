package com.cpf.admin.approval.security;

import com.cpf.core.api.data.quality.CpfDataQualityOperations;
import com.cpf.core.spi.data.quality.CpfDataQualityCorrectionPort;

import java.util.Objects;

/**
 * ADM-owned final security boundary for approved data-quality correction.
 *
 * <p>The ADM approval flow calls this gateway instead of a provider directly. Capability proof
 * verification and durable single-use consumption therefore happen before every provider mutation
 * and cannot be omitted by a provider implementation.</p>
 */
public final class AdmDataQualityCorrectionGateway {
    @FunctionalInterface
    public interface ApprovedCorrectionVerifier {
        void verifyAndConsume(CpfDataQualityCorrectionPort.ApprovedCorrection command);
    }

    private final CpfDataQualityCorrectionPort delegate;
    private final ApprovedCorrectionVerifier verifier;

    public AdmDataQualityCorrectionGateway(
            CpfDataQualityCorrectionPort delegate,
            ApprovedCorrectionVerifier verifier) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.verifier = Objects.requireNonNull(verifier, "verifier");
    }

    public CpfDataQualityOperations.QuarantineItem correctApproved(
            CpfDataQualityCorrectionPort.ApprovedCorrection command) {
        if (command == null) throw new SecurityException("approved correction command is required");
        verifier.verifyAndConsume(command);
        return delegate.correctApproved(command);
    }
}
