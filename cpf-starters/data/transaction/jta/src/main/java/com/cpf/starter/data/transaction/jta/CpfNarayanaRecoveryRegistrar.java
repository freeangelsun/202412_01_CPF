package com.cpf.starter.data.transaction.jta;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import com.cpf.core.api.transaction.CpfXaRecoveryResourceProvider;
import java.util.List;
import java.util.Objects;
import javax.transaction.xa.XAResource;
import org.springframework.beans.factory.DisposableBean;

/**
 * CPF의 provider-neutral recovery provider를 Narayana 동적 recovery helper에 등록합니다.
 * WAS managed TM 환경에서는 이 bean을 만들지 않고 WAS의 Resource Recovery를 사용합니다.
 */
final class CpfNarayanaRecoveryRegistrar implements DisposableBean, AutoCloseable {
    private final XARecoveryModule module;
    private final List<XAResourceRecoveryHelper> helpers;

    CpfNarayanaRecoveryRegistrar(List<CpfXaRecoveryResourceProvider> providers) {
        Objects.requireNonNull(providers, "providers");
        this.module = XARecoveryModule.getRegisteredXARecoveryModule();
        if (module == null && !providers.isEmpty()) {
            throw new IllegalStateException("Narayana XARecoveryModule이 활성화되지 않아 XA recovery resource를 등록할 수 없습니다.");
        }
        this.helpers = providers.stream().map(CpfNarayanaRecoveryRegistrar::helper).toList();
        if (module != null) helpers.forEach(module::addXAResourceRecoveryHelper);
    }

    private static XAResourceRecoveryHelper helper(CpfXaRecoveryResourceProvider provider) {
        return new XAResourceRecoveryHelper() {
            @Override public boolean initialise(String ignored) { return true; }
            @Override public XAResource[] getXAResources() {
                return provider.recoveryResources().stream().map(handle -> handle.resource()).toArray(XAResource[]::new);
            }
            @Override public String toString() { return "CPF-XA-Recovery[" + provider.resourceId() + "]"; }
        };
    }

    @Override public void close() { destroy(); }

    @Override public void destroy() {
        if (module != null) helpers.forEach(module::removeXAResourceRecoveryHelper);
    }
}
