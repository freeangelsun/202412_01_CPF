package com.cpf.integration.resilience.runtime;

import com.cpf.integration.resilience.spi.CpfResilienceFailureClassifier;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

/** Provider 예외 타입을 외부에 노출하지 않는 보수적 기본 실패 분류기입니다. */
public final class CpfDefaultResilienceFailureClassifier implements CpfResilienceFailureClassifier {
    @Override public Classification classify(Throwable failure) {
        Throwable root = root(failure);
        if (root instanceof SocketTimeoutException || root instanceof TimeoutException) return Classification.UNKNOWN_RESULT;
        if (root instanceof ConnectException || root instanceof IOException) return Classification.RETRYABLE;
        return Classification.NON_RETRYABLE;
    }
    private static Throwable root(Throwable t){while(t.getCause()!=null&&t.getCause()!=t)t=t.getCause();return t;}
}
