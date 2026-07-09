package cpf.xyz.edu.failure;

/**
 * 사용자 메시지와 내부 오류 원인을 분리하는 실패 응답 샘플입니다.
 */
public class XyzFailureEducationSample {

    public FailureResponse businessError(String code, String internalCause) {
        return new FailureResponse(code, "요청을 처리할 수 없습니다.", internalCause == null ? "MASKED" : "MASKED");
    }

    public record FailureResponse(String code, String userMessage, String internalCauseForLogOnly) {
    }
}
