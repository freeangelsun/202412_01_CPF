package cpf.adm.opr.service;

import cpf.adm.config.AdmPasswordPolicyProperties;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.exception.FpsValidationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 愿由ъ옄 鍮꾨?踰덊샇 ?뺤콉 寃利??쒕퉬?ㅼ엯?덈떎.
 *
 * <p>?깅줉/蹂寃????뺤콉 寃利앹쓣 ?대떦?섍퀬, 濡쒓렇???ㅽ뙣 ?좉툑 湲곗???媛숈? ?뺤콉 媛믪쑝濡??쒓났?⑸땲??</p>
 */
@Service
public class AdmPasswordPolicyService {
    private final AdmPasswordPolicyProperties properties;

    public AdmPasswordPolicyService(AdmPasswordPolicyProperties properties) {
        this.properties = properties;
    }

    /**
     * 鍮꾨?踰덊샇 ?뺤콉 ?꾨컲 紐⑸줉??諛섑솚?⑸땲??
     *
     * @param operatorId ?댁쁺??ID
     * @param password   寃利앺븷 鍮꾨?踰덊샇
     * @return ?꾨컲 硫붿떆吏 紐⑸줉
     */
    public List<String> validate(String operatorId, String password) {
        List<String> violations = new ArrayList<>();
        if (!TextUtils.hasText(password)) {
            violations.add("鍮꾨?踰덊샇???꾩닔?낅땲??");
            return violations;
        }
        if (password.length() < properties.getMinLength()) {
            violations.add("鍮꾨?踰덊샇??" + properties.getMinLength() + "???댁긽?댁뼱???⑸땲??");
        }
        if (TextUtils.hasText(operatorId) && password.toLowerCase().contains(operatorId.toLowerCase())) {
            violations.add("鍮꾨?踰덊샇???댁쁺??ID瑜??ы븿?????놁뒿?덈떎.");
        }
        if (categoryCount(password) < properties.getRequiredCategoryCount()) {
            violations.add("?곷Ц ?臾몄옄, ?곷Ц ?뚮Ц?? ?レ옄, ?뱀닔臾몄옄 以?"
                    + properties.getRequiredCategoryCount() + "醫??댁긽???ы븿?댁빞 ?⑸땲??");
        }
        return violations;
    }

    /**
     * ?꾨컲???덉쑝硫??쒖? 寃利??덉쇅瑜?諛쒖깮?쒗궢?덈떎.
     *
     * @param operatorId ?댁쁺??ID
     * @param password   寃利앺븷 鍮꾨?踰덊샇
     */
    public void requireValid(String operatorId, String password) {
        List<String> violations = validate(operatorId, password);
        if (!violations.isEmpty()) {
            throw new FpsValidationException("愿由ъ옄 鍮꾨?踰덊샇 ?뺤콉 ?꾨컲: " + String.join(", ", violations));
        }
    }

    /**
     * 鍮꾨?踰덊샇 留뚮즺 ?щ?瑜?怨꾩궛?⑸땲??
     *
     * @param changedAt 留덉?留?蹂寃??쒓컖
     * @return 留뚮즺 ?щ?
     */
    public boolean isExpired(LocalDateTime changedAt) {
        return changedAt == null || changedAt.plusDays(properties.getExpireDays()).isBefore(LocalDateTime.now());
    }

    /**
     * 濡쒓렇???ㅽ뙣 ?좉툑 湲곗? ?잛닔?낅땲??
     *
     * @return 理쒕? ?ㅽ뙣 ?덉슜 ?잛닔
     */
    public int maxFailCount() {
        return properties.getMaxFailCount();
    }

    private int categoryCount(String password) {
        int count = 0;
        if (password.chars().anyMatch(Character::isUpperCase)) {
            count++;
        }
        if (password.chars().anyMatch(Character::isLowerCase)) {
            count++;
        }
        if (password.chars().anyMatch(Character::isDigit)) {
            count++;
        }
        if (password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch))) {
            count++;
        }
        return count;
    }
}

