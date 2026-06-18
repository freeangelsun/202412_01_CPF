package cpf.adm;

import cpf.adm.config.AdmPasswordPolicyProperties;
import cpf.adm.config.AdmSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
@SpringBootApplication(scanBasePackages = {"cpf.pfw", "cpf.cmn", "cpf.adm"})
@EnableConfigurationProperties({AdmPasswordPolicyProperties.class, AdmSecurityProperties.class})
public class AdmApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdmApplication.class, args);
    }
}

