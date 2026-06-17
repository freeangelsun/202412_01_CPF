package cpf.adm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ADM 愿由ъ옄 API 蹂댁븞 ?ㅼ젙?낅땲??
 *
 * <p>?꾩옱 援ы쁽? ?꾨젅?꾩썙???섑뵆???⑤룆 ?ㅽ뻾?????덈룄濡??몃찓⑤━ Bearer ?좏겙???ъ슜?⑸땲??
 * ?댁쁺 ?섍꼍?먯꽌??媛숈? API 怨꾩빟???좎???梨?Redis, DB ?몄뀡, ?щ궡 SSO, JWT ?쒕챸??諛⑹떇?쇰줈
 * ?몄뀡 ??μ냼留?援먯껜?????덉뒿?덈떎.</p>
 */
@ConfigurationProperties(prefix = "cpf.adm.security")
public class AdmSecurityProperties {
    private boolean enabled = true;
    private long sessionTtlSeconds = 3600;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getSessionTtlSeconds() {
        return sessionTtlSeconds;
    }

    public void setSessionTtlSeconds(long sessionTtlSeconds) {
        this.sessionTtlSeconds = sessionTtlSeconds;
    }
}

