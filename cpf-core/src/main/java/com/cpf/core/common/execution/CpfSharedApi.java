package cpf.pfw.common.execution;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CPF 주제영역 간 공유 API에 선언하는 내부 전용 실행 메타데이터입니다.
 *
 * <p>이 annotation이 붙은 실행점은 CPF 서비스 신원과 신뢰된 내부 ingress를 검증해야 하며
 * 공개 Gateway route catalog에는 포함하지 않습니다.</p>
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfSharedApi {
    String id();

    String name();

    String ownerDomain() default "";

    String description() default "";

    String requiredPermission() default "";

    boolean auditReasonRequired() default false;

    /**
     * 이 공유 API를 호출할 수 있는 CPF 서비스 ID 목록입니다.
     * 빈 목록은 신원 검증을 통과한 모든 CPF 내부 서비스를 의미합니다.
     */
    String[] allowedCallers() default {};
}
