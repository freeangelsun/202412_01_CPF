package com.cpf.core.common.security.password;

import com.cpf.core.api.security.password.CpfPasswordService;
import com.cpf.core.api.security.password.CpfPasswordVerification;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Public Password API adapter이며 신규 hash 전에 Runtime Complexity Policy를 집행합니다. */
@Component
public class CpfPasswordServiceAdapter implements CpfPasswordService {
    private final CpfPasswordHashingPort delegate;
    private final CpfPasswordRuntimePolicy runtimePolicy;

    /** 기존 직접 생성 테스트 호환입니다. */
    public CpfPasswordServiceAdapter(CpfPasswordHashingPort delegate) {
        this(delegate, new CpfPasswordRuntimePolicy());
    }

    @Autowired
    public CpfPasswordServiceAdapter(
            CpfPasswordHashingPort delegate,
            ObjectProvider<CpfPasswordRuntimePolicy> runtimePolicyProvider) {
        this(delegate, runtimePolicyProvider.getIfAvailable(CpfPasswordRuntimePolicy::new));
    }

    private CpfPasswordServiceAdapter(CpfPasswordHashingPort delegate, CpfPasswordRuntimePolicy runtimePolicy) {
        this.delegate = delegate;
        this.runtimePolicy = runtimePolicy;
    }

    @Override
    public String hash(char[] rawPassword) {
        runtimePolicy.validate(rawPassword);
        return delegate.hash(rawPassword);
    }

    @Override public CpfPasswordVerification verify(char[] rawPassword, String encodedPassword) {
        com.cpf.core.common.security.password.CpfPasswordVerification result = delegate.verify(rawPassword, encodedPassword);
        return new CpfPasswordVerification(result.matched(), result.rehashRequired());
    }
    @Override public boolean needsRehash(String encodedPassword) { return delegate.needsRehash(encodedPassword); }
    @Override public String algorithmId() { return delegate.algorithmId(); }
}
