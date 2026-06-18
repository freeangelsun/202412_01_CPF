package cpf.pfw.common.exception;

import org.springframework.http.HttpStatus;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
public interface CpfErrorDefinition {

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    String getStatusCode();

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    String getMessageCode();

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    HttpStatus getHttpStatus();

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    String getDefaultExternalMessage();

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    String getDefaultInternalMessage();

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다. */
    default String getExternalMessageKey() {
        return getMessageCode();
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다. */
    default String getInternalMessageKey() {
        return getMessageCode();
    }
}

