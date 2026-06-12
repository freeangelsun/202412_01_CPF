package fps.acc.bse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ACC에서 MBR 회원 상세조회 API 응답을 받을 때 사용하는 샘플 DTO입니다.
 * MBR 모듈의 Java 클래스를 직접 참조하지 않고 HTTP 응답 계약만 맞춰 의존성을 낮춥니다.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MbrMemberDetailResponse {

    /** 응답 메시지 ID입니다. */
    private String messageId;

    /** 글로벌 거래ID입니다. ACC와 MBR 로그가 같은 값으로 연결되어야 합니다. */
    private String transactionId;

    /** 기술 로그 추적용 Trace ID입니다. */
    private String traceId;

    /** 업무 응답 코드입니다. */
    private String statusCode;

    /** 업무 응답 메시지입니다. */
    private String message;

    /** 회원 상세 데이터입니다. */
    private MbrMemberData data;

    /** 응답 생성 일시입니다. */
    private LocalDateTime timestamp;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MbrMemberData {

        /** 회원 ID입니다. */
        private Integer memberId;

        /** 회원명입니다. */
        private String memberName;

        /** 회원 설명입니다. */
        private String description;

        /** 생성자 ID입니다. */
        private String createdBy;

        /** 생성 일시입니다. */
        private LocalDateTime createdAt;

        /** 수정자 ID입니다. */
        private String updatedBy;

        /** 수정 일시입니다. */
        private LocalDateTime updatedAt;
    }
}
