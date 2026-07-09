package cpf.pfw.common.masking;

/**
 * 개인정보와 secret을 원문 노출하지 않는 마스킹 정책 샘플입니다.
 */
public class PfwMaskingPolicyEducationSample {

    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "";
        }
        int at = email.indexOf('@');
        String id = email.substring(0, at);
        String domain = email.substring(at);
        return id.substring(0, 1) + "***" + domain;
    }

    public String maskSecret(String ignoredRawValue) {
        return "****";
    }
}
