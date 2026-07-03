package cpf.exs;

import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 대외 연계 주제영역 기준 샘플 애플리케이션입니다.
 *
 * <p>교육 API는 DB가 없어도 기동할 수 있어야 하므로 기본 datasource 자동설정을 제외합니다.
 * 실제 연계 로그 적재가 필요한 런타임 검증에서는 cpf.exs.datasource.enabled=true로 EXS datasource를 명시적으로 켭니다.</p>
 */
@SpringBootApplication(
        scanBasePackages = "cpf",
        exclude = {DataSourceAutoConfiguration.class, MybatisAutoConfiguration.class})
public class ExsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExsApplication.class, args);
    }
}
