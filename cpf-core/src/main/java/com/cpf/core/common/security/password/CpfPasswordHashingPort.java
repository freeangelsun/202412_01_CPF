package com.cpf.core.common.security.password;

/**
 * 비밀번호 저장 해시 생성과 검증을 제공하는 CPF 보안 port입니다.
 *
 * <p>업무 모듈은 알고리즘과 저장 형식을 직접 구현하지 않고 이 port만 사용합니다.</p>
 */
public interface CpfPasswordHashingPort {

    String hash(char[] rawPassword);

    CpfPasswordVerification verify(char[] rawPassword, String encodedPassword);

    boolean needsRehash(String encodedPassword);

    String algorithmId();
}
