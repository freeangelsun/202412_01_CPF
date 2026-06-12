package fps.xyz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * XYZ 교육용 주제영역 애플리케이션 진입점입니다.
 *
 * <p>XYZ는 실제 업무 주제영역이 아니라 신규 개발자가 FPS 프레임워크 사용법을 익히는
 * 샘플/실습 전용 서비스입니다. PFW와 CMN을 함께 스캔해 거래 로그, 표준 예외, 캐시,
 * 트랜잭션, 동적 로그레벨 같은 공통 기능을 한 곳에서 확인할 수 있게 합니다.</p>
 */
@SpringBootApplication(scanBasePackages = {"fps.pfw", "fps.cmn", "fps.xyz"})
public class XyzApplication {

    /**
     * Spring Boot 애플리케이션을 시작합니다.
     *
     * @param args 실행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(XyzApplication.class, args);
    }
}
