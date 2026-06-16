package cpf.pfw.common.logging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 而⑦듃濡ㅻ윭 ?먮뒗 而⑦듃濡ㅻ윭 ?대옒?ㅼ뿉 ?낅Т 嫄곕옒ID? 嫄곕옒紐낆쓣 ?좎뼵?섎뒗 PFW ?쒖? ?대끂?뚯씠?섏엯?덈떎.
 *
 * <p>PFW 濡쒓렇 AOP?????대끂?뚯씠?섏쓣 ?쎌뼱 TRAN_LOG?? * {@code BUSINESS_TRANSACTION_ID}, {@code BUSINESS_TRANSACTION_NAME} 而щ읆????ν빀?덈떎.
 * 媛쒕컻?먮뒗 ?좉퇋 API瑜?留뚮뱾 ??諛섎뱶???낅Т 嫄곕옒ID? ?щ엺???쎌쓣 ???덈뒗 嫄곕옒紐낆쓣 ?④퍡 ?좎뼵?댁빞 ?⑸땲??</p>
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FpsTransaction {
    /**
     * ?낅Т 嫄곕옒ID?낅땲??
     * ?뺤떇: {二쇱젣?곸뿭3}{嫄곕옒?좏삎2}{以묎컙?꾨찓??}{?쇰젴踰덊샇4}
     * ?? MBR01BSE0001
     */
    String id();

    /**
     * 濡쒓렇? 愿由??붾㈃???쒖떆???낅Т 嫄곕옒紐낆엯?덈떎.
     */
    String name();
}

