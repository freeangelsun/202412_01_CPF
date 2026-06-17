package cpf.pfw.common.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ?쒕툕 ⑤뱢 媛??쒕퉬??吏곸젒 ?몄텧??諛⑹??섍퀬 而⑦듃濡ㅻ윭 吏꾩엯??媛뺤젣?섎뒗 ?꾨젅?꾩썙???뺤콉 AOP?낅땲??
 * CMN 媛쒕컻 怨듯넻 ?쒕퉬?ㅼ? PFW ?꾨젅?꾩썙???대? ?쒕퉬?ㅻ뒗 怨듯넻 ?명봽?쇱씠誘濡??덉쇅濡??〓땲??
 */
@Aspect
@Component
public class ServiceAccessAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceAccessAspect.class);

    /**
     * ?낅Т ?쒕퉬?ㅻ뒗 而⑦듃濡ㅻ윭瑜??듯빐?쒕쭔 吏꾩엯?섎룄濡??쒗븳?⑸땲??
     * ?꾨젅?꾩썙???대? 濡쒓렇 ????쒕퉬?ㅻ뒗 ?대깽??由ъ뒪?덉뿉???몄텧?섎?濡??덉쇅 泥섎━?⑸땲??
     */
    @Before("within(cpf..service..*) && !within(cpf.cmn.service..*) && !within(cpf.pfw.service..*)")
    public void preventDirectServiceAccess() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        boolean isAllowedEntryPoint = false;

        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.contains(".controller.")
                    || className.contains(".filter.")
                    || className.contains(".interceptor.")) {
                isAllowedEntryPoint = true;
                break;
            }
        }

        if (!isAllowedEntryPoint) {
            logger.error("?쒕퉬??吏곸젒 ?몄텧??媛먯??섏뿀?듬땲?? 而⑦듃濡ㅻ윭/?꾪꽣/?명꽣?됲꽣 媛숈? ?쒖? 吏꾩엯?먯쓣 ?듯빐?쒕쭔 ?묎렐 媛?ν빀?덈떎.");
            throw new SecurityException("Direct service access is not allowed. Use standard web entry points.");
        }
    }
}

