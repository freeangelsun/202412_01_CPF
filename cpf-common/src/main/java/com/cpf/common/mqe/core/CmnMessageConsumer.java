package cpf.cmn.mqe.core;

import java.util.List;

/**
 * CPF 기능 설명입니다.
 */
public interface CmnMessageConsumer {

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    void subscribe(String destination, CmnMessageHandler handler);

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    List<CmnMessageEnvelope> findRecentMessages(String destination, int limit);
}

