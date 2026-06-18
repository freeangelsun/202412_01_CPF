package cpf.mbr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * ?뚯썝 愿由?⑤뱢 Spring Boot ?좏뵆由ъ??댁뀡 吏꾩엯??
 * 
 * 二쇱슂 ?뱀쭠:
 * - 湲덉쑖沅??쒖? REST API ?쒓났
 * - ?듭씪???묐떟/?먮윭 泥섎━
 * - AOP 湲곕컲 ?먮룞 濡쒓퉭
 * - ?낅젰媛?寃利?
 * - 媛먯떆(Audit) 湲곕뒫 ?댁옣
 * 
 * ?꾨줈?뚯씪:
 * - local: 濡쒖뺄 媛쒕컻 ?섍꼍 (MariaDB localhost:3306)
 * - dev: 媛쒕컻 ?쒕쾭
 * - prod: ?댁쁺 ?쒕쾭
 * 
 * ?ㅽ뻾 諛⑸쾿:
 *   ./gradlew :mbr:bootRun --args='--spring.profiles.active=local'
 * 
 * @author CPF Team
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"cpf.pfw", "cpf.cmn", "cpf.mbr"})
public class MbrApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MbrApplication.class, args);
    }
}

