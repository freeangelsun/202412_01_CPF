package cpf.exs;

import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 대외 연계 주제영역 기준 샘플 애플리케이션입니다.
 *
 * <p>샘플 API는 정적 교육 데이터를 반환하므로 DB가 없어도 기동할 수 있게 datasource와
 * MyBatis 자동설정을 제외합니다. 실제 프로젝트에서는 exsDB를 붙인 뒤 제외 설정을 제거합니다.</p>
 */
@SpringBootApplication(
        scanBasePackages = "cpf",
        exclude = {DataSourceAutoConfiguration.class, MybatisAutoConfiguration.class})
public class ExsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExsApplication.class, args);
    }
}
