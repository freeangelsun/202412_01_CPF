package fps.pfw.common.http;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 주제영역별 서버 접속 정보를 공통으로 관리합니다.
 * 예: fps.services.mbr.base-url=http://localhost:8081
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "fps")
public class FpsServiceEndpointProperties {

    /** 서비스 ID별 접속 정보입니다. 키는 mbr, acc, cmn 같은 주제영역 ID를 사용합니다. */
    private Map<String, ServiceEndpoint> services = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class ServiceEndpoint {

        /** 대상 서비스의 기본 URL입니다. 운영에서는 VIP, DNS, Kubernetes Service 주소 등을 사용합니다. */
        private String baseUrl;

        /** 로그와 예외 메시지에서 사람이 식별하기 쉬운 서비스 이름입니다. */
        private String description;
    }
}
