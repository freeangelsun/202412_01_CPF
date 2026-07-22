package cpf.pfw.common.exception;

import java.util.Locale;
import java.util.Map;

/** CPF 응답코드와 메시지 카탈로그를 해석합니다. */
public interface CpfResponseCodeResolver {
    CpfResolvedResponse resolve(
            String responseCode,
            Locale locale,
            Map<String, Object> arguments,
            String detail);

    CpfResolvedResponse resolve(
            CpfErrorDefinition errorDefinition,
            Locale locale,
            Map<String, Object> arguments,
            String detail);
}

