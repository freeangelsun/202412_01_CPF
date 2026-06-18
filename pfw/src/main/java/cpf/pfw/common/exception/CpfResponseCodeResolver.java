package cpf.pfw.common.exception;

import java.util.Locale;
import java.util.Map;

/**
 * Resolves CPF response codes through the common response/message catalog.
 */
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

