package com.cpf.core.common.security.password;

import com.cpf.core.api.security.password.CpfPasswordService;
import com.cpf.core.api.security.password.CpfPasswordVerification;
import org.springframework.stereotype.Component;

/**
 * 기존 내부 Password Hashing Port를 Public API로 노출하는 단일 adapter입니다.
 * 업무 모듈이 내부 저장 포맷/알고리즘 계약에 결합되지 않도록 경계를 수렴합니다.
 */
@Component
public class CpfPasswordServiceAdapter implements CpfPasswordService {
    private final CpfPasswordHashingPort delegate;
    public CpfPasswordServiceAdapter(CpfPasswordHashingPort delegate) { this.delegate = delegate; }
    @Override public String hash(char[] rawPassword) { return delegate.hash(rawPassword); }
    @Override public CpfPasswordVerification verify(char[] rawPassword, String encodedPassword) {
        com.cpf.core.common.security.password.CpfPasswordVerification result = delegate.verify(rawPassword, encodedPassword);
        return new CpfPasswordVerification(result.matched(), result.rehashRequired());
    }
    @Override public boolean needsRehash(String encodedPassword) { return delegate.needsRehash(encodedPassword); }
    @Override public String algorithmId() { return delegate.algorithmId(); }
}
