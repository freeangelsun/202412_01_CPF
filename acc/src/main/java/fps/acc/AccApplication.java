package fps.acc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ACC 모듈의 메인 애플리케이션 진입점 클래스입니다.
 * Spring Boot 3.0 기반으로 실행되며, CMN 공통 모듈과 ACC 모듈을 함께 스캔하여 컴포넌트를 등록합니다.
 */
@SpringBootApplication(scanBasePackages = {"fps.pfw", "fps.cmn", "fps.acc"})
public class AccApplication {

	/**
	 * 애플리케이션 시작 메서드
	 * @param args SpringApplication에 전달되는 실행 인자
	 */
	public static void main(String[] args) {
		SpringApplication.run(AccApplication.class, args);
	}

}
