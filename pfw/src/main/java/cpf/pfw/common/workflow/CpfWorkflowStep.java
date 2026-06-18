package cpf.pfw.common.workflow;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CPF 기능 설명입니다.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CpfWorkflowStep {
    /**
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    String id() default "";

    /**
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    String name() default "";

    /**
     * CPF 기능 설명입니다.
     */
    CpfWorkflowFailurePolicy failurePolicy() default CpfWorkflowFailurePolicy.FAIL;

    /**
     * CPF 기능 설명입니다.
     */
    boolean compensation() default false;

    /**
     * CPF 기능 설명입니다.
     */
    String compensationTransactionId() default "";

    /**
     * CPF 기능 설명입니다.
     */
    String compensationTargetTransactionId() default "";
}

