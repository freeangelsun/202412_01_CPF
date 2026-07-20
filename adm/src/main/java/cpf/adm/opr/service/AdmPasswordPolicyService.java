package cpf.adm.opr.service;

import cpf.adm.config.AdmPasswordPolicyProperties;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.exception.CpfValidationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ADM 운영자 비밀번호 정책을 검증합니다.
 */
@Service
public class AdmPasswordPolicyService extends cpf.adm.common.base.AdmBaseService {
    private final AdmPasswordPolicyProperties properties;

    public AdmPasswordPolicyService(AdmPasswordPolicyProperties properties) {
        this.properties = properties;
    }

    /**
     * 비밀번호 정책 위반 목록을 반환합니다.
     */
    public List<String> validate(String operatorId, String password) {
        List<String> violations = new ArrayList<>();
        if (!TextUtils.hasText(password)) {
            violations.add("비밀번호는 필수입니다.");
            return violations;
        }
        if (password.length() < properties.getMinLength()) {
            violations.add("비밀번호는 " + properties.getMinLength() + "자 이상이어야 합니다.");
        }
        if (TextUtils.hasText(operatorId) && password.toLowerCase().contains(operatorId.toLowerCase())) {
            violations.add("비밀번호에는 운영자 ID를 포함할 수 없습니다.");
        }
        if (categoryCount(password) < properties.getRequiredCategoryCount()) {
            violations.add("영문 대문자, 영문 소문자, 숫자, 특수문자 중 "
                    + properties.getRequiredCategoryCount() + "개 이상 유형을 포함해야 합니다.");
        }
        return violations;
    }

    /**
     * 비밀번호가 정책을 만족하지 않으면 예외를 발생시킵니다.
     */
    public void requireValid(String operatorId, String password) {
        List<String> violations = validate(operatorId, password);
        if (!violations.isEmpty()) {
            throw new CpfValidationException("ADM 비밀번호 정책 위반: " + String.join(", ", violations));
        }
    }

    /**
     * 비밀번호 만료 여부를 반환합니다.
     */
    public boolean isExpired(LocalDateTime changedAt) {
        return changedAt == null || changedAt.plusDays(properties.getExpireDays()).isBefore(LocalDateTime.now());
    }

    public int maxFailCount() {
        return properties.getMaxFailCount();
    }

    /**
     * 현재 비밀번호를 포함해 재사용을 금지할 최근 비밀번호 개수를 반환합니다.
     */
    public int historyCount() {
        return Math.max(1, properties.getHistoryCount());
    }

    public Map<String, Object> currentPolicy() {
        return Map.of(
                "minLength", properties.getMinLength(),
                "requiredCategoryCount", properties.getRequiredCategoryCount(),
                "maxFailCount", properties.getMaxFailCount(),
                "historyCount", historyCount(),
                "expireDays", properties.getExpireDays());
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
