package com.cpf.core.spi.resilience;

/** Classifies retry eligibility and ambiguous outcomes without exposing OSS exception types. */
public interface CpfResilienceFailureClassifier {
    Classification classify(Throwable failure);
    enum Classification { RETRYABLE, NON_RETRYABLE, UNKNOWN_RESULT }
}
