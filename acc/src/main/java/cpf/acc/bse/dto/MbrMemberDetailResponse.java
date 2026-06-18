package cpf.acc.bse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MBR 회원 상세 조회 응답입니다.
 * ACC 모듈은 이 DTO로 MBR API 응답을 역직렬화합니다.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MbrMemberDetailResponse {

    /** 응답 메시지 ID입니다. */
    private String messageId;

    /** ACC와 MBR 호출을 같은 흐름으로 추적하기 위한 거래 ID입니다. */
    private String transactionId;

    /** 분산 추적에 사용하는 Trace ID입니다. */
    private String traceId;

    /** 처리 결과 코드입니다. */
    private String statusCode;

    /** 처리 결과 메시지입니다. */
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

        /** 회원 이름입니다. */
        private String memberName;

        /** 회원 설명입니다. */
        private String description;

        /** 등록자입니다. */
        private String createdBy;

        /** 등록 일시입니다. */
        private LocalDateTime createdAt;

        /** 수정자입니다. */
        private String updatedBy;

        /** 수정 일시입니다. */
        private LocalDateTime updatedAt;
    }
}
