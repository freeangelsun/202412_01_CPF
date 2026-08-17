package com.cpf.security.internal.password;

import com.cpf.security.api.CpfPasswordRuntimePolicy;

import com.cpf.security.api.password.CpfPasswordEncoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Public Password API adapter이며 신규 hash 전에 Runtime Complexity Policy를 집행합니다. */
@Component
public class CpfPasswordEncoderAdapter implements CpfPasswordEncoder {
    private final CpfPasswordHashingPort delegate;
    private final CpfPasswordRuntimePolicy runtimePolicy;

    /** 기존 직접 생성 테스트 호환입니다. */
    public CpfPasswordEncoderAdapter(CpfPasswordHashingPort delegate) {
        this(delegate, new CpfPasswordRuntimePolicy());
    }

    @Autowired
    public CpfPasswordEncoderAdapter(
            CpfPasswordHashingPort delegate,
            ObjectProvider<CpfPasswordRuntimePolicy> runtimePolicyProvider) {
        this(delegate, runtimePolicyProvider.getIfAvailable(CpfPasswordRuntimePolicy::new));
    }

    private CpfPasswordEncoderAdapter(CpfPasswordHashingPort delegate, CpfPasswordRuntimePolicy runtimePolicy) {
        this.delegate = delegate;
        this.runtimePolicy = runtimePolicy;
    }

    @Override
    public String encode(char[] rawPassword) {
        runtimePolicy.validate(rawPassword);
        return delegate.hash(rawPassword);
    }

    @Override public boolean matches(char[] rawPassword, String encodedPassword) { return delegate.verify(rawPassword, encodedPassword).matched(); }
    @Override public boolean upgradeEncoding(String encodedPassword) { return delegate.needsRehash(encodedPassword); }
    @Override public String algorithmId() { return delegate.algorithmId(); }
}
