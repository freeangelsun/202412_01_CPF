package cpf.exs.edu.unknown;

/**
 * 대외 호출 결과 불명확 상태를 표준 후보로 분리하는 샘플입니다.
 */
public class ExsUnknownResultEducationSample {

    public UnknownResult classify(String responseCode) {
        boolean unknown = responseCode == null || responseCode.isBlank() || "TIMEOUT".equals(responseCode);
        return new UnknownResult(unknown, unknown ? "RECONCILE" : "CONFIRMED");
    }

    public record UnknownResult(boolean unknown, String action) {
    }
}
