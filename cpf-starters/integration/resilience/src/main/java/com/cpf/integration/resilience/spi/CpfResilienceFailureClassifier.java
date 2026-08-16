package com.cpf.integration.resilience.spi;

/** Classifies retry eligibility and ambiguous outcomes without exposing OSS exception types. */
/** CpfResilienceFailureClassifier 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfResilienceFailureClassifier {
    Classification classify(Throwable failure);
    enum Classification { RETRYABLE, NON_RETRYABLE, UNKNOWN_RESULT }
}
