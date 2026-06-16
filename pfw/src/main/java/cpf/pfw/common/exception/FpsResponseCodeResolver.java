package cpf.pfw.common.exception;

import java.util.Locale;
import java.util.Map;

/**
 * Resolves FPS response codes through the common response/message catalog.
 */
public interface FpsResponseCodeResolver {
    FpsResolvedResponse resolve(
            String responseCode,
            Locale locale,
            Map<String, Object> arguments,
            String detail);

    FpsResolvedResponse resolve(
            FpsErrorDefinition errorDefinition,
            Locale locale,
            Map<String, Object> arguments,
            String detail);
}

