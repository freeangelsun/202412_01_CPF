package com.cpf.backoffice.online;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MBW Backoffice 업무 Domain의 Online 실행 진입점입니다.
 *
 * <p>Generated Business Domain과 동일하게 애플리케이션 Root package 아래의 업무 Feature만 스캔하고,
 * CPF Framework 기능은 Public Starter의 AutoConfiguration으로 조립합니다.</p>
 */
@SpringBootApplication
public class BackofficeOnlineApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackofficeOnlineApplication.class, args);
    }
}
