package cpf.cmn.mqe.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * CMN 硫붿떆吏?怨듯넻 紐⑤뱢??Spring Bean?쇰줈 ?쒖꽦?뷀빀?덈떎.
 *
 * <p>?ㅽ뻾 ?쒕퉬?ㅺ? {@code cpf.cmn} ?⑦궎吏瑜??ㅼ틪?섎㈃ ???ㅼ젙???④퍡 濡쒕뱶?섍퀬,
 * ?낅Т ?쒕퉬?ㅻ뒗 {@code CmnMessagePublisher}, {@code CmnMessageConsumer} ?명꽣?섏씠?ㅻ?
 * 二쇱엯諛쏆븘 釉뚮줈而?醫낅쪟? 臾닿??섍쾶 硫붿떆吏瑜?諛쒗뻾/援щ룆?????덉뒿?덈떎.</p>
 */
@Configuration
@EnableConfigurationProperties(CmnMessagingProperties.class)
public class CmnMessagingConfig {
}

