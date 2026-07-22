package cpf.mbr.common.response;

/**
 * MBR 표준 응답 코드입니다.
 */
public enum ResponseCode {
    SUCCESS("SMBR000000", "MMBR000000", "정상 처리되었습니다."),
    CREATED("SMBR010001", "MMBR010001", "등록 처리되었습니다."),
    UPDATED("SMBR010002", "MMBR010002", "수정 처리되었습니다."),
    DELETED("SMBR010003", "MMBR010003", "삭제 처리되었습니다."),

    BAD_REQUEST("EMBR010001", "MMBR010101", "요청 형식이 올바르지 않습니다."),
    INVALID_PARAMETER("EMBR010002", "MMBR010102", "요청 파라미터가 올바르지 않습니다."),
    NOT_FOUND("EMBR010003", "MMBR010103", "요청한 회원 정보를 찾을 수 없습니다."),
    DUPLICATE("EMBR010004", "MMBR010104", "이미 등록된 회원 정보입니다."),
    VALIDATION_FAILED("EMBR010005", "MMBR010105", "입력값 검증에 실패했습니다."),
    UNAUTHORIZED("EMBR010006", "MMBR010106", "인증이 필요합니다."),
    FORBIDDEN("EMBR010007", "MMBR010107", "처리 권한이 없습니다."),

    INTERNAL_SERVER_ERROR("EMBR990000", "MMBR990000", "처리 중 오류가 발생했습니다."),
    DATABASE_ERROR("EMBR990001", "MMBR990001", "데이터베이스 처리 중 오류가 발생했습니다."),
    EXTERNAL_SERVICE_ERROR("EMBR030001", "MMBR030001", "외부 서비스 호출 중 오류가 발생했습니다.");

    private final String code;
    private final String messageCode;
    private final String message;

    ResponseCode(String code, String messageCode, String message) {
        this.code = code;
        this.messageCode = messageCode;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public String getMessage() {
        return message;
    }
}
