package com.cpf.core.common.saga;
/** Durable Saga state. Legacy status names are retained for binary/data compatibility. */
public enum CpfSagaStatus {
    STARTED, RUNNING, COMPLETED, FAILED, COMPENSATING, COMPENSATED, UNKNOWN, MANUAL_REVIEW,
    /** @deprecated use MANUAL_REVIEW. */ @Deprecated MANUAL_INTERVENTION_REQUIRED,
    /** Final operator-resolved state retained for existing ADM data. */ MANUALLY_RESOLVED,
    /** Compensation-specific legacy failure state retained for existing data. */ COMPENSATION_FAILED
}
