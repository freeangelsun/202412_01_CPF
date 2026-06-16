package cpf.pfw.common.http;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 二쇱젣?곸뿭 媛?HTTP ?몄텧??怨듯넻?쇰줈 ?곸슜??WebClient 湲곕낯 ?ㅼ젙?낅땲??
 * 媛??쒕퉬?ㅼ쓽 媛쒕퀎 二쇱냼??{@link FpsServiceEndpointProperties}?먯꽌 愿由ы빀?덈떎.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "cpf.http-client")
public class FpsHttpClientProperties {

    /** ?쒕쾭 ?곌껐??留브린源뚯? 湲곕떎由щ뒗 理쒕? ?쒓컙?낅땲?? */
    private int connectTimeoutMillis = 3000;

    /** ?묐떟??諛쏄린源뚯? 湲곕떎由щ뒗 理쒕? ?쒓컙?낅땲?? */
    private int readTimeoutMillis = 5000;

    /** ?묐떟 Body瑜?硫붾え由ъ뿉 ?곸옱?????덈뒗 理쒕? ?ш린?낅땲?? */
    private int maxInMemorySizeKb = 2048;
}

