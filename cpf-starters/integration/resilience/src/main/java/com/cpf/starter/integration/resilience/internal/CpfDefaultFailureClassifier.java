package com.cpf.starter.integration.resilience.internal;

import com.cpf.core.spi.resilience.CpfResilienceFailureClassifier;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

/** Conservative classifier: ambiguous transport completion is UNKNOWN_RESULT. */
public final class CpfDefaultFailureClassifier implements CpfResilienceFailureClassifier {
    @Override public Classification classify(Throwable failure) {
        if (failure instanceof SocketTimeoutException || failure instanceof TimeoutException) return Classification.UNKNOWN_RESULT;
        if (failure instanceof IOException) return Classification.RETRYABLE;
        return Classification.NON_RETRYABLE;
    }
}
