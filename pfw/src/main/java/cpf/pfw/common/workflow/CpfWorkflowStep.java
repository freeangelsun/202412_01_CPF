package cpf.pfw.common.workflow;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ?뚰겕?뚮줈???덉뿉???ㅼ젣 ?섑뻾?섎뒗 ???④퀎??嫄곕옒瑜??좎뼵?⑸땲??
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CpfWorkflowStep {
    /**
     * ?뚰겕?뚮줈???덉뿉???꾩옱 嫄곕옒瑜?援щ텇?섎뒗 ?ㅽ뀦 ID?낅땲??
     * ?앸왂?섎㈃ PFW 濡쒓렇 AOP媛 ?꾩옱 {@code @CpfTransaction.id}瑜??ㅽ뀦 ID濡??ъ슜?⑸땲??
     */
    String id() default "";

    /**
     * 濡쒓렇 ?붾㈃???쒖떆???ㅽ뀦 ?쇰━낆엯?덈떎.
     * ?앸왂?섎㈃ PFW 濡쒓렇 AOP媛 ?꾩옱 {@code @CpfTransaction.name}???ㅽ뀦낆쑝濡??ъ슜?⑸땲??
     */
    String name() default "";

    /**
     * ???ㅽ뀦???ㅽ뙣?덉쓣 ???댁쁺?곸쑝濡??대뼡 ?곹깭濡??④만吏 寃곗젙?섎뒗 ?뺤콉?낅땲??
     */
    CpfWorkflowFailurePolicy failurePolicy() default CpfWorkflowFailurePolicy.FAIL;

    /**
     * true?대㈃ ??嫄곕옒 ?먯껜媛 蹂댁긽 嫄곕옒?꾩쓣 ?섎??⑸땲??
     */
    boolean compensation() default false;

    /**
     * ???④퀎 ?ㅽ뙣 ???몄텧?섍굅???湲곗떆??蹂댁긽 嫄곕옒ID?낅땲??
     */
    String compensationTransactionId() default "";

    /**
     * 蹂댁긽 嫄곕옒??寃쎌슦 ?대뼡 ?먭굅?섎? 蹂댁긽?섎뒗吏 湲곕줉?⑸땲??
     */
    String compensationTargetTransactionId() default "";
}

