package cpf.cmn.mqe.core;

import java.util.List;

/**
 * 硫붿떆吏 ?뚮퉬? 理쒓렐 硫붿떆吏 議고쉶瑜??쒓났?섎뒗 怨듯넻 ?명꽣?섏씠?ㅼ엯?덈떎.
 */
public interface CmnMessageConsumer {

    /**
     * 紐⑹쟻吏蹂??몃찓紐⑤━ Handler瑜??깅줉?⑸땲??
     *
     * @param destination Kafka topic ?먮뒗 Rabbit routing key
     * @param handler     硫붿떆吏 泥섎━ 肄쒕갚
     */
    void subscribe(String destination, CmnMessageHandler handler);

    /**
     * 援먯쑁/吏꾨떒??理쒓렐 硫붿떆吏瑜?議고쉶?⑸땲??
     *
     * @param destination 議고쉶??紐⑹쟻吏. null?대㈃ ?꾩껜 議고쉶
     * @param limit       議고쉶 嫄댁닔
     * @return 理쒓렐 硫붿떆吏 紐⑸줉
     */
    List<CmnMessageEnvelope> findRecentMessages(String destination, int limit);
}

