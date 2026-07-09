package cpf.cmn.edu.message;

import java.util.Locale;
import java.util.Map;

/**
 * 프로젝트 공통 메시지/코드 조회 helper 샘플입니다.
 */
public class CmnMessageCodeEducationSample {
    private final Map<String, String> messages = Map.of(
            "ko:CMN-0001", "정상 처리되었습니다.",
            "en:CMN-0001", "Completed successfully.");

    public String message(Locale locale, String messageCode) {
        return messages.getOrDefault(locale.getLanguage() + ":" + messageCode, messageCode);
    }
}
