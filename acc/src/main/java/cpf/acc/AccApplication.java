package cpf.acc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ACC 紐⑤뱢??硫붿씤 ?좏뵆由ъ??댁뀡 吏꾩엯???대옒?ㅼ엯?덈떎.
 * Spring Boot 3.0 湲곕컲?쇰줈 ?ㅽ뻾?섎ŉ, CMN 怨듯넻 紐⑤뱢怨?ACC 紐⑤뱢???④퍡 ?ㅼ틪?섏뿬 而댄룷?뚰듃瑜??깅줉?⑸땲??
 */
@SpringBootApplication(scanBasePackages = {"cpf.pfw", "cpf.cmn", "cpf.acc"})
public class AccApplication {

	/**
	 * ?좏뵆由ъ??댁뀡 ?쒖옉 硫붿꽌??
	 * @param args SpringApplication???꾨떖?섎뒗 ?ㅽ뻾 ?몄옄
	 */
	public static void main(String[] args) {
		SpringApplication.run(AccApplication.class, args);
	}

}

