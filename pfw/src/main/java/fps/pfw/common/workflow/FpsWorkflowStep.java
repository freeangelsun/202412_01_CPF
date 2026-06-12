package fps.pfw.common.workflow;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 워크플로우 안에서 실제 수행되는 한 단계의 거래를 선언합니다.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface FpsWorkflowStep {
    /**
     * 워크플로우 안에서 현재 거래를 구분하는 스텝 ID입니다.
     * 생략하면 PFW 로그 AOP가 현재 {@code @FpsTransaction.id}를 스텝 ID로 사용합니다.
     */
    String id() default "";

    /**
     * 로그 화면에 표시할 스텝 논리명입니다.
     * 생략하면 PFW 로그 AOP가 현재 {@code @FpsTransaction.name}을 스텝명으로 사용합니다.
     */
    String name() default "";

    /**
     * 이 스텝이 실패했을 때 운영적으로 어떤 상태로 남길지 결정하는 정책입니다.
     */
    FpsWorkflowFailurePolicy failurePolicy() default FpsWorkflowFailurePolicy.FAIL;

    /**
     * true이면 이 거래 자체가 보상 거래임을 의미합니다.
     */
    boolean compensation() default false;

    /**
     * 이 단계 실패 시 호출하거나 대기시킬 보상 거래ID입니다.
     */
    String compensationTransactionId() default "";

    /**
     * 보상 거래인 경우 어떤 원거래를 보상하는지 기록합니다.
     */
    String compensationTargetTransactionId() default "";
}
