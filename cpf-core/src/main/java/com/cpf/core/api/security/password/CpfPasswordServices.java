package com.cpf.core.api.security.password;

import com.cpf.core.common.security.password.CpfPasswordServiceAdapter;
import com.cpf.core.common.security.password.CpfPbkdf2PasswordHasher;

/**
 * 단위테스트·독립 Runtime이 Core 내부 해시 구현 클래스를 직접 참조하지 않고
 * CPF 표준 비밀번호 서비스를 생성하는 공개 factory입니다.
 */
public final class CpfPasswordServices {
    private CpfPasswordServices() {
    }

    public static CpfPasswordService pbkdf2(
            int iterations,
            int keyLengthBits,
            char[] pepper) {
        return new CpfPasswordServiceAdapter(
                new CpfPbkdf2PasswordHasher(iterations, keyLengthBits, pepper));
    }
}
