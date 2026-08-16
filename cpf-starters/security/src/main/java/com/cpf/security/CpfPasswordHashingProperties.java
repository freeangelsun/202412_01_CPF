package com.cpf.security;

import com.cpf.security.internal.password.CpfPbkdf2PasswordHasher;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CPF 비밀번호 해시 Runtime 설정입니다.
 *
 * <p>반복 횟수와 키 길이는 일반 설정이며 pepper 자체는 설정 파일에 두지 않습니다.
 * {@code pepperEnvironmentVariable}에는 Secret을 담은 환경변수 이름만 지정합니다.</p>
 */
@ConfigurationProperties(prefix = "cpf.security.password-hashing")
public class CpfPasswordHashingProperties {
    private boolean enabled = true;
    private int iterations = CpfPbkdf2PasswordHasher.DEFAULT_ITERATIONS;
    private int keyLengthBits = CpfPbkdf2PasswordHasher.DEFAULT_KEY_BITS;
    private String pepperEnvironmentVariable = "CPF_PASSWORD_PEPPER";
    /** 운영 프로파일에서 pepper Secret 누락을 허용하지 않을지 결정하며 기본값은 프로파일 정책이 보완한다. */
    private boolean requirePepper;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getIterations() { return iterations; }
    public void setIterations(int iterations) { this.iterations = iterations; }
    public int getKeyLengthBits() { return keyLengthBits; }
    public void setKeyLengthBits(int keyLengthBits) { this.keyLengthBits = keyLengthBits; }
    public String getPepperEnvironmentVariable() { return pepperEnvironmentVariable; }
    public void setPepperEnvironmentVariable(String pepperEnvironmentVariable) { this.pepperEnvironmentVariable = pepperEnvironmentVariable; }
    public boolean isRequirePepper() { return requirePepper; }
    public void setRequirePepper(boolean requirePepper) { this.requirePepper = requirePepper; }

    void validate() {
        if (iterations < 210_000) throw new IllegalArgumentException("cpf.security.password-hashing.iterations must be >= 210000");
        if (keyLengthBits < 256) throw new IllegalArgumentException("cpf.security.password-hashing.key-length-bits must be >= 256");
        if (pepperEnvironmentVariable == null || pepperEnvironmentVariable.isBlank()) {
            throw new IllegalArgumentException("cpf.security.password-hashing.pepper-environment-variable is required");
        }
    }
}
