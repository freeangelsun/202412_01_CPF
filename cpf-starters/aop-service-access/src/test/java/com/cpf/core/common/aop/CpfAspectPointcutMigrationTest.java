package com.cpf.core.common.aop;

import com.cpf.core.common.logging.LoggingAspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/** 모듈 패키지 전환 후에도 핵심 AOP가 공식 {@code com.cpf} 루트를 가리키는지 검증합니다. */
class CpfAspectPointcutMigrationTest {

    /** 거래 로깅 pointcut이 이전 Java 루트가 아닌 공식 패키지 전체를 대상으로 하는지 확인합니다. */
    @Test
    void loggingAspectTargetsOfficialControllerPackages() throws NoSuchMethodException {
        Method method = LoggingAspect.class.getMethod("logTransaction", org.aspectj.lang.ProceedingJoinPoint.class);
        String expression = method.getAnnotation(Around.class).value();

        assertThat(expression).contains("com.cpf..*Controller");
        assertThat(expression).doesNotContain("execution(* " + "cpf" + "..");
    }

    /** 서비스 접근 보호 pointcut도 공식 패키지 루트에 연결되는지 확인합니다. */
    @Test
    void serviceAccessAspectTargetsOfficialServicePackages() throws NoSuchMethodException {
        Method method = ServiceAccessAspect.class.getMethod("preventDirectServiceAccess");
        String expression = method.getAnnotation(Before.class).value();

        assertThat(expression).startsWith("within(com.cpf..service..*)");
        assertThat(expression).doesNotContain("within(" + "cpf" + "..");
    }
}
