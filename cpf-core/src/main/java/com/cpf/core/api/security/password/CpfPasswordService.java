package com.cpf.core.api.security.password;

/**
 * 업무/운영 모듈이 CPF 내부 해시 구현에 직접 의존하지 않고 사용하는 공개 비밀번호 서비스 계약입니다.
 */
public interface CpfPasswordService {
    String hash(char[] rawPassword);
    CpfPasswordVerification verify(char[] rawPassword, String encodedPassword);
    boolean needsRehash(String encodedPassword);
    String algorithmId();
}
