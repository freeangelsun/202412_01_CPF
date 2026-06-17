package cpf.mbr.common.logging;

import cpf.pfw.common.logging.TransactionIdGenerator;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PFW 湲濡쒕쾶 嫄곕옒ID ?앹꽦 洹쒖튃???뺤씤?섎뒗 ?뚯뒪?몄엯?덈떎.
 *
 * <p>嫄곕옒ID??⑤뱺 二쇱젣?곸뿭 濡쒓렇瑜??섎굹濡?臾띕뒗 媛??以묒슂???ㅼ엯?덈떎.
 * ?곕씪???앹꽦 ?쒓컖, ?앹꽦 二쇱젣?곸뿭, WAS ID, ?쇰젴踰덊샇媛 ?뺥빐吏??쒖꽌濡?議고빀?섎뒗吏 ?뺤씤?⑸땲??</p>
 */
class TransactionIdGeneratorTest {

    /**
     * 怨좎젙 ?쒓컖???ъ슜??嫄곕옒ID ?щ㎎怨??쇰젴踰덊샇 利앷?瑜?寃利앺빀?덈떎.
     */
    @Test
    void generatesTransactionIdWithTimestampModuleWasAndSequence() {
        // ?뚯뒪?멸? ?ㅽ뻾?섎뒗 ?ㅼ젣 ?쒓컙???붾뱾由ъ? ?딅룄濡?Clock??怨좎젙?⑸땲??
        Clock clock = Clock.fixed(
                Instant.parse("2026-06-11T05:12:34.567Z"),
                ZoneId.of("Asia/Seoul"));
        // ACC 二쇱젣?곸뿭, accAP01 WAS, 7?먮━ ?쇰젴踰덊샇 湲곗????앹꽦湲곕? 留뚮벊?덈떎.
        TransactionIdGenerator generator = new TransactionIdGenerator("ACC", "accAP01", 7, clock);

        // 泥?踰덉㎏ ?앹꽦 媛믪? ?쇰젴踰덊샇 0000001濡??앸굹???⑸땲??
        assertThat(generator.generate()).isEqualTo("20260611141234567ACCaccAP010000001");
        // 媛숈? 諛由ъ큹 ?덉뿉????踰????앹꽦?섎㈃ ?쇰젴踰덊샇留?0000002濡?利앷??댁빞 ?⑸땲??
        assertThat(generator.generate()).isEqualTo("20260611141234567ACCaccAP010000002");
    }

    /**
     * ?몃??먯꽌 ?ㅼ뼱??嫄곕옒ID瑜??ъ궗?⑺븷吏 ?덈줈 留뚮뱾吏 ?먮떒?섎뒗 洹쒖튃??寃利앺빀?덈떎.
     */
    @Test
    void reusesOnlyValidIncomingTransactionId() {
        // 鍮꾩젙??嫄곕옒ID媛 ?ㅼ뼱?붿쓣 ????嫄곕옒ID ?앹꽦 媛믪쓣 ?덉륫?????덈룄濡??쒓컙??怨좎젙?⑸땲??
        Clock clock = Clock.fixed(
                Instant.parse("2026-06-11T05:12:34.567Z"),
                ZoneId.of("Asia/Seoul"));
        // MBR 二쇱젣?곸뿭 湲곗? ?앹꽦湲곕? 留뚮뱾??鍮꾩젙???낅젰 ??MBR 湲곗? 嫄곕옒ID媛 ?앹꽦?섎뒗吏 ?뺤씤?⑸땲??
        TransactionIdGenerator generator = new TransactionIdGenerator("MBR", "mbrAP01", 7, clock);

        // ?쒖? ?щ㎎??嫄곕옒ID???곸쐞 梨꾨꼸?대굹 ?곸쐞 二쇱젣?곸뿭?먯꽌 ??媛믪씠誘濡?洹몃?濡??댁뼱諛쏆뒿?덈떎.
        assertThat(generator.generateOrUse("20260611141234567ACCaccAP010000001"))
                .isEqualTo("20260611141234567ACCaccAP010000001");
        // 鍮꾪몴以 嫄곕옒ID??異붿쟻 ?덉쭏???댁튂誘濡??꾩옱 二쇱젣?곸뿭 湲곗?????嫄곕옒ID濡?援먯껜?⑸땲??
        assertThat(generator.generateOrUse("TRX-TEST-001"))
                .isEqualTo("20260611141234567MBRmbrAP010000001");
    }
}

