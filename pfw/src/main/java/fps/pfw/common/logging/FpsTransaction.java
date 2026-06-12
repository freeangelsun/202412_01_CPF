package fps.pfw.common.logging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 또는 컨트롤러 클래스에 업무 거래ID와 거래명을 선언하는 PFW 표준 어노테이션입니다.
 *
 * <p>PFW 로그 AOP는 이 어노테이션을 읽어 TRAN_LOG의
 * {@code BUSINESS_TRANSACTION_ID}, {@code BUSINESS_TRANSACTION_NAME} 컬럼에 저장합니다.
 * 개발자는 신규 API를 만들 때 반드시 업무 거래ID와 사람이 읽을 수 있는 거래명을 함께 선언해야 합니다.</p>
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FpsTransaction {
    /**
     * 업무 거래ID입니다.
     * 형식: {주제영역3}{거래유형2}{중간도메인3}{일련번호4}
     * 예: MBR01BSE0001
     */
    String id();

    /**
     * 로그와 관리 화면에 표시할 업무 거래명입니다.
     */
    String name();
}
