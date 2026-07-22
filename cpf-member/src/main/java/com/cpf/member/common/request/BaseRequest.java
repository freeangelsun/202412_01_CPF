package com.cpf.member.common.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 회원 API 요청에 서버가 주입하는 공통 실행 문맥입니다.
 *
 * <p>클라이언트가 본문으로 위조할 수 없도록 JSON 역직렬화에서 제외하고,
 * 인증 및 거래 헤더 처리 단계에서 신뢰할 수 있는 값만 설정합니다.</p>
 *
 * @author CPF Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 인증된 요청 사용자 식별자입니다. */
    @JsonIgnore
    private String requesterId;
    
    /** 요청을 유입시킨 채널 코드입니다. */
    @JsonIgnore
    private String channel;
    
    /** 프록시 신뢰 정책을 적용한 최종 클라이언트 IP입니다. */
    @JsonIgnore
    private String clientIp;
    
    /** 호출 클라이언트의 User-Agent 값입니다. */
    @JsonIgnore
    private String userAgent;
}

