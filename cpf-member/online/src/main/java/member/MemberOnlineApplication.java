package member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** member Generated API 실행 진입점입니다. */
@SpringBootApplication(scanBasePackages="member")
@MapperScan(basePackages="member.sample.repository", sqlSessionFactoryRef="cpfDomainSqlSessionFactory")
public class MemberOnlineApplication {
    public static void main(String[] args) { SpringApplication.run(MemberOnlineApplication.class,args); }
}
