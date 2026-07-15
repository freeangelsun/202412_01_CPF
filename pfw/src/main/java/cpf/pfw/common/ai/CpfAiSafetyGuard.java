package cpf.pfw.common.ai;

import cpf.pfw.common.logging.SensitiveDataMasker;

import java.util.List;
import java.util.Locale;

/** AI 입력의 크기, prompt injection 징후, 민감정보 마스킹을 공통 처리합니다. */
public final class CpfAiSafetyGuard {
    private static final int MAX_PROMPT_LENGTH = 20_000;
    private static final List<String> INJECTION_MARKERS = List.of(
            "ignore previous", "ignore all previous", "system prompt", "developer message",
            "이전 지시를 무시", "시스템 프롬프트", "개발자 메시지 공개");

    private CpfAiSafetyGuard() {
    }

    public static String inspectAndMask(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("AI prompt는 필수입니다.");
        }
        if (prompt.length() > MAX_PROMPT_LENGTH) {
            throw new IllegalArgumentException("AI prompt 허용 길이를 초과했습니다.");
        }
        String normalized = prompt.toLowerCase(Locale.ROOT);
        if (INJECTION_MARKERS.stream().anyMatch(normalized::contains)) {
            throw new IllegalArgumentException("prompt injection 의심 입력을 차단했습니다.");
        }
        return SensitiveDataMasker.mask(prompt, MAX_PROMPT_LENGTH);
    }

    public static String validateOutput(String output) {
        if (output == null || output.isBlank()) {
            throw new IllegalArgumentException("AI 응답이 비어 있습니다.");
        }
        return SensitiveDataMasker.mask(output, MAX_PROMPT_LENGTH);
    }
}
