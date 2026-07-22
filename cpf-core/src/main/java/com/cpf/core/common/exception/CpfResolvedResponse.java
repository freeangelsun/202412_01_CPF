package cpf.pfw.common.exception;

/**
 * Fully resolved response metadata derived from a response code and message code.
 */
public record CpfResolvedResponse(
        int httpStatus,
        String responseCode,
        String messageCode,
        String externalMessage,
        String internalMessage,
        String errorCode,
        String errorMessage) {
}

