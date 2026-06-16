package cpf.pfw.common.http;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 二쇱젣?곸뿭蹂??쒕쾭 ?묒냽 ?뺣낫瑜?怨듯넻?쇰줈 愿由ы빀?덈떎.
 * ?? cpf.services.mbr.base-url=http://localhost:8081
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "fps")
public class FpsServiceEndpointProperties {

    /** ?쒕퉬??ID蹂??묒냽 ?뺣낫?낅땲?? ?ㅻ뒗 mbr, acc, cmn 媛숈? 二쇱젣?곸뿭 ID瑜??ъ슜?⑸땲?? */
    private Map<String, ServiceEndpoint> services = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class ServiceEndpoint {

        /** ????쒕퉬?ㅼ쓽 湲곕낯 URL?낅땲?? ?댁쁺?먯꽌??VIP, DNS, Kubernetes Service 二쇱냼 ?깆쓣 ?ъ슜?⑸땲?? */
        private String baseUrl;

        /** 濡쒓렇? ?덉쇅 硫붿떆吏?먯꽌 ?щ엺???앸퀎?섍린 ?ъ슫 ?쒕퉬???대쫫?낅땲?? */
        private String description;
    }
}

