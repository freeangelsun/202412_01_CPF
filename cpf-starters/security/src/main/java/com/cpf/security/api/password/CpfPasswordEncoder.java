package com.cpf.security.api.password;
/** Spring Security PasswordEncoder naming/return semantics를 따르는 CPF 공개 계약입니다. */
public interface CpfPasswordEncoder {
    String encode(char[] rawPassword);
    boolean matches(char[] rawPassword,String encodedPassword);
    boolean upgradeEncoding(String encodedPassword);
    String algorithmId();
}
