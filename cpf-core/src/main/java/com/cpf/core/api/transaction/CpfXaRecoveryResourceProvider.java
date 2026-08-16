package com.cpf.core.api.transaction;

import java.util.List;

/**
 * XA 복구 스캔 시 Resource Manager에 다시 연결할 수 있도록 XAResource를 제공하는 계약입니다.
 * 구현체는 특정 JTA Transaction Manager API를 노출하지 않아야 합니다.
 */
public interface CpfXaRecoveryResourceProvider {
    /** 운영 로그/ADM에서 식별할 안정적인 Resource Manager 이름입니다. */
    String resourceId();
    /** 각 복구 sweep에서 사용할 XAResource 목록입니다. */
    List<CpfXaResourceHandle> recoveryResources();
}
