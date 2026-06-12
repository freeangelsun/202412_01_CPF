package fps.pfw.common.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 서브 모듈 간 서비스 직접 호출을 방지하고 컨트롤러 진입을 강제하는 프레임워크 정책 AOP입니다.
 * CMN 개발 공통 서비스와 PFW 프레임워크 내부 서비스는 공통 인프라이므로 예외로 둡니다.
 */
@Aspect
@Component
public class ServiceAccessAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceAccessAspect.class);

    /**
     * 업무 서비스는 컨트롤러를 통해서만 진입하도록 제한합니다.
     * 프레임워크 내부 로그 저장 서비스는 이벤트 리스너에서 호출되므로 예외 처리합니다.
     */
    @Before("within(fps..service..*) && !within(fps.cmn.service..*) && !within(fps.pfw.service..*)")
    public void preventDirectServiceAccess() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        boolean isControllerCall = false;

        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains(".controller.")) {
                isControllerCall = true;
                break;
            }
        }

        if (!isControllerCall) {
            logger.error("서비스 직접 호출이 감지되었습니다. 컨트롤러를 통해서만 접근 가능합니다.");
            throw new SecurityException("Direct service access is not allowed. Use controllers to access services.");
        }
    }
}
