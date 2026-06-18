package cpf.cmn.mqe.core;

import java.util.Map;

/**
 * CPF 기능 설명입니다.
 */
public interface CmnMessagePublisher {

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    CmnMessagePublishResult publish(String key, Object payload);

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    CmnMessagePublishResult publish(String destination, String key, Object payload, Map<String, String> headers);
}

