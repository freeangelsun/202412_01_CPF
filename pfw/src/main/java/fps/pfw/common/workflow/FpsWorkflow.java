package fps.pfw.common.workflow;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 여러 주제영역을 통과하는 하나의 업무 흐름을 선언합니다.
 * 값이 없으면 상위 서비스에서 전달한 워크플로우 헤더를 이어받습니다.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface FpsWorkflow {
    /**
     * 워크플로우 ID입니다.
     * 같은 업무 흐름을 여러 주제영역에서 동일하게 추적할 때 사용합니다.
     */
    String id() default "";

    /**
     * 로그 화면과 운영 문서에 표시할 워크플로우 논리명입니다.
     */
    String name() default "";
}
