package cpf.cmn.mqe.core;

import java.util.Map;

/**
 * CMN 硫붿떆吏??쒖? Envelope?낅땲??
 *
 * <p>?낅Т Payload留?MQ濡?蹂대궡硫?嫄곕옒ID, TraceId, ?뚯썝踰덊샇 媛숈? 異붿쟻 媛믪씠 ?좎떎?????덉쑝誘濡? * CMN? 紐⑤뱺 硫붿떆吏瑜?Envelope濡?媛먯떥怨?PFW 嫄곕옒 ?꾪뙆 ?ㅻ뜑瑜??④퍡 ?댁뒿?덈떎.</p>
 *
 * @param broker      ?ъ슜 釉뚮줈而? * @param destination Kafka topic ?먮뒗 Rabbit routing key
 * @param key         ?뚰떚???쇱슦?낆뿉 ?ъ슜???낅Т ?? * @param payload     ?낅Т 硫붿떆吏 蹂몃Ц
 * @param headers     嫄곕옒ID, TraceId, 梨꾨꼸 ???꾪뙆 ?ㅻ뜑
 * @param createdAt   諛쒗뻾 ?쒓컖
 */
public record CmnMessageEnvelope(
        String broker,
        String destination,
        String key,
        Object payload,
        Map<String, String> headers,
        String createdAt) {
}

