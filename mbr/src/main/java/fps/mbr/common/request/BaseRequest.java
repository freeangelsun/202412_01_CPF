package fps.mbr.common.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 금융권 표준 요청 객체
 * 모든 API 요청에서 공통으로 포함되어야 할 메타 정보 정의
 * 
 * @author FPS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 요청자 ID (감시/로깅용)
     * - 나중에 security context에서 자동 설정
     */
    @JsonIgnore
    private String requesterId;
    
    /**
     * 요청 채널 (모바일, WEB, API 등)
     * - 금융권 감시 로그에 필수
     */
    @JsonIgnore
    private String channel;
    
    /**
     * 요청 IP 주소
     * - 접근 제어 및 감시에 사용
     */
    @JsonIgnore
    private String clientIp;
    
    /**
     * 사용자 에이전트
     * - 감시 및 통계에 사용
     */
    @JsonIgnore
    private String userAgent;
}
