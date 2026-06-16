package cpf.acc.bse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ACC?먯꽌 MBR ?뚯썝 ?곸꽭議고쉶 API ?묐떟??諛쏆쓣 ???ъ슜?섎뒗 ?섑뵆 DTO?낅땲??
 * MBR 紐⑤뱢??Java ?대옒?ㅻ? 吏곸젒 李몄“?섏? ?딄퀬 HTTP ?묐떟 怨꾩빟留?留욎떠 ?섏〈?깆쓣 ??땅?덈떎.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MbrMemberDetailResponse {

    /** ?묐떟 硫붿떆吏 ID?낅땲?? */
    private String messageId;

    /** 湲濡쒕쾶 嫄곕옒ID?낅땲?? ACC? MBR 濡쒓렇媛 媛숈? 媛믪쑝濡??곌껐?섏뼱???⑸땲?? */
    private String transactionId;

    /** 湲곗닠 濡쒓렇 異붿쟻??Trace ID?낅땲?? */
    private String traceId;

    /** ?낅Т ?묐떟 肄붾뱶?낅땲?? */
    private String statusCode;

    /** ?낅Т ?묐떟 硫붿떆吏?낅땲?? */
    private String message;

    /** ?뚯썝 ?곸꽭 ?곗씠?곗엯?덈떎. */
    private MbrMemberData data;

    /** ?묐떟 ?앹꽦 ?쇱떆?낅땲?? */
    private LocalDateTime timestamp;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MbrMemberData {

        /** ?뚯썝 ID?낅땲?? */
        private Integer memberId;

        /** ?뚯썝紐낆엯?덈떎. */
        private String memberName;

        /** ?뚯썝 ?ㅻ챸?낅땲?? */
        private String description;

        /** ?앹꽦??ID?낅땲?? */
        private String createdBy;

        /** ?앹꽦 ?쇱떆?낅땲?? */
        private LocalDateTime createdAt;

        /** ?섏젙??ID?낅땲?? */
        private String updatedBy;

        /** ?섏젙 ?쇱떆?낅땲?? */
        private LocalDateTime updatedAt;
    }
}

