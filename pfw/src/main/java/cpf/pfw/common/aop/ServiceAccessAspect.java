package cpf.pfw.common.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
@Aspect
@Component
public class ServiceAccessAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceAccessAspect.class);

    /**
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @Before("within(cpf..service..*) && !within(cpf.cmn.service..*) && !within(cpf.pfw.service..*)")
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
            logger.error("CPF 처리 기준입니다.");
            throw new SecurityException("Direct service access is not allowed. Use standard web entry points.");
        }
    }
}

