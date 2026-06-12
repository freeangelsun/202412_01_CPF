package fps.mbr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 회원 관리 모듈 Spring Boot 애플리케이션 진입점
 * 
 * 주요 특징:
 * - 금융권 표준 REST API 제공
 * - 통일된 응답/에러 처리
 * - AOP 기반 자동 로깅
 * - 입력값 검증
 * - 감시(Audit) 기능 내장
 * 
 * 프로파일:
 * - local: 로컬 개발 환경 (MariaDB localhost:3306)
 * - dev: 개발 서버
 * - prod: 운영 서버
 * 
 * 실행 방법:
 *   ./gradlew :mbr:bootRun --args='--spring.profiles.active=local'
 * 
 * @author FPS Team
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"fps.pfw", "fps.cmn", "fps.mbr"})
public class MbrApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MbrApplication.class, args);
    }
}
