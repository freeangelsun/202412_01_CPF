package cpf.adm;

import cpf.adm.config.AdmPasswordPolicyProperties;
import cpf.adm.config.AdmSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * ADM 愿由ъ옄 二쇱젣?곸뿭 ?좏뵆由ъ??댁뀡?낅땲??
 *
 * <p>ADM? PFW/CMN???쇱씠釉뚮윭由щ줈 ?ы븿??嫄곕옒 濡쒓렇 議고쉶, 罹먯떆 由ы봽?덉떆,
 * ?숈쟻 濡쒓렇?덈꺼, ?댁쁺??沅뚰븳/硫붾돱 愿由?湲곕뒫???쒓났?⑸땲??</p>
 */
@SpringBootApplication(scanBasePackages = {"cpf.pfw", "cpf.cmn", "cpf.adm"})
@EnableConfigurationProperties({AdmPasswordPolicyProperties.class, AdmSecurityProperties.class})
public class AdmApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdmApplication.class, args);
    }
}

