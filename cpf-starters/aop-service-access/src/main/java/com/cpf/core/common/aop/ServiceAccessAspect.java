package com.cpf.core.common.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 서비스 계층이 허용된 진입점 밖에서 직접 호출되는 것을 차단합니다.
 *
 * <p>Controller, Filter, Interceptor를 통한 표준 진입 흐름만 허용하여
 * 인증·거래 헤더·감사 로깅 같은 공통 처리가 우회되지 않도록 보호합니다.</p>
 */
@Aspect
@Component
public class ServiceAccessAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceAccessAspect.class);

    /** 호출 스택을 검사해 허용된 웹 진입점이 없는 서비스 직접 호출을 거부합니다. */
    @Before("within(com.cpf..service..*) && !within(com.cpf.common.service..*) && !within(com.cpf.core.service..*)")
    public void preventDirectServiceAccess() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        boolean isAllowedEntryPoint = false;

        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.contains(".controller.")
                    || className.contains(".filter.")
                    || className.contains(".interceptor.")) {
                isAllowedEntryPoint = true;
                break;
            }
        }

        if (!isAllowedEntryPoint) {
            logger.error("표준 진입점을 거치지 않은 서비스 직접 호출을 차단했습니다.");
            throw new SecurityException("Direct service access is not allowed. Use standard web entry points.");
        }
    }
}

