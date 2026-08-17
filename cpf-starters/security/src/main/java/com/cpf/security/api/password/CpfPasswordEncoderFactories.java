package com.cpf.security.api.password;

import com.cpf.security.internal.password.CpfPasswordEncoderAdapter;
import com.cpf.security.internal.password.CpfPbkdf2PasswordHasher;

/**
 * 단위테스트·독립 Runtime이 Core 내부 해시 구현 클래스를 직접 참조하지 않고
 * CPF 표준 비밀번호 서비스를 생성하는 공개 factory입니다.
 */
public final class CpfPasswordEncoderFactories {
    private CpfPasswordEncoderFactories() {
    }

    public static CpfPasswordEncoder pbkdf2(
            int iterations,
            int keyLengthBits,
            char[] pepper) {
        return new CpfPasswordEncoderAdapter(
                new CpfPbkdf2PasswordHasher(iterations, keyLengthBits, pepper));
    }
}
