package fps.pfw.common.http;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 주제영역 간 HTTP 호출에 공통으로 적용할 WebClient 기본 설정입니다.
 * 각 서비스의 개별 주소는 {@link FpsServiceEndpointProperties}에서 관리합니다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "fps.http-client")
public class FpsHttpClientProperties {

    /** 서버 연결을 맺기까지 기다리는 최대 시간입니다. */
    private int connectTimeoutMillis = 3000;

    /** 응답을 받기까지 기다리는 최대 시간입니다. */
    private int readTimeoutMillis = 5000;

    /** 응답 Body를 메모리에 적재할 수 있는 최대 크기입니다. */
    private int maxInMemorySizeKb = 2048;
}
