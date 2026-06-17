package cpf.xyz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * XYZ 교육 샘플 애플리케이션입니다.
 *
 * <p>XYZ는 신규 개발자가 CPF의 PFW, CMN, 업무 모듈 작성법을 실제 API로 따라 해볼 수 있도록 만든 교육용 모듈입니다.
 * 운영 업무를 담는 모듈이 아니라 CRUD, 거래 헤더, 예외, 배치, 메시징, 파일, 보안 같은 표준 사용법을 보여주는 역할을 합니다.</p>
 */
@SpringBootApplication(scanBasePackages = {"cpf.pfw", "cpf.cmn", "cpf.xyz"})
public class XyzApplication {

    /**
     * Spring Boot 교육 샘플 애플리케이션을 시작합니다.
     *
     * @param args 실행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(XyzApplication.class, args);
    }
}
