package cpf.cmn.mqe.core;

/**
 * 硫붿떆吏 ?뚮퉬 肄쒕갚?낅땲??
 *
 * <p>濡쒖뺄 ?몃찓⑤━ 釉뚮줈而ㅼ뿉?쒕뒗 諛쒗뻾 利됱떆 ?깅줉??Handler媛 ?몄텧?⑸땲??
 * Kafka/RabbitMQ ?댁쁺 ?뚮퉬?먮뒗 媛??쒕퉬?ㅼ쓽 {@code @KafkaListener} ?먮뒗
 * {@code @RabbitListener}?먯꽌 ??Handler 援ъ“瑜??ъ궗?⑺븯??諛⑹떇?쇰줈 ?뺤옣?⑸땲??</p>
 */
@FunctionalInterface
public interface CmnMessageHandler {

    /**
     * ?쒖? Envelope 硫붿떆吏瑜?泥섎━?⑸땲??
     *
     * @param envelope 嫄곕옒 異붿쟻 ?ㅻ뜑媛 ?ы븿??硫붿떆吏
     */
    void handle(CmnMessageEnvelope envelope);
}

